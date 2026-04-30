package com.movielog.app.ui.recommendations;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.movielog.app.BuildConfig;
import com.movielog.app.data.model.FavoriteMovie;
import com.movielog.app.data.model.Movie;
import com.movielog.app.data.model.SearchResponse;
import com.movielog.app.data.repository.FavoriteRepository;
import com.movielog.app.data.repository.MovieRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecommendationsViewModel extends AndroidViewModel {

    private FavoriteRepository favoriteRepository;
    private MovieRepository movieRepository;
    private MutableLiveData<List<Movie>> recommendations = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> error = new MutableLiveData<>();
    private boolean isLoaded = false;

    public RecommendationsViewModel(Application application) {
        super(application);
        favoriteRepository = new FavoriteRepository(application);
        movieRepository = new MovieRepository();
    }

    /** Sadece daha önce yüklenmediyse yükler — HomeFragment ile RecommendationsFragment arası çakışmayı önler */
    public void loadIfNotLoaded(List<FavoriteMovie> favorites) {
        // Zaten yüklendiyse tekrar çağırma
        if (isLoaded && recommendations.getValue() != null && !recommendations.getValue().isEmpty()) {
            return;
        }
        // Hâlâ yükleniyorsa ikinci istek atma
        if (Boolean.TRUE.equals(isLoading.getValue())) {
            return;
        }
        getRecommendations(favorites);
    }

    /** Favori listesine göre Groq AI'den öneri alır ve OMDB'de arar */
    public void getRecommendations(List<FavoriteMovie> favorites) {
        if (favorites == null || favorites.isEmpty()) {
            error.setValue("no_favorites");
            return;
        }

        isLoading.setValue(true);
        isLoaded = false;

        StringBuilder favoriteTitles = new StringBuilder();
        for (FavoriteMovie movie : favorites) {
            favoriteTitles.append(movie.getTitle()).append(", ");
        }

        new Thread(() -> {
            try {
                // Groq'u sadece JSON dönmeye zorluyoruz
                String prompt = "I have watched and liked these movies/series: " + favoriteTitles +
                        ". Based on these, recommend exactly 6 similar movies or TV series that I would enjoy. " +
                        "You MUST return the result ONLY in JSON format as an object containing a single array named 'titles'. " +
                        "Example: {\"titles\": [\"Title 1\", \"Title 2\", \"Title 3\", \"Title 4\", \"Title 5\", \"Title 6\"]}";

                String groqResponse = callGroqApi(prompt);
                android.util.Log.d("GROQ_RAW", groqResponse);

                // Groq'tan error gelirse düzgün yakala
                try {
                    JSONObject responseObj = new JSONObject(groqResponse);
                    if (responseObj.has("error")) {
                        String errMsg = responseObj.getJSONObject("error").optString("message", "Unknown Groq error");
                        android.util.Log.e("GROQ_ERR", errMsg);
                        error.postValue("AI Error: " + errMsg);
                        isLoading.postValue(false);
                        return;
                    }
                } catch (Exception ignored) {}

                List<String> recommendedTitles = parseAIResponse(groqResponse);

                List<Movie> recommendedMovies = new ArrayList<>();
                for (String title : recommendedTitles) {
                    retrofit2.Response<SearchResponse> response =
                            movieRepository.searchMovies(title).execute();
                    if (response.isSuccessful() && response.body() != null
                            && response.body().getMovies() != null
                            && !response.body().getMovies().isEmpty()) {
                        recommendedMovies.add(response.body().getMovies().get(0));
                    }
                }

                if (!recommendedMovies.isEmpty()) {
                    isLoaded = true;
                    recommendations.postValue(recommendedMovies);
                } else {
                    error.postValue("No recommendations found. Try adding more favorites.");
                }
                isLoading.postValue(false);

            } catch (Exception e) {
                error.postValue("Recommendation error: " + e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }

    private String callGroqApi(String prompt) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.groq.com/openai/v1/chat/completions";

        try {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a movie recommendation assistant. You strictly output JSON.");

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(systemMessage);
            messages.put(userMessage);

            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "llama-3.1-8b-instant");
            requestBody.put("messages", messages);
            requestBody.put("response_format", responseFormat);
            requestBody.put("temperature", 0.7);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + BuildConfig.GROQ_API_KEY)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("Empty response from Groq API");
            }

        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /** Groq Formatını Parse Eder: choices[0].message.content içindeki 'titles' array'ini alır */
    private List<String> parseAIResponse(String response) {
        List<String> titles = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(response);
            String contentString = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            JSONObject contentJson = new JSONObject(contentString);
            JSONArray arr = contentJson.getJSONArray("titles");

            for (int i = 0; i < arr.length(); i++) {
                titles.add(arr.getString(i));
            }
        } catch (Exception e) {
            android.util.Log.e("PARSE_ERROR", "Error parsing Groq response", e);
        }
        return titles;
    }

    public LiveData<List<Movie>> getRecommendations() { return recommendations; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
}