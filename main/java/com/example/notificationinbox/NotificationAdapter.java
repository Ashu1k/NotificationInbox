package com.example.notificationinbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(NotificationItem item);
    }

    private final Context context;
    private final ArrayList<NotificationItem> list;
    private final OnItemClickListener listener;

    public NotificationAdapter(Context context, ArrayList<NotificationItem> list,
                               OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void updateList(ArrayList<NotificationItem> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());
        holder.time.setText(formatDate(item.getTimestamp()));
        holder.unreadDot.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

        if (item.getBannerImg() != null && !item.getBannerImg().isEmpty()) {
          /*  Glide.with(context).load(item.getBannerImg())
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder).centerCrop().into(holder.banner);*/

            Glide.with(context)
                    .load(item.getBannerImg())
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .centerCrop()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                com.bumptech.glide.load.engine.GlideException e,
                                Object model,
                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                boolean isFirstResource) {

//                            android.util.Log.e("GLIDE_IMAGE", "Image loading failed", e);
                            android.util.Log.e("GLIDE_IMAGE",
                                    "FAILED URL: " + model, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                android.graphics.drawable.Drawable resource,
                                Object model,
                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                com.bumptech.glide.load.DataSource dataSource,
                                boolean isFirstResource) {

//                            android.util.Log.d("GLIDE_IMAGE", "Image loaded successfully");

                            android.util.Log.d("GLIDE_IMAGE",
                                    "SUCCESS URL: " + model);
                            return false;
                        }
                    })
                    .into(holder.banner);
        } else {
            holder.banner.setImageResource(R.drawable.image_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    private String formatDate(String value) {
        try {
            long millis = Long.parseLong(value);
            return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(new Date(millis));
        } catch (Exception e) {
            return value == null ? "" : value;
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView banner;
        TextView title, description, time, unreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            banner = itemView.findViewById(R.id.itemBanner);
            title = itemView.findViewById(R.id.itemTitle);
            description = itemView.findViewById(R.id.itemDescription);
            time = itemView.findViewById(R.id.itemTime);
            unreadDot = itemView.findViewById(R.id.unreadDot);
        }
    }
}
