package com.movielog.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import com.movielog.app.databinding.ActivityLoginBinding;
import com.movielog.app.util.ThemeHelper;


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
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
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userRepository = new UserRepository(getApplication());

        // Zaten giriş yapılmış mı kontrol et
        SharedPreferences prefs = getSharedPreferences("movielog_prefs", MODE_PRIVATE);
        if (prefs.getInt("user_id", -1) != -1) {
            goToMain();
            return;
        }

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
        // Email/şifre ile giriş
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getEditText().getText().toString().trim();
            String password = binding.etPassword.getEditText().getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(binding.getRoot(), "Please fill all fields", Snackbar.LENGTH_SHORT).show();
                return;
            }

            userRepository.login(email, password, (success, user) -> {
                runOnUiThread(() -> {
                    if (success) {
                        saveSessionAndGoMain(user);
                    } else {
                        Snackbar.make(binding.getRoot(),
                                "Invalid email or password",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // Google ile giriş
        binding.btnGoogleSignIn.setOnClickListener(v -> {
            // Önceki oturumu kapat ki hesap seçici açılsın
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        // Şifremi unuttum
        binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // Kayıt ol linki
        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
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

            // Room'da bu email'e sahip kullanıcı var mı kontrol et
            userRepository.login(email, "google_auth", (success, user) -> {
                if (success) {
                    runOnUiThread(() -> saveSessionAndGoMain(user));
                } else {
                    // Yoksa otomatik kayıt yap
                    User newUser = new User(displayName, "google_auth", email);
                    userRepository.register(newUser, (regSuccess, msg) -> {
                        if (regSuccess) {
                            userRepository.login(email, "google_auth", (s2, u2) -> {
                                if (s2) runOnUiThread(() -> saveSessionAndGoMain(u2));
                            });
                        } else {
                            // Kullanıcı zaten var ama şifre farklı (email+password kayıtlı)
                            // Kullanıcıya uyarı ver
                            runOnUiThread(() -> Snackbar.make(binding.getRoot(),
                                    "This email is already registered. Please login with your password.",
                                    Snackbar.LENGTH_LONG).show());
                        }
                    });
                }
            });

        } catch (ApiException e) {
            if (e.getStatusCode() != 12501) { // 12501 = user cancelled
                Snackbar.make(binding.getRoot(),
                        "Google Sign-In failed: " + e.getStatusCode(),
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void showForgotPasswordDialog() {
        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        LinearLayout container = new LinearLayout(this);
        container.setPadding(60, 20, 60, 0);
        container.addView(etEmail);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Enter your registered email address.")
                .setView(container)
                .setPositiveButton("Continue", (dialog, which) -> {
                    String email = etEmail.getText().toString().trim();
                    if (email.isEmpty()) return;
                    checkEmailAndResetPassword(email);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkEmailAndResetPassword(String email) {
        userRepository.getUserByEmail(email, user -> {
            runOnUiThread(() -> {
                if (user != null) {
                    showNewPasswordDialog(email);
                } else {
                    Snackbar.make(binding.getRoot(),
                            "No account found with this email.", Snackbar.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showNewPasswordDialog(String email) {
        EditText etNewPass = new EditText(this);
        etNewPass.setHint("New password (min 6 chars)");
        etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout container = new LinearLayout(this);
        container.setPadding(60, 20, 60, 0);
        container.addView(etNewPass);

        new AlertDialog.Builder(this)
                .setTitle("New Password")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newPass = etNewPass.getText().toString().trim();
                    if (newPass.length() < 6) {
                        Snackbar.make(binding.getRoot(),
                                "Password must be at least 6 characters.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    userRepository.updatePassword(email, newPass, () ->
                            runOnUiThread(() -> Snackbar.make(binding.getRoot(),
                                    "Password updated! You can now login.", Snackbar.LENGTH_LONG).show())
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Kullanıcı bilgilerini SharedPreferences'a kaydeder ve ana ekrana yönlendirir. */
    private void saveSessionAndGoMain(User user) {
        SharedPreferences.Editor editor = getSharedPreferences("movielog_prefs", MODE_PRIVATE).edit();
        editor.putInt("user_id", user.getId());
        editor.putString("username", user.getUsername());
        editor.putBoolean("is_admin", user.isAdmin());
        editor.apply();
        goToMain();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        finish();
    }
}
