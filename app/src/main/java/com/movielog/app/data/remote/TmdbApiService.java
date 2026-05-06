package com.movielog.app.data.remote;

import com.movielog.app.data.model.TmdbCreditsResponse;
import com.movielog.app.data.model.TmdbVideoResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmdbApiService {
    // IMDB ID (örn: tt1375666) ile film bilgisini arayıp videolarını çeker
    @GET("movie/{movie_id}/videos")
    Call<TmdbVideoResponse> getMovieVideos(
            @Path("movie_id") String imdbId,
            @Query("api_key") String apiKey
    );

    // IMDB ID ile oyuncu/ekip kadrosunu çeker
    @GET("movie/{movie_id}/credits")
    Call<TmdbCreditsResponse> getMovieCredits(
            @Path("movie_id") String imdbId,
            @Query("api_key") String apiKey
    );
}