package com.movielog.app.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.movielog.app.R;
import com.movielog.app.data.model.Movie;
import com.movielog.app.databinding.FragmentSearchBinding;
import com.movielog.app.ui.adapter.MovieAdapter;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private MovieAdapter adapter;

    // Debounce: kullanıcı yazmayı bıraktıktan 450ms sonra ara
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private static final long DEBOUNCE_MS = 450;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupViewModel();
        setupSearch();
    }

    private void setupRecyclerView() {
        adapter = new MovieAdapter(movie -> navigateToDetail(movie));
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvSearchResults.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && !movies.isEmpty()) {
                adapter.setMovies(movies);
                binding.tvNoResults.setVisibility(View.GONE);
                binding.rvSearchResults.setVisibility(View.VISIBLE);
            } else {
                binding.tvNoResults.setVisibility(View.VISIBLE);
                binding.rvSearchResults.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Önceki bekleyen aramayı iptal et
                searchHandler.removeCallbacksAndMessages(null);

                String query = s.toString().trim();
                if (query.length() > 2) {
                    // 450ms sonra ara
                    searchHandler.postDelayed(() -> viewModel.searchMovies(query), DEBOUNCE_MS);
                } else if (query.isEmpty()) {
                    // Arama temizlendiyse sonuçları sıfırla
                    adapter.setMovies(null);
                    binding.tvNoResults.setVisibility(View.GONE);
                    binding.rvSearchResults.setVisibility(View.GONE);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void navigateToDetail(Movie movie) {
        Bundle args = new Bundle();
        args.putString("movieId", movie.getImdbId());
        Navigation.findNavController(requireView())
                .navigate(R.id.detailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchHandler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
