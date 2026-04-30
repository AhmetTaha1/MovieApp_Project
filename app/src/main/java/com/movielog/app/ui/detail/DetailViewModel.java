package com.movielog.app.ui.detail;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.movielog.app.data.model.FavoriteMovie;
import com.movielog.app.data.model.MovieDetail;
import com.movielog.app.data.repository.FavoriteRepository;
import com.movielog.app.data.repository.MovieRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailViewModel extends AndroidViewModel {

    private MovieRepository movieRepository;
    private FavoriteRepository favoriteRepository;
    private MutableLiveData<MovieDetail> movieDetail = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isFavorite = new MutableLiveData<>();
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
                    movieDetail.setValue(response.body());
                    checkIsFavorite(imdbId);
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
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
    public LiveData<String> getError() { return error; }
}