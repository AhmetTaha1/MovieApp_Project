package com.movielog.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.movielog.app.R;
import java.util.List;

public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {

    private List<String> posterUrls;

    public PosterAdapter(List<String> posterUrls) {
        this.posterUrls = posterUrls;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_poster, parent, false);
        return new PosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(posterUrls.get(position))
                .placeholder(R.drawable.ic_movie_placeholder)
                .centerCrop()
                .into(holder.ivPoster);
    }

    @Override
    public int getItemCount() {
        return posterUrls.size();
    }

    static class PosterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;

        public PosterViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
        }
    }
}