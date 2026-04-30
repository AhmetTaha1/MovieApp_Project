package com.movielog.app.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.movielog.app.AdminActivity;
import com.movielog.app.LoginActivity;
import com.movielog.app.R;
import com.movielog.app.data.model.FavoriteMovie;
import com.movielog.app.databinding.FragmentProfileBinding;
import com.movielog.app.ui.adapter.ProfileFavoriteAdapter;
import com.movielog.app.util.ThemeHelper;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private ProfileFavoriteAdapter favoritesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupHeader();
        setupFavoritesRecyclerView();
        setupViewModel();
        setupButtons();
        setupSettings();
    }

    private void setupHeader() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("movielog_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        int userId = prefs.getInt("user_id", -1);

        binding.tvUsername.setText(username);
        if (username.length() > 0) {
            binding.tvAvatar.setText(String.valueOf(username.charAt(0)).toUpperCase());
        }

        // Email'i Room'dan yükle
        if (userId != -1) {
            viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
            viewModel.loadUser(userId);
            viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null && user.getEmail() != null) {
                    binding.tvEmail.setText(user.getEmail());
                }
            });
        }
    }

    private void setupFavoritesRecyclerView() {
        favoritesAdapter = new ProfileFavoriteAdapter(movie -> navigateToDetail(movie));
        binding.rvProfileFavorites.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvProfileFavorites.setAdapter(favoritesAdapter);
    }

    private void setupViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        }

        viewModel.getFavorites().observe(getViewLifecycleOwner(), favorites -> {
            if (favorites != null && !favorites.isEmpty()) {
                binding.rvProfileFavorites.setVisibility(View.VISIBLE);
                binding.llEmptyFavorites.setVisibility(View.GONE);
                favoritesAdapter.setFavorites(favorites);

                binding.tvFavoritesCount.setText(String.valueOf(favorites.size()));

                String topGenre = findTopGenre(favorites);
                binding.tvTopGenre.setText(topGenre.isEmpty() ? "—" : topGenre);
            } else {
                binding.rvProfileFavorites.setVisibility(View.GONE);
                binding.llEmptyFavorites.setVisibility(View.VISIBLE);
                binding.tvFavoritesCount.setText("0");
                binding.tvTopGenre.setText("—");
            }
        });
    }

    private void setupSettings() {
        // ── Tema switch ──────────────────────────────────────────────────────
        String currentTheme = ThemeHelper.getSavedTheme(requireContext());
        binding.switchDarkMode.setChecked(ThemeHelper.THEME_DARK.equals(currentTheme));

        binding.switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            String newTheme = isChecked ? ThemeHelper.THEME_DARK : ThemeHelper.THEME_LIGHT;
            ThemeHelper.saveAndApplyTheme(requireContext(), newTheme);
            requireActivity().recreate();
        });

        // ── Dil butonları ────────────────────────────────────────────────────
        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
        boolean isTurkish = !appLocales.isEmpty()
                && "tr".equals(appLocales.get(0).getLanguage());

        updateLanguageButtons(isTurkish);

        binding.btnLangEn.setOnClickListener(v -> {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
            updateLanguageButtons(false);
        });

        binding.btnLangTr.setOnClickListener(v -> {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr"));
            updateLanguageButtons(true);
        });

        // ── Admin Panel ──────────────────────────────────────────────────────
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("movielog_prefs", Context.MODE_PRIVATE);
        boolean isAdmin = prefs.getBoolean("is_admin", false);

        if (isAdmin) {
            binding.btnAdminPanel.setVisibility(View.VISIBLE);
            binding.btnAdminPanel.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), AdminActivity.class)));
        }
    }

    /** Seçili dil butonunu kırmızı, diğerini outline olarak gösterir. */
    private void updateLanguageButtons(boolean isTurkish) {
        if (isTurkish) {
            binding.btnLangTr.setBackgroundColor(requireContext().getColor(R.color.red_primary));
            binding.btnLangEn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        } else {
            binding.btnLangEn.setBackgroundColor(requireContext().getColor(R.color.red_primary));
            binding.btnLangTr.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
    }

    private String findTopGenre(List<FavoriteMovie> favorites) {
        java.util.Map<String, Integer> genreCount = new java.util.HashMap<>();
        for (FavoriteMovie movie : favorites) {
            if (movie.getGenre() != null && !movie.getGenre().isEmpty()) {
                String[] genres = movie.getGenre().split(",");
                if (genres.length > 0) {
                    String primary = genres[0].trim();
                    genreCount.put(primary, genreCount.getOrDefault(primary, 0) + 1);
                }
            }
        }
        String topGenre = "";
        int max = 0;
        for (java.util.Map.Entry<String, Integer> entry : genreCount.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                topGenre = entry.getKey();
            }
        }
        return topGenre;
    }

    private void setupButtons() {
        binding.btnAiRecommendations.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_profile_to_recommendations));

        binding.btnSignOut.setOnClickListener(v -> signOut());

        binding.tvSeeAllFavorites.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.favoritesFragment));
    }

    private void signOut() {
        requireContext().getSharedPreferences("movielog_prefs", Context.MODE_PRIVATE)
                .edit().clear().apply();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
