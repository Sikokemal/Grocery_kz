// Category.java
package com.example.myapplication;

public class Category {
    private String categoryName;
    private String categoryIconUrl;

    public Category() {
        // Пустой конструктор, требуется для Firebase
    }

    public Category(String categoryName, String categoryIconUrl) {
        this.categoryName = categoryName;
        this.categoryIconUrl = categoryIconUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIconUrl() {
        return categoryIconUrl;
    }

    public void setCategoryIconUrl(String categoryIconUrl) {
        this.categoryIconUrl = categoryIconUrl;
    }
}
