package com.movielog.app.ui.browse;

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
import com.movielog.app.databinding.FragmentBrowseCategoryBinding;
import com.movielog.app.ui.adapter.MovieAdapter;

public class BrowseCategoryFragment extends Fragment {

    private FragmentBrowseCategoryBinding binding;
    private BrowseCategoryViewModel viewModel;
    private MovieAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBrowseCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String category = getArguments() != null
                ? getArguments().getString("category", "Movies")
                : "Movies";

        // Toolbar
        binding.toolbar.setTitle(category);
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());

        // RecyclerView
        adapter = new MovieAdapter(movie -> navigateToDetail(movie));
        binding.rvBrowse.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvBrowse.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(BrowseCategoryViewModel.class);
        viewModel.movies.observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && !movies.isEmpty()) {
                adapter.setMovies(movies);
                binding.tvEmpty.setVisibility(View.GONE);
                binding.rvBrowse.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.rvBrowse.setVisibility(View.GONE);
            }
        });
        viewModel.isLoading.observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));
        viewModel.error.observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                binding.tvEmpty.setText(err);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Type filter chips
        View[] chips = {binding.chipTypeAll, binding.chipTypeMovie, binding.chipTypeSeries};
        selectTypeChip(binding.chipTypeAll, chips);

        binding.chipTypeAll.setOnClickListener(v -> {
            selectTypeChip(binding.chipTypeAll, chips);
            viewModel.changeType(null);
        });
        binding.chipTypeMovie.setOnClickListener(v -> {
            selectTypeChip(binding.chipTypeMovie, chips);
            viewModel.changeType("movie");
        });
        binding.chipTypeSeries.setOnClickListener(v -> {
            selectTypeChip(binding.chipTypeSeries, chips);
            viewModel.changeType("series");
        });

        // İlk yükleme
        viewModel.load(category, null);
    }

    private void selectTypeChip(View selected, View[] all) {
        for (View chip : all) {
            boolean isSel = chip == selected;
            chip.setBackgroundResource(isSel
                    ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            if (chip instanceof android.widget.TextView) {
                android.widget.TextView tv = (android.widget.TextView) chip;
                tv.setTextColor(requireContext().getColor(
                        isSel ? R.color.chip_selected_text : R.color.home_text_primary));
                tv.setTypeface(null, isSel
                        ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            }
        }
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
        binding = null;
    }
}
