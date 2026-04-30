package com.movielog.app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "favorites")
public class FavoriteMovie {

    @PrimaryKey
    @NonNull
    private String imdbId;
    private String title;
    private String year;
    private String poster;
    private String genre;
    private String imdbRating;

    // Constructor
    public FavoriteMovie(@NonNull String imdbId, String title,
                         String year, String poster,
                         String genre, String imdbRating) {
        this.imdbId = imdbId;
        this.title = title;
        this.year = year;
        this.poster = poster;
        this.genre = genre;
        this.imdbRating = imdbRating;
    }

    // Getters ve Setters
    @NonNull
    public String getImdbId() { return imdbId; }
    public void setImdbId(@NonNull String imdbId) { this.imdbId = imdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }
}