package com.movielog.app.data.repository;

import android.app.Application;
import com.movielog.app.BuildConfig;
import com.movielog.app.data.model.MovieDetail;
import com.movielog.app.data.model.SearchResponse;
import com.movielog.app.data.remote.OmdbApiService;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieRepository {

    private static final String BASE_URL = "https://www.omdbapi.com/";
    private OmdbApiService apiService;

    public MovieRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(OmdbApiService.class);
    }

    public Call<SearchResponse> searchMovies(String query) {
        return apiService.searchMovies(query, "movie", BuildConfig.OMDB_API_KEY);
    }

    /** type: "movie" | "series" | null (hepsi) */
    public Call<SearchResponse> browseByCategory(String query, String type) {
        return apiService.searchMoviesFlexible(query, type, BuildConfig.OMDB_API_KEY);
    }

    public Call<MovieDetail> getMovieDetail(String imdbId) {
        return apiService.getMovieDetail(imdbId, "full", BuildConfig.OMDB_API_KEY);
    }
}