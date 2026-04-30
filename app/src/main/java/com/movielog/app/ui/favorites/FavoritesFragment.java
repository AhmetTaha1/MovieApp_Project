package com.movielog.app.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.movielog.app.R;
import com.movielog.app.data.model.FavoriteMovie;
import com.movielog.app.databinding.FragmentFavoritesBinding;
import com.movielog.app.ui.adapter.FavoriteAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private FavoritesViewModel viewModel;
    private FavoriteAdapter adapter;

    // Sıralama seçeneği: 0=Varsayılan, 1=A-Z, 2=Z-A
    private int currentSortMode = 0;
    private List<FavoriteMovie> latestFavorites = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupViewModel();
        setupSortButton();
    }

    private void setupRecyclerView() {
        adapter = new FavoriteAdapter(new FavoriteAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(FavoriteMovie movie) {
                navigateToDetail(movie);
            }

            @Override
            public void onFavoriteDelete(FavoriteMovie movie) {
                viewModel.delete(movie);
                Snackbar.make(requireView(),
                        movie.getTitle() + " removed from favorites",
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        binding.rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvFavorites.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);

        viewModel.getAllFavorites().observe(getViewLifecycleOwner(), favorites -> {
            latestFavorites = favorites != null ? new ArrayList<>(favorites) : new ArrayList<>();
            showSorted();
        });
    }

    private void setupSortButton() {
        binding.btnSort.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 0, 0, getString(R.string.sort_default));
            popup.getMenu().add(0, 1, 1, getString(R.string.sort_az));
            popup.getMenu().add(0, 2, 2, getString(R.string.sort_za));

            popup.setOnMenuItemClickListener(item -> {
                currentSortMode = item.getItemId();
                showSorted();
                return true;
            });
            popup.show();
        });
    }

    private void showSorted() {
        if (latestFavorites.isEmpty()) {
            binding.tvNoFavorites.startAnimation(
                    AnimationUtils.loadAnimation(getContext(), R.anim.fade_slide_up));
            binding.tvNoFavorites.setVisibility(View.VISIBLE);
            binding.rvFavorites.setVisibility(View.GONE);
            return;
        }

        List<FavoriteMovie> sorted = new ArrayList<>(latestFavorites);
        if (currentSortMode == 1) {
            Collections.sort(sorted, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        } else if (currentSortMode == 2) {
            Collections.sort(sorted, (a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle()));
        }

        adapter.setFavorites(sorted);
        binding.tvNoFavorites.setVisibility(View.GONE);
        binding.rvFavorites.setVisibility(View.VISIBLE);
    }

    private void navigateToDetail(FavoriteMovie movie) {
        Bundle args = new Bundle();
        args.putString("movieId", movie.getImdbId());
        Navigation.findNavController(requireView())
                .navigate(R.id.detailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
