package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private List<BannerAd> bannerAds;
    private Context context;

    public BannerAdapter(Context context, List<BannerAd> bannerAds) {
        this.context = context;
        this.bannerAds = bannerAds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BannerAd bannerAd = bannerAds.get(position);
         Glide.with(context).load(bannerAd.getImageUrl()).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return bannerAds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageviewbanner);
        }
    }
}
