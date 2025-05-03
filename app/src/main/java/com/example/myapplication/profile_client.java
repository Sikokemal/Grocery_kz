package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class profile_client extends AppCompatActivity {

    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView phonenumber;
    private TextView address;
    private ImageView avatarImageView;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_client);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        avatarImageView = findViewById(R.id.avatarImageView);
        phonenumber = findViewById(R.id.phonenumber);
        address = findViewById(R.id.address);
        TextView addressTextView = findViewById(R.id.address);
        addressTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = addressTextView.getText().toString();
                openMap(address);
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Проверяем, вошел ли пользователь в систему
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Если пользователь вошел в систему, загружаем его данные
            loadUserData(currentUser.getUid());
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_shop:
                        // Обработка нажатия на вкладку "Магазин"
                        // Можно добавить переход или другую логику
                        Intent profileIntent = new Intent(profile_client.this, buyeractivity.class);
                        profileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(profileIntent);

                        break;

                    case R.id.menu_cart:
                        // Обработка нажатия на вкладку "Корзина"
                        // Переход в активити корзины или другая логика
                        Intent profile2Intent = new Intent(profile_client.this,UserOrdersActivity.class);
                        profile2Intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(profile2Intent);
                        break;

                    case R.id.menu_profile:
                        // Обработка нажатия на вкладку "Профиль"
                        // Переход в активити профиля или другая логика


                        break;
                }
                return true;
            }
        });

        LinearLayout linearLayout = findViewById(R.id.addinfo); // замените на ваш id
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), edit_profile.class); // Замените NextActivity на ваше Activity
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Найдите ваше BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Установите выбранный элемент на "Главная" (замените на ID вашего элемента для главного экрана)
        bottomNavigationView.setSelectedItemId(R.id.menu_profile);
    }
    public void openNewWindow(View view) {
        // Код для открытия новой активности или фрагмента
        Intent intent = new Intent(this, UserOrdersActivity.class);
        startActivity(intent);
    }
    public void addproducts(View view) {
        // Код для открытия новой активности или фрагмента
        Intent intent = new Intent(this, UserOrdersActivity.class);
        startActivityForResult(intent, 1);
    }

    // Метод для обработки результатов редактирования профиля
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Если результат редактирования профиля успешен, обновляем данные
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                loadUserData(currentUser.getUid());
            }
        }
    }

    private void loadUserData(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    users user = dataSnapshot.getValue(users.class);

                    if (user != null) {
                        textViewUsername.setText(user.getname());
                        textViewEmail.setText(user.getEmail());
                        phonenumber.setText(user.getPhonenumber());
                        address.setText(user.getAddress());

                        // Загрузка изображения с помощью Glide
                        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                            Glide.with(profile_client.this)
                                    .load(user.getAvatarUrl())
                                    .placeholder(R.drawable.profile1) // Заглушка, пока изображение загружается
                                    .error(R.drawable.profile1) // Используется в случае ошибки загрузки
                                    .into(avatarImageView);
                        } else {
                            // Если URL изображения не предоставлен, используйте изображение по умолчанию
                            avatarImageView.setImageResource(R.drawable.profile1);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибки чтения данных из базы данных
                Toast.makeText(profile_client.this, "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMap(String address) {
        // Создаем интент для открытия карты с указанным адресом
        Uri locationUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, locationUri);
        mapIntent.setPackage("com.google.android.apps.maps"); // Указываем, что хотим открыть в приложении Google Maps
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Если приложение Google Maps не установлено, можно открыть в браузере
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address)));
            startActivity(browserIntent);
        }
    }

    @Override
    public void onBackPressed() {
        // Создаем Intent для перехода на главную активность
        Intent intent = new Intent(this, buyeractivity.class); // Замените на вашу главную активность
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Убедиться, что активность не дублируется
        startActivity(intent);
    }

}
