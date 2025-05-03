package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private List<OnboardingItem> onboardingItems; // <-- переместили сюда

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        TextView btnSkip = findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(v -> {
            // Сохраняем, что онбординг пройден (если используешь SharedPreferences)
            SharedPreferences prefs = getSharedPreferences("onboarding", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            // Запускаем следующую активность
            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class); // или LoginActivity
            startActivity(intent);
            finish(); // закрываем онбординг
        });

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        onboardingItems = Arrays.asList(
                new OnboardingItem(R.raw.buy, "Выбирайте и покупайте свежие продукты без лишней суеты."),
                new OnboardingItem(R.raw.sell, "Продавайте свои товары с комфортом и уверенностью."),
                new OnboardingItem(R.raw.delivery, "Быстрая доставка и удобная связь в одном приложении.")
        );


        viewPager.setAdapter(new OnboardingAdapter(this, onboardingItems));

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < onboardingItems.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }
}
