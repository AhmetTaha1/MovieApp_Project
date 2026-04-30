package com.movielog.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.movielog.app.data.model.FavoriteMovie;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteMovie movie);

    @Delete
    void delete(FavoriteMovie movie);

    @Query("SELECT * FROM favorites")
    LiveData<List<FavoriteMovie>> getAllFavorites();

    @Query("SELECT * FROM favorites WHERE imdbId = :imdbId")
    FavoriteMovie getFavoriteById(String imdbId);

    @Query("SELECT COUNT(*) FROM favorites WHERE imdbId = :imdbId")
    int isFavorite(String imdbId);
}