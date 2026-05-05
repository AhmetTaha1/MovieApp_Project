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
    private final MutableLiveData<List<Movie>> actionMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> comedyMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> dramaMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> horrorMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> sciFiMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> adventureMovies = new MutableLiveData<>();
    private final MutableLiveData<List<Movie>> romanceMovies = new MutableLiveData<>();
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
        loadCategory("action movie", actionMovies);
        loadCategory("comedy", comedyMovies);
        loadCategory("drama", dramaMovies);
        loadCategory("horror", horrorMovies);
        loadCategory("sci-fi", sciFiMovies);
        loadCategory("adventure", adventureMovies);
        loadCategory("romance", romanceMovies);
    }

    public void loadByFilter(String filter) {
        isLoading.setValue(true);
        switch (filter) {
            case "tv":
                loadFeatured("series");
                loadTopPicks("tv show");
                loadPopularShows("drama series");
                loadCategory("action series", actionMovies);
                loadCategory("comedy series", comedyMovies);
                loadCategory("drama series", dramaMovies);
                loadCategory("horror series", horrorMovies);
                loadCategory("sci-fi series", sciFiMovies);
                loadCategory("adventure series", adventureMovies);
                loadCategory("romance series", romanceMovies);
                break;
            case "movie":
                loadFeatured("thriller");
                loadTopPicks("romance");
                loadPopularShows("comedy");
                loadCategory("action movie", actionMovies);
                loadCategory("comedy movie", comedyMovies);
                loadCategory("drama movie", dramaMovies);
                loadCategory("horror movie", horrorMovies);
                loadCategory("sci-fi movie", sciFiMovies);
                loadCategory("adventure movie", adventureMovies);
                loadCategory("romance movie", romanceMovies);
                break;
            case "cartoon":
                loadFeatured("animated");
                loadTopPicks("cartoon");
                loadPopularShows("animation");
                loadCategory("action cartoon", actionMovies);
                loadCategory("comedy cartoon", comedyMovies);
                loadCategory("drama cartoon", dramaMovies);
                loadCategory("horror cartoon", horrorMovies);
                loadCategory("sci-fi cartoon", sciFiMovies);
                loadCategory("adventure cartoon", adventureMovies);
                loadCategory("romance cartoon", romanceMovies);
                break;
            default:
                loadFeatured("action");
                loadTopPicks("drama series");
                loadPopularShows("crime");
                loadCategory("action movie", actionMovies);
                loadCategory("comedy", comedyMovies);
                loadCategory("drama", dramaMovies);
                loadCategory("horror", horrorMovies);
                loadCategory("sci-fi", sciFiMovies);
                loadCategory("adventure", adventureMovies);
                loadCategory("romance", romanceMovies);
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

    private void loadCategory(String query, MutableLiveData<List<Movie>> targetLiveData) {
        repository.searchMovies(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && "True".equals(response.body().getResponse())) {
                    targetLiveData.setValue(response.body().getMovies());
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
    public LiveData<List<Movie>> getActionMovies() { return actionMovies; }
    public LiveData<List<Movie>> getComedyMovies() { return comedyMovies; }
    public LiveData<List<Movie>> getDramaMovies() { return dramaMovies; }
    public LiveData<List<Movie>> getHorrorMovies() { return horrorMovies; }
    public LiveData<List<Movie>> getSciFiMovies() { return sciFiMovies; }
    public LiveData<List<Movie>> getAdventureMovies() { return adventureMovies; }
    public LiveData<List<Movie>> getRomanceMovies() { return romanceMovies; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
}
