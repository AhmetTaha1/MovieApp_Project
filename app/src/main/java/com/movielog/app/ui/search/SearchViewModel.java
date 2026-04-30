package com.movielog.app.ui.search;

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

public class SearchViewModel extends ViewModel {

    private MovieRepository repository;
    private MutableLiveData<List<Movie>> searchResults = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public SearchViewModel() {
        repository = new MovieRepository();
    }

    public void searchMovies(String query) {
        isLoading.setValue(true);
        repository.searchMovies(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    if ("True".equals(response.body().getResponse())) {
                        searchResults.setValue(response.body().getMovies());
                    } else {
                        error.setValue(response.body().getError());
                    }
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }

    public LiveData<List<Movie>> getSearchResults() { return searchResults; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
}