package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

    private EditText editProductName, editProductPrice, editProductDescription;
    private ImageView productImageView;
    private ArrayList<String> imageUrlsList = new ArrayList<>();
    private Button btnUpdateProduct;
    private DatabaseReference productRef;
    private StorageReference productImageRef;
    private Uri imageUri;
    private String productId;
    private ProgressDialog loadingBar;
    private LinearLayout linearLayoutImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);


        productId = getIntent().getStringExtra("productId");
        if (TextUtils.isEmpty(productId)) {
            Toast.makeText(this, "Ошибка: продукт не найден!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button deleteProductButton = findViewById(R.id.btnDeleteProduct);

        deleteProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Получаем значение productId, например, из Intent или поля в классе
                String productId = getIntent().getStringExtra("productId");

                if (productId != null && !productId.isEmpty()) {
                    deleteProduct(productId);
                } else {
                    Toast.makeText(EditProductActivity.this, "Ошибка: productId пустой", Toast.LENGTH_SHORT).show();
                }
            }
        });

        linearLayoutImages = findViewById(R.id.linearLayoutImages);
        productImageView = findViewById(R.id.imageView2);
        btnUpdateProduct = findViewById(R.id.editbutton);
        editProductName = findViewById(R.id.editTextProductName);
        editProductPrice = findViewById(R.id.editTextProductPrice);
        editProductDescription = findViewById(R.id.editTextDescription1);
        loadingBar = new ProgressDialog(this);

        // Загрузка текущих данных
        loadProductData();

        // Получаем список изображений
        String[] imageUrls = getIntent().getStringArrayExtra("getImageUrls");
        if (imageUrls != null) {
            imageUrlsList.addAll(Arrays.asList(imageUrls));
            for (String imageUrl : imageUrls) {
                addImageToLayout(imageUrl);
            }
        }

        // Обработчик клика для выбора нового изображения
        productImageView.setOnClickListener(v -> openGallery());

        // Кнопка обновления
        btnUpdateProduct.setOnClickListener(v -> updateProduct());
    }

    private void addImageToLayout(String imageUrl) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
        layoutParams.setMargins(10, 0, 10, 0);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(imageUrl).into(imageView);

        imageView.setOnLongClickListener(v -> {
            // Удаляем изображение из Layout
            linearLayoutImages.removeView(imageView);
            imageUrlsList.remove(imageUrl); // Удаляем из списка URL
            return true;
        });

        // Добавляем изображение в Layout
        linearLayoutImages.addView(imageView);
    }



    private void loadProductData() {
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("productName");
            String description = intent.getStringExtra("description");
            double price = intent.getDoubleExtra("productPrice", 0.0);
            editProductName.setText(title);
            editProductPrice.setText(String.valueOf(price));
            editProductDescription.setText(description);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 1); // Исправлен requestCode
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) { // Исправлено
            imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void deleteProduct(String productId) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");

        // Поиск продукта по полю productId
        Query query = productsRef.orderByChild("productId").equalTo(productId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Получаем ключ записи
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();

                        // Удаляем продукт по ключу
                        productsRef.child(key).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Продукт успешно удален
                                            Toast.makeText(EditProductActivity.this, "Продукт удален", Toast.LENGTH_SHORT).show();
                                            finish(); // Закрываем активность или можно перенаправить пользователя
                                        } else {
                                            // Ошибка при удалении
                                            Toast.makeText(EditProductActivity.this, "Ошибка при удалении продукта", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else {
                    // Продукт не найден
                    Toast.makeText(EditProductActivity.this, "Продукт не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Ошибка при запросе
                Toast.makeText(EditProductActivity.this, "Ошибка запроса к базе данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            if (productImageRef == null) {
                // Инициализация, если это не сделано ранее
                productImageRef = FirebaseStorage.getInstance().getReference().child("product_images");
            }
            StorageReference filePath = productImageRef.child(productId + "_" + System.currentTimeMillis() + ".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                        String newImageUrl = uri.toString();
                        imageUrlsList.add(newImageUrl);
                        addImageToLayout(newImageUrl);
                    });
                } else {
                    Toast.makeText(EditProductActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Изображение не выбрано", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProduct() {
        String name = editProductName.getText().toString().trim();
        String price = editProductPrice.getText().toString().trim();
        String description = editProductDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Ошибка: productId пуст!", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setTitle("Обновление продукта");
        loadingBar.setMessage("Пожалуйста, подождите...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");

        // Находим товар по полю productId
        Query query = productsRef.orderByChild("productId").equalTo(productId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                        String firebaseKey = productSnapshot.getKey(); // ID в базе

                        Map<String, Object> productUpdates = new HashMap<>();
                        productUpdates.put("name", name);
                        productUpdates.put("price", Double.parseDouble(price));
                        productUpdates.put("description", description);

                        if (!imageUrlsList.isEmpty()) {
                            productUpdates.put("imageUrls", imageUrlsList);
                        }

                        // Обновляем найденный товар
                        productsRef.child(firebaseKey).updateChildren(productUpdates)
                                .addOnCompleteListener(task -> {
                                    loadingBar.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditProductActivity.this, "Продукт обновлён!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(EditProductActivity.this, "Ошибка обновления!", Toast.LENGTH_SHORT).show();
                                        Log.e("EditProductActivity", "Ошибка обновления", task.getException());
                                    }
                                });
                        break; // Обновляем только первый найденный
                    }
                } else {
                    loadingBar.dismiss();
                    Toast.makeText(EditProductActivity.this, "Ошибка: продукт не найден!", Toast.LENGTH_SHORT).show();
                    Log.e("EditProductActivity", "Продукт с ID " + productId + " не найден.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingBar.dismiss();
                Toast.makeText(EditProductActivity.this, "Ошибка базы данных!", Toast.LENGTH_SHORT).show();
                Log.e("EditProductActivity", "Ошибка базы данных", error.toException());
            }
        });
    }
}
