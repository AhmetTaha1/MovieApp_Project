package com.movielog.app;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.movielog.app.data.model.User;
import com.movielog.app.data.repository.UserRepository;
import com.movielog.app.databinding.ActivityAdminBinding;
import com.movielog.app.ui.adapter.AdminUserAdapter;
import com.movielog.app.util.ThemeHelper;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private UserRepository userRepository;
    private AdminUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        userRepository = new UserRepository(getApplication());

        setupRecyclerView();
        loadUsers();
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(
                user -> showDeleteConfirmation(user),
                user -> showResetPasswordDialog(user));
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);
    }

    private void loadUsers() {
        userRepository.getAllUsers(users -> {
            runOnUiThread(() -> {
                adapter.setUsers(users);
                binding.tvUserCount.setText(users.size() + " Registered Users");
            });
        });
    }

    private void showDeleteConfirmation(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete \"" + user.getUsername() + "\"?\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        userRepository.deleteUser(user.getId(), () -> {
            runOnUiThread(() -> {
                adapter.removeUser(user);
                int remaining = adapter.getItemCount();
                binding.tvUserCount.setText(remaining + " Registered Users");
            });
        });
    }

    private void showResetPasswordDialog(User user) {
        EditText etNewPass = new EditText(this);
        etNewPass.setHint("New password (min 6 chars)");
        etNewPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout container = new LinearLayout(this);
        container.setPadding(60, 20, 60, 0);
        container.addView(etNewPass);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Reset password for: " + user.getUsername())
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newPass = etNewPass.getText().toString().trim();
                    if (newPass.length() < 6) {
                        new AlertDialog.Builder(this)
                                .setMessage("Password must be at least 6 characters.")
                                .setPositiveButton("OK", null).show();
                        return;
                    }
                    userRepository.updatePassword(user.getEmail(), newPass, () ->
                            runOnUiThread(() ->
                                    new AlertDialog.Builder(this)
                                            .setMessage("Password for \"" + user.getUsername() + "\" updated.")
                                            .setPositiveButton("OK", null).show()
                            )
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
