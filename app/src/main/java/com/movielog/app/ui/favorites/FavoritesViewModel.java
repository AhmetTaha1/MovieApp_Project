package com.movielog.app.ui.favorites;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.movielog.app.data.model.FavoriteMovie;
import com.movielog.app.data.repository.FavoriteRepository;
import java.util.List;

public class FavoritesViewModel extends AndroidViewModel {

    private FavoriteRepository repository;
    private LiveData<List<FavoriteMovie>> allFavorites;

    public FavoritesViewModel(Application application) {
        super(application);
        repository = new FavoriteRepository(application);
        allFavorites = repository.getAllFavorites();
    }

    public LiveData<List<FavoriteMovie>> getAllFavorites() {
        return allFavorites;
    }

    public void delete(FavoriteMovie movie) {
        repository.delete(movie);
    }
}