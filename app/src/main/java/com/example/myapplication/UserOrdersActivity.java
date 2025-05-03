package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

public class UserOrdersActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private UserOrdersPagerAdapter pagerAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_orders);

        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new UserOrdersPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_shop:
                        // Обработка нажатия на вкладку "Магазин"
                        // Можно добавить переход или другую логику
                        Intent profileIntent = new Intent(UserOrdersActivity.this, buyeractivity.class);
                        profileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(profileIntent);

                        break;

                    case R.id.menu_cart:

                        break;

                    case R.id.menu_profile:
                        // Обработка нажатия на вкладку "Корзина"
                        // Переход в активити корзины или другая логика
                        Intent profile2Intent = new Intent(UserOrdersActivity.this,profile_client.class);
                        profile2Intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(profile2Intent);
                        break;
                }
                return true;
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Найдите ваше BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Установите выбранный элемент на "Главная" (замените на ID вашего элемента для главного экрана)
        bottomNavigationView.setSelectedItemId(R.id.menu_cart);
    }
    @Override
    public void onBackPressed() {
        // Создаем Intent для перехода на главную активность
        Intent intent = new Intent(this, buyeractivity.class); // Замените на вашу главную активность
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Убедиться, что активность не дублируется
        startActivity(intent);
    }

}
