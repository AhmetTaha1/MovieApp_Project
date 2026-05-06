package com.movielog.app.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.movielog.app.R;
import com.movielog.app.databinding.FragmentDetailBinding;
import com.movielog.app.ui.adapter.CastAdapter;
import com.movielog.app.ui.adapter.HorizontalMovieAdapter;
import java.util.Arrays;
import java.util.List;

public class DetailFragment extends Fragment {

    private FragmentDetailBinding binding;
    private DetailViewModel viewModel;
    private String movieId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // movieId'yi al
        if (getArguments() != null) {
            movieId = getArguments().getString("movieId");
        }

        setupViewModel();
        setupButtons();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        if (movieId != null) {
            viewModel.loadMovieDetail(movieId);
        }

        viewModel.getMovieDetail().observe(getViewLifecycleOwner(), movie -> {
            if (movie != null) {
                // Film bilgilerini göster
                binding.tvTitle.setText(movie.getTitle());
                binding.tvRating.setText("⭐ " + movie.getImdbRating());
                binding.tvYear.setText("📅 " + movie.getYear());
                binding.tvDirector.setText("🎬 " + movie.getDirector());
                binding.tvSummary.setText(movie.getPlot());

                // Türleri Chip olarak ekle
                binding.cgGenres.removeAllViews();
                if (movie.getGenre() != null) {
                    for (String genre : movie.getGenre().split(",")) {
                        Chip chip = new Chip(requireContext());
                        chip.setText(genre.trim());
                        chip.setCheckable(false);
                        binding.cgGenres.addView(chip);
                    }
                }

                // Posteri yükle
                Glide.with(requireContext())
                        .load(movie.getPoster())
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(binding.ivPoster);
            }
        });

        // Fragman işlemleri (Oynatıcı kalktı, sadece görsel var)
        viewModel.getTrailerVideoId().observe(getViewLifecycleOwner(), videoId -> {
            if (videoId != null && !videoId.isEmpty()) {
                binding.flTrailerContainer.setVisibility(View.VISIBLE);
                
                // YouTube kapak fotoğrafını çek (hqdefault en mantıklısıdır)
                String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                Glide.with(requireContext())
                        .load(thumbnailUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(binding.ivTrailerThumb);

                // Ortadaki play ikonuna veya görsele tıklandığında YouTube'a gitsin
                binding.flTrailerContainer.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
                    startActivity(intent);
                });
            }
        });

        // TMDB Üzerinden Oyuncuları (Credits) observe et
        viewModel.getMovieCast().observe(getViewLifecycleOwner(), castList -> {
            if (castList != null && !castList.isEmpty()) {
                CastAdapter castAdapter = new CastAdapter(castList);
                binding.rvCast.setAdapter(castAdapter);
            }
        });

        // Benzer Yapımlar (Önerilenler) Listesi
        viewModel.getSimilarMovies().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null && !movies.isEmpty()) {
                binding.tvSimilarTitle.setVisibility(View.VISIBLE);
                binding.rvSimilarMovies.setVisibility(View.VISIBLE);

                HorizontalMovieAdapter adapter = new HorizontalMovieAdapter(selectedMovie -> {
                    // Benzer filme tıklandığında Detay Sayfasını baştan yükle
                    Bundle bundle = new Bundle();
                    bundle.putString("movieId", selectedMovie.getImdbId());
                    Navigation.findNavController(requireView()).navigate(R.id.detailFragment, bundle);
                });
                adapter.setMovies(movies);
                binding.rvSimilarMovies.setAdapter(adapter);
            }
        });

        // Favori durumunu güncelle
        viewModel.getIsFavorite().observe(getViewLifecycleOwner(), isFavorite -> {
            if (isFavorite) {
                binding.fabFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                binding.fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.ivPoster.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });
    }

    private void setupButtons() {
        // Geri butonu
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).popBackStack();
        });

        // Favori butonu
        binding.fabFavorite.setOnClickListener(v -> {
            if (viewModel.getMovieDetail().getValue() != null) {
                viewModel.toggleFavorite(viewModel.getMovieDetail().getValue());
                Snackbar.make(requireView(),
                        "Favorites updated!",
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}