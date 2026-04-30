package com.movielog.app.ui.recommendations;

import android.os.Bundle;
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
import com.movielog.app.databinding.FragmentRecommendationsBinding;
import com.movielog.app.ui.adapter.MovieAdapter;
import com.movielog.app.ui.favorites.FavoritesViewModel;

public class RecommendationsFragment extends Fragment {

    private FragmentRecommendationsBinding binding;
    private RecommendationsViewModel viewModel;
    private FavoritesViewModel favoritesViewModel;
    private MovieAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecommendationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupViewModels();
    }

    private void setupRecyclerView() {
        adapter = new MovieAdapter(movie -> {
            Bundle args = new Bundle();
            args.putString("movieId", movie.getImdbId());
            Navigation.findNavController(requireView()).navigate(R.id.detailFragment, args);
        });
        binding.rvRecommendations.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvRecommendations.setAdapter(adapter);
    }

    private void setupViewModels() {
        // Activity scope — HomeFragment ile paylaşılan aynı instance
        viewModel = new ViewModelProvider(requireActivity()).get(RecommendationsViewModel.class);
        favoritesViewModel = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);

        favoritesViewModel.getAllFavorites().observe(getViewLifecycleOwner(), favorites -> {
            if (favorites != null && !favorites.isEmpty()) {
                binding.tvNoRecommendations.setVisibility(View.GONE);
                // loadIfNotLoaded: daha önce yüklendiyse Gemini'ye tekrar istek atmaz
                viewModel.loadIfNotLoaded(favorites);
            } else {
                binding.tvNoRecommendations.setText(
                        "Add movies to favorites to get AI recommendations!");
                binding.tvNoRecommendations.setVisibility(View.VISIBLE);
                binding.rvRecommendations.setVisibility(View.GONE);
            }
        });

        viewModel.getRecommendations().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && !movies.isEmpty()) {
                adapter.setMovies(movies);
                binding.rvRecommendations.setVisibility(View.VISIBLE);
                binding.tvNoRecommendations.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    binding.tvNoRecommendations.setText("Finding the best movies for you…");
                    binding.tvNoRecommendations.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && !"no_favorites".equals(error)) {
                binding.tvNoRecommendations.setText(error);
                binding.tvNoRecommendations.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
