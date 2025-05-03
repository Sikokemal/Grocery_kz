package com.example.myapplication;

public class BannerAd {
    private String imageUrl;
    private String bannerText;
    public BannerAd() {
        // Пустое тело конструктора
    }


    public BannerAd(String imageUrl, String bannerText) {
        this.imageUrl = imageUrl;
        this.bannerText = bannerText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getBannerText() {
        return bannerText;
    }
}


