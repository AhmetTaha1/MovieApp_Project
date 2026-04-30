package com.movielog.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResponse {

    @SerializedName("Search")
    private List<Movie> movies;

    @SerializedName("totalResults")
    private String totalResults;

    @SerializedName("Response")
    private String response;

    @SerializedName("Error")
    private String error;

    // Getters ve Setters
    public List<Movie> getMovies() { return movies; }
    public void setMovies(List<Movie> movies) { this.movies = movies; }

    public String getTotalResults() { return totalResults; }
    public void setTotalResults(String totalResults) { this.totalResults = totalResults; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}