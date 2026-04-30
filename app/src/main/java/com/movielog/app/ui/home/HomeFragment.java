package com.movielog.app.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.movielog.app.R;
import com.movielog.app.data.model.Movie;
import com.movielog.app.databinding.FragmentHomeBinding;
import com.movielog.app.ui.adapter.FeaturedAdapter;
import com.movielog.app.ui.adapter.HorizontalMovieAdapter;
import com.movielog.app.ui.favorites.FavoritesViewModel;
import com.movielog.app.ui.recommendations.RecommendationsViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private RecommendationsViewModel recommendationsViewModel;
    private FavoritesViewModel favoritesViewModel;

    private FeaturedAdapter featuredAdapter;
    private HorizontalMovieAdapter topPicksAdapter;
    private HorizontalMovieAdapter popularShowsAdapter;

    private View[] dots;
    private int currentDot = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupHeader();
        setupSearchBar();
        setupFilterChips();
        setupFeaturedCarousel();
        setupTopPicks();
        setupPopularShows();
        setupViewModels();
        setupSeeAllButtons();
    }

    private void setupHeader() {
        // Root view'dan insets al — child view'lara dispatch garantili değil
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            float density = getResources().getDisplayMetrics().density;
            int pad20 = (int) (20 * density);
            int pad12 = (int) (12 * density);
            binding.headerContainer.setPadding(pad20, statusBarHeight + pad12, pad20, pad12);
            return insets;
        });

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("movielog_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        // Sadece "Welcome, [isim]" — "Welcome back" kaldırıldı
        binding.tvWelcome.setText("Welcome, " + username + " 👋");
        if (!username.isEmpty()) {
            binding.tvProfileAvatar.setText(
                    String.valueOf(username.charAt(0)).toUpperCase());
        }

        binding.tvProfileAvatar.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.profileFragment));
    }

    private void setupSearchBar() {
        binding.searchBarContainer.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.searchFragment));
    }

    private void setupFilterChips() {
        View[] chips = {
                binding.chipAll, binding.chipTvShows,
                binding.chipMovies, binding.chipCartoon
        };
        selectChip(binding.chipAll, chips);

        binding.chipAll.setOnClickListener(v -> {
            selectChip(binding.chipAll, chips);
            viewModel.loadByFilter("all");
        });
        binding.chipTvShows.setOnClickListener(v -> {
            selectChip(binding.chipTvShows, chips);
            viewModel.loadByFilter("tv");
        });
        binding.chipMovies.setOnClickListener(v -> {
            selectChip(binding.chipMovies, chips);
            viewModel.loadByFilter("movie");
        });
        binding.chipCartoon.setOnClickListener(v -> {
            selectChip(binding.chipCartoon, chips);
            viewModel.loadByFilter("cartoon");
        });
    }

    private void selectChip(View selected, View[] allChips) {
        for (View chip : allChips) {
            boolean isSelected = chip == selected;
            chip.setBackgroundResource(isSelected
                    ? R.drawable.bg_chip_selected
                    : R.drawable.bg_chip_unselected);
            if (chip instanceof android.widget.TextView) {
                android.widget.TextView tv = (android.widget.TextView) chip;
                tv.setTextColor(requireContext().getColor(
                        isSelected ? R.color.chip_selected_text : R.color.home_text_primary));
                tv.setTypeface(null, isSelected
                        ? android.graphics.Typeface.BOLD
                        : android.graphics.Typeface.NORMAL);
            }
        }
    }

    private void setupFeaturedCarousel() {
        featuredAdapter = new FeaturedAdapter(this::navigateToDetail);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvFeatured.setLayoutManager(layoutManager);
        binding.rvFeatured.setAdapter(featuredAdapter);
        new LinearSnapHelper().attachToRecyclerView(binding.rvFeatured);

        dots = new View[]{
                binding.dot0, binding.dot1, binding.dot2,
                binding.dot3, binding.dot4
        };

        binding.rvFeatured.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int first = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (first != RecyclerView.NO_POSITION && first != currentDot) {
                    updateDots(Math.min(first, dots.length - 1));
                }
            }
        });
    }

    private void updateDots(int selected) {
        currentDot = selected;
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(
                    i == selected ? R.drawable.bg_dot_selected : R.drawable.bg_dot_unselected);
        }
    }

    private void setupTopPicks() {
        topPicksAdapter = new HorizontalMovieAdapter(this::navigateToDetail);
        binding.rvTopPicks.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTopPicks.setAdapter(topPicksAdapter);
    }

    private void setupPopularShows() {
        popularShowsAdapter = new HorizontalMovieAdapter(this::navigateToDetail);
        binding.rvPopularShows.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPopularShows.setAdapter(popularShowsAdapter);
    }

    private void setupViewModels() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        // Activity scope'ta paylaşılan ViewModels
        favoritesViewModel = new ViewModelProvider(requireActivity()).get(FavoritesViewModel.class);
        recommendationsViewModel = new ViewModelProvider(requireActivity()).get(RecommendationsViewModel.class);

        // Featured carousel
        viewModel.getFeaturedMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null) {
                featuredAdapter.setMovies(movies);
                updateDots(0);
            }
        });

        // Popular shows (her zaman OMDB'den)
        viewModel.getPopularShowsMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null) popularShowsAdapter.setMovies(movies);
        });

        // TOP PICKS: Önce OMDB yükle (her zaman içerik göster)
        viewModel.getTopPicksMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && topPicksAdapter.getItemCount() == 0) {
                topPicksAdapter.setMovies(movies);
            }
        });

        // Favoriler varsa Gemini'yi tetikle
        favoritesViewModel.getAllFavorites().observe(getViewLifecycleOwner(), favorites -> {
            if (favorites != null && !favorites.isEmpty()) {
                recommendationsViewModel.loadIfNotLoaded(favorites);
            }
        });

        // AI önerileri gelince OMDB'nin üzerine yaz
        recommendationsViewModel.getRecommendations().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && !movies.isEmpty()) {
                topPicksAdapter.setMovies(movies);
            }
        });

        // AI hata alırsa sessizce OMDB fallback'te kalır (zaten yüklü)

        // Loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupSeeAllButtons() {
        binding.tvSeeAllTopPicks.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.recommendationsFragment));
        binding.tvSeeAllPopular.setOnClickListener(v ->
                navigateToCategory("Popular"));
        binding.tvSeeAllCategories.setOnClickListener(v ->
                navigateToCategory("Action"));

        // Kategori chip'leri
        binding.catAction.setOnClickListener(v -> navigateToCategory("Action"));
        binding.catAdventure.setOnClickListener(v -> navigateToCategory("Adventure"));
        binding.catComedy.setOnClickListener(v -> navigateToCategory("Comedy"));
        binding.catDrama.setOnClickListener(v -> navigateToCategory("Drama"));
        binding.catHorror.setOnClickListener(v -> navigateToCategory("Horror"));
        binding.catSciFi.setOnClickListener(v -> navigateToCategory("Sci-Fi"));
    }

    private void navigateToCategory(String category) {
        Bundle args = new Bundle();
        args.putString("category", category);
        Navigation.findNavController(requireView())
                .navigate(R.id.browseCategoryFragment, args);
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
