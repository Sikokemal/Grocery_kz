package com.example.myapplication;

public class review {
    private String userId;
    private String userEmail; // Добавлено поле для email пользователя
    private String name; // Добавлено поле для email пользователя
    private String productId;
    private String reviewText;
    private float rating;
    private String userAvatarUrl; // Добавлено поле для URL аватарки пользователя


    public review() {
    }

    public review(String userId, String userEmail, String productId, String reviewText, float rating, String userAvatarUrl,String name) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.name = name;
        this.productId = productId;
        this.reviewText = reviewText;
        this.rating = rating;
        this.userAvatarUrl = userAvatarUrl; // Инициализация URL аватарки

    }

    public String getUserAvatarUrl() {
        return userAvatarUrl; // Возвращаем URL аватарки
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl; // Устанавливаем URL аватарки
    }

    public String getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getName() {
        return name;
    }

    public String getProductId() {
        return productId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public float getRating() {
        return rating;
    }
    // Геттеры и сеттеры (необходимые для Firebase)
}
