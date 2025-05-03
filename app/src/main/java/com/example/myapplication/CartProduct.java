// CartProduct.java
package com.example.myapplication;

public class CartProduct {
    private String userId;
    private String url;
    private String status;

    private String productId;
    private int quantity;
    private String productName;
    private double productPrice;
    private String sellerId; // Поле для sellerId
    private String unit;



    public CartProduct() {
        // Default constructor required for Firebase
    }

    public CartProduct(String url,String userId, String productId, int quantity, String productName, double productPrice,String status,String sellerId,String unit) {
        this.userId = userId;
        this.url = url;
        this.productId = productId;
        this.status = status;
        this.sellerId = sellerId;

        this.quantity = quantity;
        this.productName = productName;
        this.productPrice = productPrice;
        this.unit = unit;
    }

    public String getUserId() {
        return userId;
    }
    // Вложенный интерфейс для слушателя изменений количества
    public interface OnQuantityChangedListener {
        void onQuantityChanged(int newQuantity);
    }

    // Добавьте переменную для хранения слушателя изменений количества
    private OnQuantityChangedListener quantityChangedListener;

    // Метод для установки слушателя изменений количества
    public void setOnQuantityChangedListener(OnQuantityChangedListener listener) {
        this.quantityChangedListener = listener;
    }


    // Геттер и сеттер для sellerId
    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
    // Метод для вызова слушателя при изменении количества
    private void notifyQuantityChanged(int newQuantity) {
        if (quantityChangedListener != null) {
            quantityChangedListener.onQuantityChanged(newQuantity);
        }
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getProductId() {
        return productId;
    }
    public String getUrl() {
        return url;
    }


    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }



    public String getUnit() {

        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }
}
