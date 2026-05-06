package com.movielog.app.ui.detail;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.movielog.app.data.model.FavoriteMovie;
import com.movielog.app.data.model.Movie;
import com.movielog.app.data.model.MovieDetail;
import com.movielog.app.data.model.SearchResponse;
import com.movielog.app.data.model.TmdbCreditsResponse;
import com.movielog.app.data.model.TmdbVideoResponse;
import com.movielog.app.data.repository.FavoriteRepository;
import com.movielog.app.data.repository.MovieRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailViewModel extends AndroidViewModel {

    private MovieRepository movieRepository;
    private FavoriteRepository favoriteRepository;
    private MutableLiveData<MovieDetail> movieDetail = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isFavorite = new MutableLiveData<>();
    private MutableLiveData<String> trailerVideoId = new MutableLiveData<>();
    private MutableLiveData<List<TmdbCreditsResponse.TmdbCast>> movieCast = new MutableLiveData<>();
    private MutableLiveData<List<Movie>> similarMovies = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public DetailViewModel(Application application) {
        super(application);
        movieRepository = new MovieRepository();
        favoriteRepository = new FavoriteRepository(application);
    }

    public void loadMovieDetail(String imdbId) {
        isLoading.setValue(true);
        movieRepository.getMovieDetail(imdbId).enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetail detail = response.body();
                    movieDetail.setValue(detail);
                    checkIsFavorite(imdbId);
                    loadTrailer(imdbId);
                    loadCredits(imdbId);
                    if (detail.getGenre() != null && !detail.getGenre().isEmpty()) {
                        String firstGenre = detail.getGenre().split(",")[0].trim();
                        loadSimilarMovies(firstGenre);
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }

    private void loadTrailer(String imdbId) {
        movieRepository.getMovieTrailer(imdbId).enqueue(new Callback<TmdbVideoResponse>() {
            @Override
            public void onResponse(Call<TmdbVideoResponse> call, Response<TmdbVideoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (TmdbVideoResponse.TmdbVideo video : response.body().getResults()) {
                        if ("YouTube".equals(video.getSite()) && "Trailer".equals(video.getType())) {
                            trailerVideoId.setValue(video.getKey());
                            return;
                        }
                    }
                    // Eğer "Trailer" tipinde bir video bulunamazsa ilk YouTube videosunu gösterelim
                    for (TmdbVideoResponse.TmdbVideo video : response.body().getResults()) {
                        if ("YouTube".equals(video.getSite())) {
                            trailerVideoId.setValue(video.getKey());
                            return;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<TmdbVideoResponse> call, Throwable t) {
                // Hata olursa sessizce yok sayabiliriz, fragman alanı gizli kalır
            }
        });
    }

    private void loadSimilarMovies(String genre) {
        movieRepository.browseByCategory(genre, "movie").enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null 
                        && "True".equals(response.body().getResponse())) {
                    similarMovies.setValue(response.body().getMovies());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                // Ignore API failures for similar movies
            }
        });
    }

    private void loadCredits(String imdbId) {
        movieRepository.getMovieCredits(imdbId).enqueue(new Callback<TmdbCreditsResponse>() {
            @Override
            public void onResponse(Call<TmdbCreditsResponse> call, Response<TmdbCreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieCast.setValue(response.body().getCast());
                }
            }

            @Override
            public void onFailure(Call<TmdbCreditsResponse> call, Throwable t) {
                // Ignore API failures
            }
        });
    }

    private void checkIsFavorite(String imdbId) {
        favoriteRepository.isFavorite(imdbId, result -> {
            isFavorite.postValue(result);
        });
    }

    public void toggleFavorite(MovieDetail movie) {
        favoriteRepository.isFavorite(movie.getImdbId(), isFav -> {
            FavoriteMovie favoriteMovie = new FavoriteMovie(
                    movie.getImdbId(),
                    movie.getTitle(),
                    movie.getYear(),
                    movie.getPoster(),
                    movie.getGenre(),
                    movie.getImdbRating()
            );
            if (isFav) {
                favoriteRepository.delete(favoriteMovie);
                isFavorite.postValue(false);
            } else {
                favoriteRepository.insert(favoriteMovie);
                isFavorite.postValue(true);
            }
        });
    }

    public LiveData<MovieDetail> getMovieDetail() { return movieDetail; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsFavorite() { return isFavorite; }
    public LiveData<String> getTrailerVideoId() { return trailerVideoId; }
    public LiveData<List<TmdbCreditsResponse.TmdbCast>> getMovieCast() { return movieCast; }
    public LiveData<List<Movie>> getSimilarMovies() { return similarMovies; }
    public LiveData<String> getError() { return error; }
}