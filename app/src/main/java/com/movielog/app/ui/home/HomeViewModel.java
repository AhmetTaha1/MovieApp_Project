package com.movielog.app.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.movielog.app.data.model.Movie;
import com.movielog.app.data.model.SearchResponse;
import com.movielog.app.data.repository.MovieRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private final MovieRepository repository;

    private final MutableLiveData<List<Movie>> featuredMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> topPicksMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> popularShowsMovies = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public HomeViewModel() {
        repository = new MovieRepository();
        loadAllSections();
    }

    public void loadAllSections() {
        isLoading.setValue(true);
        loadFeatured("action");
        loadTopPicks("drama series");
        loadPopularShows("crime");
    }

    public void loadByFilter(String filter) {
        isLoading.setValue(true);
        switch (filter) {
            case "tv":
                loadFeatured("series");
                loadTopPicks("tv show");
                loadPopularShows("drama series");
                break;
            case "movie":
                loadFeatured("thriller");
                loadTopPicks("romance");
                loadPopularShows("comedy");
                break;
            case "cartoon":
                loadFeatured("animated");
                loadTopPicks("cartoon");
                loadPopularShows("animation");
                break;
            default:
                loadFeatured("action");
                loadTopPicks("drama series");
                loadPopularShows("crime");
                break;
        }
    }

    private void loadFeatured(String query) {
        repository.searchMovies(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null
                        && "True".equals(response.body().getResponse())) {
                    featuredMovies.setValue(response.body().getMovies());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }

    private void loadTopPicks(String query) {
        repository.searchMovies(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && "True".equals(response.body().getResponse())) {
                    topPicksMovies.setValue(response.body().getMovies());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
    }

    private void loadPopularShows(String query) {
        repository.searchMovies(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && "True".equals(response.body().getResponse())) {
                    popularShowsMovies.setValue(response.body().getMovies());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
    }

    public LiveData<List<Movie>> getFeaturedMovies() { return featuredMovies; }
    public LiveData<List<Movie>> getTopPicksMovies() { return topPicksMovies; }
    public LiveData<List<Movie>> getPopularShowsMovies() { return popularShowsMovies; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
}
