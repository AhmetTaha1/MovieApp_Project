package com.movielog.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.movielog.app.data.model.User;
import com.movielog.app.data.repository.UserRepository;
import com.movielog.app.databinding.ActivityRegisterBinding;
import com.movielog.app.util.ThemeHelper;


public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private UserRepository userRepository;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleGoogleSignInResult(task);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userRepository = new UserRepository(getApplication());

        setupGoogleSignIn();
        setupButtons();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupButtons() {
        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.etUsername.getEditText().getText().toString().trim();
            String email = binding.etEmail.getEditText().getText().toString().trim();
            String password = binding.etPassword.getEditText().getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Snackbar.make(binding.getRoot(), "Please fill all fields", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Snackbar.make(binding.getRoot(), "Password must be at least 6 characters", Snackbar.LENGTH_SHORT).show();
                return;
            }

            User user = new User(username, password, email);
            userRepository.register(user, (success, message) -> {
                runOnUiThread(() -> {
                    if (success) {
                        Snackbar.make(binding.getRoot(), "Registration successful!", Snackbar.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
                    }
                });
            });
        });

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String email = account.getEmail();
            String displayName = account.getDisplayName() != null ? account.getDisplayName() : "User";

            if (email == null) {
                Snackbar.make(binding.getRoot(), "Could not get Google email", Snackbar.LENGTH_SHORT).show();
                return;
            }

            userRepository.login(email, "google_auth", (success, user) -> {
                if (success) {
                    runOnUiThread(() -> saveSessionAndGoMain(user));
                } else {
                    User newUser = new User(displayName, "google_auth", email);
                    userRepository.register(newUser, (regSuccess, msg) -> {
                        if (regSuccess) {
                            userRepository.login(email, "google_auth", (s2, u2) -> {
                                if (s2) runOnUiThread(() -> saveSessionAndGoMain(u2));
                            });
                        } else {
                            runOnUiThread(() -> Snackbar.make(binding.getRoot(),
                                    "This email is already registered. Please login with your password.",
                                    Snackbar.LENGTH_LONG).show());
                        }
                    });
                }
            });

        } catch (ApiException e) {
            if (e.getStatusCode() != 12501) {
                Snackbar.make(binding.getRoot(),
                        "Google Sign-In failed: " + e.getStatusCode(),
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void saveSessionAndGoMain(User user) {
        SharedPreferences.Editor editor = getSharedPreferences("movielog_prefs", MODE_PRIVATE).edit();
        editor.putInt("user_id", user.getId());
        editor.putString("username", user.getUsername());
        editor.putBoolean("is_admin", user.isAdmin());
        editor.apply();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        finish();
    }
}
