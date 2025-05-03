package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class seller_profile extends AppCompatActivity {

    private TextView textViewUsername;
    private TextView roletype;
    private TextView textViewEmail;
    private TextView phonenumber;
    private TextView address;
    private ImageView avatarImageView;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_profile);
        textViewUsername = findViewById(R.id.textViewUsername);
        roletype = findViewById(R.id.roletype);
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
        LinearLayout linearLayout = findViewById(R.id.addinfo); // замените на ваш id
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), edit_profile.class); // Замените NextActivity на ваше Activity
                startActivity(intent);
            }
        });

    }

    public void openNewWindow(View view) {
        Intent intent = new Intent(this, activity_seller_orders.class);
        startActivity(intent);
    }
    public void addproducts(View view) {
        Intent intent = new Intent(this, add_new_product.class);
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
                        roletype.setText(user.getType());
                        textViewEmail.setText(user.getEmail());
                        phonenumber.setText(user.getPhonenumber());
                        address.setText(user.getAddress());

                        // Загрузка изображения с помощью Glide
                        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                            Glide.with(seller_profile.this)
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
                Toast.makeText(seller_profile.this, "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

}
