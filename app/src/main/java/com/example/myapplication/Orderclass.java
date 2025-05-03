package com.example.myapplication;

import java.util.List;

public class Orderclass {
    private String orderId;
    private List<CartProduct> cartProductsList;
    private double totalAmount;
    private String selectedDeliveryMethod;
    private String userId;
    private String status;
    private String sellerIdString; // Новое поле для хранения всех sellerId, разделённых запятой

    private String pdfUri; // Поле для хранения URI PDF
    private String userName; // Поле для имени пользователя
    private String userAddress; // Поле для адреса пользователя
    private String phoneNumber; // Поле для номера телефона
    private String clientCode; // Поле для кода клиента
    private String orderDate; // Поле для даты заказа

    public Orderclass() {
        // Обязательный конструктор без параметров для Firebase
    }

    public Orderclass(String orderId, String userName, String userAddress, String phoneNumber, List<CartProduct> cartProductsList,
                      double totalAmount, String selectedDeliveryMethod, String userId, String clientCode,String sellerIdString, String orderDate) {
        this.orderId = orderId;
        this.userName = userName; // Инициализация имени пользователя
        this.userAddress = userAddress; // Инициализация адреса пользователя
        this.phoneNumber = phoneNumber; // Инициализация номера телефона
        this.cartProductsList = cartProductsList;
        this.totalAmount = totalAmount;
        this.selectedDeliveryMethod = selectedDeliveryMethod;
        this.userId = userId;
        this.clientCode = clientCode; // Инициализация кода клиента
        this.sellerIdString = sellerIdString; // Устанавливаем строку sellerId

        this.status = "Pending"; // По умолчанию статус "Pending"
        this.orderDate = orderDate; // Сохранение даты заказа

    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public List<CartProduct> getCartProductsList() {
        return cartProductsList;
    }
    public String getSellerIdString() {
        return sellerIdString; // Геттер для sellerIdString
    }

    public void setSellerIdString(String sellerIdString) {
        this.sellerIdString = sellerIdString; // Сеттер для sellerIdString
    }
    public double getTotalAmount() {
        return totalAmount;
    }

    public String getSelectedDeliveryMethod() {
        return selectedDeliveryMethod;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPdfUri() {
        return pdfUri;
    }

    public void setPdfUri(String pdfUri) {
        this.pdfUri = pdfUri;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getClientCode() {
        return clientCode; // Геттер для кода клиента
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode; // Сеттер для кода клиента
    }
}
