package com.example.myapplication;

public class OnboardingItem {
    int animationResId;
    String title;

    public OnboardingItem(int animationResId, String title) {
        this.animationResId = animationResId;
        this.title = title;
    }

    public int getAnimationResId() { return animationResId; }
    public String getTitle() { return title; }
}
