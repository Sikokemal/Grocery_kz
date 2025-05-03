package com.example.myapplication;

import java.util.List;
import java.util.UUID;

public class Product {
    private float averageRating;
    private long totalRatings;
    private String productId;
    private String id;
    private String name;
    private double price;
    private String sellerId;
    private String category;
    private String description; // Новое поле для описания продукта
    private String unit;  // Штука или Килограмм

    private List<String> imageUrls; // Список URL изображений
    private String userId;
    private int orderCount; // Новое поле для отслеживания количества заказов

    // Пустой конструктор, требуется для использования Firebase
    public Product() {
        // Генерируем случайный идентификатор при создании объекта
        this.id = UUID.randomUUID().toString();
    }

    // Конструктор для создания нового продукта
    public Product(String userId, String name, double price, String category, String description, String unit,List<String> imageUrls) {
        this.userId = userId;
        this.productId = UUID.randomUUID().toString();
        this.name = name;
        this.price = price;
        this.unit = unit;

        this.sellerId = sellerId;
        this.category = category;
        this.imageUrls = imageUrls; // Инициализируем список изображений
        this.description = description; // Добавляем инициализацию описания
    }

    // Геттеры и сеттеры для totalRatings
    public long getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(long totalRatings) {
        this.totalRatings = totalRatings;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    // Геттеры и сеттеры для orderCount
    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    // Геттеры и сеттеры для описания продукта
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProductId() {
        return productId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Геттеры и сеттеры для списка изображений
    public List<String> getImageUrls() {

        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getSellerId() {
        return sellerId;
    }
}
