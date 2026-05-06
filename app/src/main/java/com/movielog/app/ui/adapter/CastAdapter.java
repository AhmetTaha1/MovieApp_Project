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
import com.movielog.app.data.model.TmdbCreditsResponse;
import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.ViewHolder> {

    private final List<TmdbCreditsResponse.TmdbCast> castList;

    public CastAdapter(List<TmdbCreditsResponse.TmdbCast> castList) {
        this.castList = castList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TmdbCreditsResponse.TmdbCast actor = castList.get(position);
        holder.tvName.setText(actor.getName());

        String photoUrl = null;
        if (actor.getProfilePath() != null) {
            photoUrl = "https://image.tmdb.org/t/p/w185" + actor.getProfilePath();
        }

        Glide.with(holder.itemView.getContext())
                .load(photoUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivPhoto);
    }

    @Override
    public int getItemCount() {
        return castList == null ? 0 : castList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivPhoto;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_actor_name);
            ivPhoto = itemView.findViewById(R.id.iv_actor_photo);
        }
    }
}
