package com.movielog.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TmdbCreditsResponse {
    @SerializedName("cast")
    private List<TmdbCast> cast;

    public List<TmdbCast> getCast() { return cast; }

    public static class TmdbCast {
        @SerializedName("name")
        private String name;

        @SerializedName("character")
        private String character;

        @SerializedName("profile_path")
        private String profilePath;

        public String getName() { return name; }
        public String getCharacter() { return character; }
        public String getProfilePath() { return profilePath; }
    }
}