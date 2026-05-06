package com.movielog.app.data.repository;

import android.app.Application;
import com.movielog.app.BuildConfig;
import com.movielog.app.data.model.MovieDetail;
import com.movielog.app.data.model.SearchResponse;
import com.movielog.app.data.model.TmdbCreditsResponse;
import com.movielog.app.data.model.TmdbVideoResponse;
import com.movielog.app.data.remote.OmdbApiService;
import com.movielog.app.data.remote.TmdbApiService;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieRepository {

    private static final String BASE_URL = "https://www.omdbapi.com/";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    
    private OmdbApiService apiService;
    private TmdbApiService tmdbApiService;

    public MovieRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(OmdbApiService.class);

        Retrofit tmdbRetrofit = new Retrofit.Builder()
                .baseUrl(TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        tmdbApiService = tmdbRetrofit.create(TmdbApiService.class);
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

    public Call<TmdbVideoResponse> getMovieTrailer(String imdbId) {
        return tmdbApiService.getMovieVideos(imdbId, BuildConfig.TMDB_API_KEY);
    }

    public Call<TmdbCreditsResponse> getMovieCredits(String imdbId) {
        return tmdbApiService.getMovieCredits(imdbId, BuildConfig.TMDB_API_KEY);
    }
}