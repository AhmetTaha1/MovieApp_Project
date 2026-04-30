package com.movielog.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.movielog.app.R;
import com.movielog.app.data.model.User;
import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(User user);
    }

    public interface OnResetPasswordClickListener {
        void onResetPassword(User user);
    }

    private List<User> users = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;
    private final OnResetPasswordClickListener resetListener;

    public AdminUserAdapter(OnDeleteClickListener deleteListener,
                            OnResetPasswordClickListener resetListener) {
        this.deleteListener = deleteListener;
        this.resetListener = resetListener;
    }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeUser(User user) {
        int idx = users.indexOf(user);
        if (idx >= 0) {
            users.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Avatar ilk harf
        String initial = user.getUsername().isEmpty() ? "?"
                : String.valueOf(user.getUsername().charAt(0)).toUpperCase();
        holder.tvAvatar.setText(initial);
        holder.tvUsername.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());

        // Admin badge
        if (user.isAdmin()) {
            holder.tvAdminBadge.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.INVISIBLE);
            holder.btnReset.setVisibility(View.INVISIBLE);
        } else {
            holder.tvAdminBadge.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnReset.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(user));
            holder.btnReset.setOnClickListener(v -> resetListener.onResetPassword(user));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvUsername, tvEmail, tvAdminBadge;
        MaterialButton btnDelete, btnReset;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_user_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvAdminBadge = itemView.findViewById(R.id.tv_admin_badge);
            btnDelete = itemView.findViewById(R.id.btn_delete_user);
            btnReset = itemView.findViewById(R.id.btn_reset_password);
        }
    }
}
