package com.example.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class add_new_product extends AppCompatActivity {
    private DatabaseReference productsRef;
    private List<Product> productList;
    private ProductAdapterall2 productAdapter;
    private StorageReference storageRef;
    private Uri imageUri;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private RecyclerView recyclerView;
    private ImageView imageView; // Renamed from ImageView to imageView
    private Dialog loadingDialog;

    private EditText editTextProductName;
    private LinearLayout linearLayoutImages;
    private EditText editTextDescription;
    private EditText editTextProductPrice;
    private List<Uri> imageUris = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_product);

// Получаем ссылку на AutoCompleteTextView
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.spinnerCategory);

// Создаем массив элементов
        String[] items = {"Фрукты", "Овощи","Молочные продукты","Мясо и птица","Рыба и морепродукты","Зерновые и бобовые","Яйца","Мёд и другие","Орехи и семена", "Разное"};

// Устанавливаем элементы для автозаполнения
        ((MaterialAutoCompleteTextView) autoCompleteTextView).setSimpleItems(items);

        checkPermissions();
        Spinner unitSpinner = findViewById(R.id.unitSpinner);
        String selectedUnit = unitSpinner.getSelectedItem().toString(); // Получаем выбранную единицу измерения (штука или килограмм)


        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(getCurrentUserId());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String avatarUrl = dataSnapshot.child("avatarUrl").getValue(String.class);
                imageView = findViewById(R.id.imageView);

                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Context context = add_new_product.this;

                    Glide.with(context)
                            .load(avatarUrl)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("Glide", "Error loading image", e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Log.d("Glide", "Image loaded successfully");
                                    return false;
                                }
                            })
                            .into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(add_new_product.this, "Ошибка при добавлении продукта", Toast.LENGTH_SHORT).show();
            }
        });

        storageRef = FirebaseStorage.getInstance().getReference();

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Устанавливаем GridLayoutManager с spanCount равным 2


        productsRef = FirebaseDatabase.getInstance().getReference().child("products");
        productList = new ArrayList<>();
        productAdapter = new ProductAdapterall2(this, productList);
        recyclerView.setAdapter(productAdapter);

        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextDescription = findViewById(R.id.editTextDescription1);


        loadProducts();



        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    if (data.getClipData() != null) { // Несколько изображений
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri selectedImageUri = data.getClipData().getItemAt(i).getUri();
                            imageUris.add(selectedImageUri);
                            addImageToLinearLayout(selectedImageUri); // Добавляем в интерфейс
                        }
                    } else if (data.getData() != null) { // Одно изображение
                        Uri selectedImageUri = data.getData();
                        imageUris.add(selectedImageUri);
                        addImageToLinearLayout(selectedImageUri); // Добавляем в интерфейс
                    }
                }
            }
        });
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                // Обработка снимка с камеры
                handleCameraResult();
            }
        });

        ImageView imageViewAvatar = findViewById(R.id.imageView2);
        imageViewAvatar.setOnClickListener(view -> showImagePickerDialog());

        Button buttonAddCategories = findViewById(R.id.buttonAddCategories);
        buttonAddCategories.setOnClickListener(view -> addCategoriesToFirebase());
        Button buttonAddProduct = findViewById(R.id.buttonAddProduct);
        buttonAddProduct.setOnClickListener(view -> addProduct());
    }
    private void addImageToLinearLayout(Uri imageUri) {
        // Находим LinearLayout
        LinearLayout linearLayoutImages = findViewById(R.id.linearLayoutImages);

        // Создаем новый ImageView
        ImageView imageView = new ImageView(this);

        // Устанавливаем параметры для ImageView
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                300, // Ширина изображения (например, 300 пикселей)
                300  // Высота изображения
        );
        params.setMargins(16, 0, 16, 0); // Отступы между изображениями

        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(60)))
                .into(imageView);

        // Добавляем ImageView в LinearLayout
        linearLayoutImages.addView(imageView);
    }
    private void showLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false); // Блокируем пользовательский ввод
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void addCategoriesToFirebase() {
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference().child("categories");

        Category vegetablesCategory = new Category("Овощи", "https://cdn0.iconfinder.com/data/icons/food-color-fill/100/food_-01-1024.png");
        categoriesRef.child("bvegetables").setValue(vegetablesCategory);

        Category fruitsCategory = new Category("Фрукты", "https://cdn0.iconfinder.com/data/icons/fruity-3/512/Apple-1024.png");
        categoriesRef.child("afruits").setValue(fruitsCategory);

        Category meatCategory = new Category("Мясо и птица", "https://cdn2.iconfinder.com/data/icons/food-desserts-drinks-and-sweets/512/meat-1024.png");
        categoriesRef.child("dmeat_poultry").setValue(meatCategory);

        Category fishCategory = new Category("Рыба и морепродукты", "https://cdn1.iconfinder.com/data/icons/fillio-food-kitchen-and-cooking/48/food_-_fish-1024.png");
        categoriesRef.child("efish_seafood").setValue(fishCategory);

        Category grainsCategory = new Category("Зерновые и бобовые", "https://cdn3.iconfinder.com/data/icons/food-ingredients-1/50/68-1024.png");
        categoriesRef.child("fgrains_legumes").setValue(grainsCategory);

        Category dairyCategory = new Category("Молочные продукты", "https://cdn2.iconfinder.com/data/icons/hand-drawn-dairy-products/512/milk-1024.png");
        categoriesRef.child("cdairy").setValue(dairyCategory);

        Category eggsCategory = new Category("Яйца", "https://cdn1.iconfinder.com/data/icons/spring-291/32/Egg-1024.png");
        categoriesRef.child("geggs").setValue(eggsCategory);

        Category nutsCategory = new Category("Орехи и семена", "https://cdn3.iconfinder.com/data/icons/nuts-and-seeds-1/3500/nut_food_healthy_walnut-1024.png");
        categoriesRef.child("inuts_seeds").setValue(nutsCategory);

        Category honeyCategory = new Category("Мёд и другие", "https://cdn3.iconfinder.com/data/icons/russia-element-1/64/14-honey-jar-pot-sweet-healthy-food-512.png");
        categoriesRef.child("honey").setValue(honeyCategory);

        Category other = new Category("Разное", "https://cdn3.iconfinder.com/data/icons/farm-15/64/farm-products-basket-meat-512.png");
        categoriesRef.child("jother").setValue(other);
    }


    private void setImageUri(Uri uri) {
        ImageView imageViewAvatar = findViewById(R.id.imageView2);
        if (uri != null) {
            imageViewAvatar.setImageURI(uri);
        }
    }

    private void handleCameraResult() {
        if (imageUri != null) {
            imageUris.add(imageUri);
         } else {
            Toast.makeText(this, "Ошибка: Uri изображения равен null", Toast.LENGTH_SHORT).show();
        }
    }


    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите источник изображения")
                .setItems(new CharSequence[]{"Галерея", "Камера"}, (dialogInterface, which) -> {
                    switch (which) {
                        case 0:
                            openGallery();
                            break;
                        case 1:
                            openCamera();
                            break;
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.myapplication.fileprovider", photoFile);
                imageUri = photoUri;  // Установите imageUri заранее для использования в методе onActivityResult
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(cameraIntent);
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                Toast.makeText(this, "Выбрано из галереи: " + selectedImageUri.toString(), Toast.LENGTH_SHORT).show();

                imageUri = selectedImageUri; // Сохраняем Uri в переменной imageUri

                setImageUri(imageUri);
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Другие обработки результатов...
            handleCameraResult();
        }
    }



    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            try {
                File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                imageUri = Uri.fromFile(imageFile);
                return imageFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешения получены, можно выполнять операции с галереей и камерой
            } else {
                Toast.makeText(this, "Разрешения не предоставлены. Некоторые функции могут не работать.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addProduct() {
        showLoadingDialog(); // Показываем анимацию загрузки

        String productName = editTextProductName.getText().toString().trim();
        String priceText = editTextProductPrice.getText().toString().trim();



        String sellerId = getCurrentUserId();
        AutoCompleteTextView autoCompleteCategory = findViewById(R.id.spinnerCategory);
        String selectedCategory = autoCompleteCategory.getText().toString().trim(); // Получаем выбранную категорию
        String description = editTextDescription.getText().toString().trim(); // Получаем описание продукта
        // Получаем выбранную единицу измерения
        Spinner unitSpinner = findViewById(R.id.unitSpinner);
        String selectedUnit = unitSpinner.getSelectedItem().toString();

        if (productName.isEmpty()) {
            editTextProductName.setError("Пишите название товара");
            hideLoadingDialog(); // Закрываем анимацию загрузки

            return;
        }
        if (priceText.isEmpty()) {
            editTextProductPrice.setError("Пишите цену товара");
            hideLoadingDialog(); // Закрываем анимацию загрузки

            return;
        }
        Double productPrice = Double.valueOf(priceText);

        if (description.isEmpty()) {
            editTextDescription.setError("Пишите описание продукта");
            hideLoadingDialog(); // Закрываем анимацию загрузки
            return;
        }
        // Проверяем, есть ли изображения
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "Добавьте изображение!", Toast.LENGTH_SHORT).show();
            hideLoadingDialog();
            ImageView imageViewAvatar = findViewById(R.id.imageView2);
            imageViewAvatar.setImageURI(imageUris.get(0)); // Устанавливаем первое изображение

            return;
        }
        // Проверяем, есть ли изображения
        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "выберите категории продукта", Toast.LENGTH_SHORT).show();
            hideLoadingDialog();
            return;
        }


        // Создаём список для хранения URL изображений
        List<String> imageUrls = new ArrayList<>();

        // Загружаем каждое изображение в Firebase Storage
        for (Uri imageUri : imageUris) {
            uploadImageToStorage(imageUri, sellerId, productName, productPrice, selectedCategory, description, selectedUnit, imageUrls);
        }
    }

    private void uploadImageToStorage(Uri imageUri, String sellerId, String productName, Double productPriceStr, String selectedCategory, String description, String selectedUnit, List<String> imageUrls) {
        StorageReference fileReference = storageRef.child("product_images/" + System.currentTimeMillis() + ".jpg");
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Получаем URL изображения после успешной загрузки
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrls.add(uri.toString());  // Сохраняем URL изображения в список

                        if (imageUrls.size() == imageUris.size()) {
                            // Когда все изображения загружены, сохраняем продукт в Firebase Database
                            saveProductToDatabase(sellerId, productName, productPriceStr, selectedCategory, description, selectedUnit, imageUrls);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(add_new_product.this, "Ошибка при загрузке изображения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void saveProductToDatabase(String sellerId, String productName, Double productPriceStr, String selectedCategory, String description, String selectedUnit, List<String> imageUrls) {
        // Создаём объект продукта
        Product newProduct = new Product(sellerId, productName, productPriceStr, selectedCategory, description, selectedUnit, imageUrls);

        // Добавляем продукт в базу данных
        String productId = productsRef.push().getKey();
        if (productId != null) {
            productsRef.child(productId).setValue(newProduct)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(add_new_product.this, "Продукт успешно добавлен!", Toast.LENGTH_SHORT).show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);


                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_new_product.this, "Ошибка при добавлении продукта: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }



    // Метод для отображения Toast в UI-потоке
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(add_new_product.this, message, Toast.LENGTH_SHORT).show());
    }




    // В методе addProductToDatabase


    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return "seller123";
        }
    }

    private void loadProducts() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");

            productsRef.orderByChild("userId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    productList.clear();

                    for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                        Product product = productSnapshot.getValue(Product.class);
                        if (product != null) {
                            productList.add(product);
                        }
                    }

                    productAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("BuyerActivity", "Ошибка чтения продуктов из Firebase: " + databaseError.getMessage());
                }
            });
        }
    }
}
