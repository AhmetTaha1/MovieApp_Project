package com.movielog.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.movielog.app.data.local.AppDatabase;
import com.movielog.app.data.local.FavoriteDao;
import com.movielog.app.data.model.FavoriteMovie;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteRepository {

    private FavoriteDao favoriteDao;
    private LiveData<List<FavoriteMovie>> allFavorites;
    private ExecutorService executor;

    public FavoriteRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        favoriteDao = db.favoriteDao();
        allFavorites = favoriteDao.getAllFavorites();
        executor = Executors.newSingleThreadExecutor();
    }

    // Tüm favorileri getir
    public LiveData<List<FavoriteMovie>> getAllFavorites() {
        return allFavorites;
    }

    // Favori ekle
    public void insert(FavoriteMovie movie) {
        executor.execute(() -> favoriteDao.insert(movie));
    }

    // Favori sil
    public void delete(FavoriteMovie movie) {
        executor.execute(() -> favoriteDao.delete(movie));
    }

    // Favori mi kontrol et
    public void isFavorite(String imdbId, FavoriteCallback callback) {
        executor.execute(() -> {
            int count = favoriteDao.isFavorite(imdbId);
            callback.onResult(count > 0);
        });
    }

    // Callback interface
    public interface FavoriteCallback {
        void onResult(boolean isFavorite);
    }
}