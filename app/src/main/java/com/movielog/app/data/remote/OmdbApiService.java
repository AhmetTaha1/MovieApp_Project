package com.movielog.app.data.remote;

import com.movielog.app.data.model.MovieDetail;
import com.movielog.app.data.model.SearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OmdbApiService {

    // Film ara (isim, tür, oyuncu) - type zorunlu
    @GET("/")
    Call<SearchResponse> searchMovies(
            @Query("s") String searchQuery,
            @Query("type") String type,
            @Query("apikey") String apiKey
    );

    // Film ara - type opsiyonel (null gönderilebilir)
    @GET("/")
    Call<SearchResponse> searchMoviesFlexible(
            @Query("s") String searchQuery,
            @Query("type") String type,
            @Query("apikey") String apiKey
    );

    // Film detayı getir (imdbID ile)
    @GET("/")
    Call<MovieDetail> getMovieDetail(
            @Query("i") String imdbId,
            @Query("plot") String plot,
            @Query("apikey") String apiKey
    );
}