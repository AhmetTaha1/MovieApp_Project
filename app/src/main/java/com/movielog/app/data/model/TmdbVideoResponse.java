package com.movielog.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TmdbVideoResponse {
    @SerializedName("results")
    private List<TmdbVideo> results;

    public List<TmdbVideo> getResults() { return results; }

    public static class TmdbVideo {
        @SerializedName("key")
        private String key; // YouTube Video ID'si budur
        @SerializedName("site")
        private String site;
        @SerializedName("type")
        private String type; // "Trailer", "Teaser", vs.

        public String getKey() { return key; }
        public String getSite() { return site; }
        public String getType() { return type; }
    }
}