package com.movielog.app.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.movielog.app.data.model.User;
import java.util.List;

@Dao
public interface UserDao {

    // Kullanıcı kayıt et
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(User user);

    // Email ve şifre ile giriş kontrolü
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    // Email ile kullanıcı var mı kontrol et
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // ID ile kullanıcı getir
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);

    // Tüm kullanıcıları listele (Admin panel için)
    @Query("SELECT * FROM users ORDER BY id ASC")
    List<User> getAllUsers();

    // Kullanıcıyı ID ile sil
    @Query("DELETE FROM users WHERE id = :userId")
    void deleteById(int userId);

    // Şifre güncelle
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    void updatePassword(String email, String newPassword);
}