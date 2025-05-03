package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<review> reviewList;

    public ReviewAdapter(List<review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        review review = reviewList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private TextView reviewText;
        private TextView user;
        private RatingBar ratingBar;
        private ShapeableImageView avatarImageView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewText = itemView.findViewById(R.id.textViewReviewText);
            user = itemView.findViewById(R.id.textViewUserName1);
            ratingBar = itemView.findViewById(R.id.ratingBarReview);
            avatarImageView = itemView.findViewById(R.id.imageViewUserAvatar);
        }

        public void bind(review review) {
            reviewText.setText(review.getReviewText());
            user.setText(review.getName());
            ratingBar.setRating(review.getRating());

            // Загружаем аватарку пользователя с помощью Glide
            Glide.with(itemView.getContext())
                    .load(review.getUserAvatarUrl())  // Получаем URL аватарки
                    .placeholder(R.drawable.profile1)  // Заполнитель, если изображение не загружено
                    .into(avatarImageView);  // Применяем к ShapeableImageView
        }
    }
}
