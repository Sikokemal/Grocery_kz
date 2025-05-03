package com.example.myapplication;

public class users {
    private String id;
    private String name;
    private String email;
    private String type;
    private String password;
    private String address;
    private String phonenumber;
    private String kaspinumber;
    private String avatarUrl;
    // Обязательный пустой конструктор для работы с Firebase
    public users() {
    }

    public users(String id, String email,String name, String type, String password, String address, String phonenumber,String kaspinumber, String avatarUrl) {
        this.id = id;
        this.name=name;
        this.email = email;
        this.type = type;
        this.password = password;
        this.address = address;
        this.phonenumber = phonenumber;
        this.kaspinumber = kaspinumber;
        this.avatarUrl = avatarUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name=name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhonenumber() {
        return phonenumber;
    }
    public String getKaspinumber() {
        return kaspinumber;
    }

    public void setPhonenumber(String phonenumber) {

        this.phonenumber = phonenumber;
    }
    public void setKaspinumber(String kaspinumber) {
        this.kaspinumber = kaspinumber; // Теперь корректно
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
