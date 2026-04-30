package com.movielog.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.movielog.app.R;
import com.movielog.app.data.model.FavoriteMovie;
import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<FavoriteMovie> favorites = new ArrayList<>();
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteMovie movie);
        void onFavoriteDelete(FavoriteMovie movie);
    }

    public FavoriteAdapter(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteMovie movie = favorites.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public void setFavorites(List<FavoriteMovie> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPoster;
        TextView tvTitle;
        TextView tvYear;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvYear = itemView.findViewById(R.id.tv_year);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_ID) {
                    listener.onFavoriteClick(favorites.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_ID) {
                    listener.onFavoriteDelete(favorites.get(position));
                }
                return true;
            });
        }

        public void bind(FavoriteMovie movie) {
            tvTitle.setText(movie.getTitle());
            tvYear.setText(movie.getYear());

            Glide.with(itemView.getContext())
                    .load(movie.getPoster())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(ivPoster);
        }
    }
}