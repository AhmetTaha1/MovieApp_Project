package com.movielog.app;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.movielog.app.databinding.ActivityMainBinding;
import com.movielog.app.util.ThemeHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        navController = navHostFragment.getNavController();


        NavOptions tabNavOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .setLaunchSingleTop(true)
                .build();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int destId = item.getItemId();
            // Zaten bu tab'daysa tekrar navigate etme
            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == destId) {
                return true;
            }
            navController.navigate(destId, null, tabNavOptions);
            return true;
        });

        // NavController değişikliklerini bottom nav'a yansıt
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.detailFragment || id == R.id.recommendationsFragment
                    || id == R.id.browseCategoryFragment) {
                binding.bottomNavigation.setVisibility(View.GONE);
            } else {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
                // Aktif tab'ı vurgula
                int menuId = id;
                if (binding.bottomNavigation.getSelectedItemId() != menuId) {
                    binding.bottomNavigation.setSelectedItemId(menuId);
                }
            }
        });
    }
}