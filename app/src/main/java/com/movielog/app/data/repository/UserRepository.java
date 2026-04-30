package com.movielog.app.data.repository;

import android.app.Application;
import com.movielog.app.data.local.AppDatabase;
import com.movielog.app.data.local.UserDao;
import com.movielog.app.data.model.User;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private UserDao userDao;
    private ExecutorService executor;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Kayıt ol
    public void register(User user, RegisterCallback callback) {
        executor.execute(() -> {
            // Email daha önce kayıtlı mı kontrol et
            User existingUser = userDao.getUserByEmail(user.getEmail());
            if (existingUser != null) {
                callback.onResult(false, "Email already exists");
            } else {
                long result = userDao.insert(user);
                if (result != -1) {
                    callback.onResult(true, "Registration successful");
                } else {
                    callback.onResult(false, "Registration failed");
                }
            }
        });
    }

    // Giriş yap
    public void login(String email, String password, LoginCallback callback) {
        executor.execute(() -> {
            User user = userDao.login(email, password);
            if (user != null) {
                callback.onResult(true, user);
            } else {
                callback.onResult(false, null);
            }
        });
    }

    // ID ile kullanıcı getir
    public void getUserById(int id, UserCallback callback) {
        executor.execute(() -> {
            User user = userDao.getUserById(id);
            callback.onResult(user);
        });
    }

    // Tüm kullanıcıları getir (Admin panel için)
    public void getAllUsers(AllUsersCallback callback) {
        executor.execute(() -> {
            List<User> users = userDao.getAllUsers();
            callback.onResult(users);
        });
    }

    // Email ile kullanıcı var mı kontrol et
    public void getUserByEmail(String email, UserCallback callback) {
        executor.execute(() -> {
            User user = userDao.getUserByEmail(email);
            callback.onResult(user);
        });
    }

    // Şifre güncelle
    public void updatePassword(String email, String newPassword, Runnable onComplete) {
        executor.execute(() -> {
            userDao.updatePassword(email, newPassword);
            if (onComplete != null) onComplete.run();
        });
    }

    // Kullanıcı sil (Admin panel için)
    public void deleteUser(int userId, Runnable onComplete) {
        executor.execute(() -> {
            userDao.deleteById(userId);
            if (onComplete != null) onComplete.run();
        });
    }

    // Callback interface'leri
    public interface RegisterCallback {
        void onResult(boolean success, String message);
    }

    public interface LoginCallback {
        void onResult(boolean success, User user);
    }

    public interface UserCallback {
        void onResult(User user);
    }

    public interface AllUsersCallback {
        void onResult(List<User> users);
    }
}