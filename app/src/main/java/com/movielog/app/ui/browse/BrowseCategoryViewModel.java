package com.movielog.app.ui.browse;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.movielog.app.data.model.Movie;
import com.movielog.app.data.model.SearchResponse;
import com.movielog.app.data.repository.MovieRepository;
import java.util.List;

public class BrowseCategoryViewModel extends AndroidViewModel {

    private final MovieRepository repository;
    public final MutableLiveData<List<Movie>> movies = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> error = new MutableLiveData<>();

    private String currentCategory = "";
    private String currentType = null; // null = hepsi

    public BrowseCategoryViewModel(Application application) {
        super(application);
        repository = new MovieRepository();
    }

    public void load(String category, String type) {
        currentCategory = category;
        currentType = type;
        fetch();
    }

    public void changeType(String type) {
        currentType = type;
        fetch();
    }

    private void fetch() {
        if (currentCategory.isEmpty()) return;
        isLoading.setValue(true);

        new Thread(() -> {
            try {
                retrofit2.Response<SearchResponse> response =
                        repository.browseByCategory(currentCategory, currentType).execute();
                if (response.isSuccessful() && response.body() != null
                        && response.body().getMovies() != null) {
                    movies.postValue(response.body().getMovies());
                    error.postValue(null);
                } else {
                    movies.postValue(null);
                    error.postValue("No results found for \"" + currentCategory + "\"");
                }
            } catch (Exception e) {
                error.postValue("Network error: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }
}
