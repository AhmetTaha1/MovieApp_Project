package com.movielog.app.ui.profile;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.movielog.app.data.model.FavoriteMovie;
import com.movielog.app.data.model.User;
import com.movielog.app.data.repository.FavoriteRepository;
import com.movielog.app.data.repository.UserRepository;
import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final LiveData<List<FavoriteMovie>> favorites;

    public ProfileViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
        favorites = new FavoriteRepository(application).getAllFavorites();
    }

    public void loadUser(int userId) {
        userRepository.getUserById(userId, user -> currentUser.postValue(user));
    }

    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<List<FavoriteMovie>> getFavorites() { return favorites; }
}
