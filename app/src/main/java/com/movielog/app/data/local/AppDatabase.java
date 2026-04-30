package com.movielog.app.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.movielog.app.data.model.FavoriteMovie;
import com.movielog.app.data.model.User;
import java.util.concurrent.Executors;

@Database(entities = {FavoriteMovie.class, User.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract FavoriteDao favoriteDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "movielog_database"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            // Admin kullanıcısını otomatik oluştur
                            Executors.newSingleThreadExecutor().execute(() -> {
                                AppDatabase database = getInstance(context);
                                User admin = new User("Admin", "admin123", "admin@movielog.com");
                                admin.setAdmin(true);
                                database.userDao().insert(admin);
                            });
                        }
                    })
                    .build();
        }
        return instance;
    }
}
