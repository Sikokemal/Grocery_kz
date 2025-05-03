package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
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

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Registr extends AppCompatActivity {
    private FirebaseAuth auth;
    private ImageButton buttonSelectAddress;

    private DatabaseReference databaseReference;
    private StorageReference storageRef;
    private Uri imageUri;

    private PlacesClient placesClient;
    private ActivityResultLauncher<Intent> placePickerLauncher;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private RadioGroup radioGroupUserType;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private LottieAnimationView lottieAnimationView;
    private EditText editTextName; // Добавлено поле для имени

    private EditText editTextAddress;
    private EditText editTextPhoneNumber;
    // Создайте переменную для хранения выбранного адреса
    private String selectedAddress;

    private ActivityResultLauncher<Intent> mapsLauncher;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registr);


        lottieAnimationView = findViewById(R.id.lottieAnimationView);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageRef = FirebaseStorage.getInstance().getReference().child("avatars");
        String userEmail = getIntent().getStringExtra("userEmail");

         editTextEmail = findViewById(R.id.editTextEmail); // Замените на реальный идентификатор вашего EditText

        editTextEmail.setText(userEmail);
        editTextName = findViewById(R.id.editTextName); // Инициализация поля для имени


        editTextAddress = findViewById(R.id.editTextAddress);
        editTextAddress.setFocusable(false); // Устанавливаем фокус на EditText вручную

        editTextAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Здесь разместите код для обработки нажатия на EditText
                // Например, открытие плейспикера или другое действие

                openPlacePicker();
                // После обработки события вам может потребоваться снова установить фокус на EditText:
                editTextAddress.setFocusable(true);
                editTextAddress.setFocusableInTouchMode(true);
                editTextAddress.requestFocus();
            }
        });
        editTextAddress.setOnClickListener(view -> openPlacePicker());

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        if (editTextPhoneNumber != null) {
            editTextPhoneNumber.setText("+7 "); // Автоматически добавляем "+7 "
            editTextPhoneNumber.setSelection(editTextPhoneNumber.getText().length()); // Курсор в конец

            editTextPhoneNumber.addTextChangedListener(new TextWatcher() {
                private boolean isEditing = false;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (isEditing) return;
                    isEditing = true;

                    String raw = s.toString().replaceAll("[^0-9]", ""); // Оставляем только цифры
                    if (raw.startsWith("7")) raw = raw.substring(1); // Убираем первую 7, если есть

                    String formatted = "+7";
                    if (raw.length() > 0) formatted += " (" + raw.substring(0, Math.min(3, raw.length()));
                    if (raw.length() > 3) formatted += ") " + raw.substring(3, Math.min(6, raw.length()));
                    if (raw.length() > 6) formatted += " " + raw.substring(6, Math.min(8, raw.length()));
                    if (raw.length() > 8) formatted += " " + raw.substring(8, Math.min(10, raw.length()));

                    editTextPhoneNumber.setText(formatted);
                    editTextPhoneNumber.setSelection(formatted.length()); // Ставим курсор в конец

                    isEditing = false;
                }
            });
        }        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonRegister);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);

        checkPermissions();

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    Toast.makeText(this, "Выбрано из галереи", Toast.LENGTH_SHORT).show();
                    imageUri=selectedImageUri;

                    setImageUri(selectedImageUri);

                }
            }
        });
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                // Обработка снимка с камеры
                handleCameraResult();
            }
        });


        ImageView imageViewAvatar = findViewById(R.id.imageViewAvatar);
        imageViewAvatar.setOnClickListener(view -> showImagePickerDialog());

        buttonLogin.setOnClickListener(v -> loginUser());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Инициализация Places API
        Places.initialize(getApplicationContext(), "AIzaSyC9OvphEbPlhbe-cCgKTfbk8v2oWZUToJc");
        placesClient = Places.createClient(this);


        placePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    editTextAddress.setText(place.getAddress());
                }
            }
        });
        mapsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // Обработка результата от карт
            if (result.getResultCode() == RESULT_OK) {
                // Ваш код обработки результата от карт
                // Ваш код для запуска активности карты
                Intent mapIntent = new Intent(/* Ваш Intent для запуска карты */);
                mapsLauncher.launch(mapIntent);

            }
        });



    }

    private void setImageUri(Uri uri) {
        ImageView imageViewAvatar = findViewById(R.id.imageViewAvatar);

        Glide.with(this)
                .load(uri)
                .circleCrop() // делает изображение круглым
                .into(imageViewAvatar);
    }

    private void handleCameraResult() {
        if (imageUri != null) {
            // После захвата изображения отобразим его
            setImageUri(imageUri);
        } else {
            Toast.makeText(this, "Ошибка: Uri изображения равен null", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPlacePicker() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);

        startActivityForResult(intent,100);
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

        if (requestCode == 100 && resultCode==RESULT_OK) {
            Place place=Autocomplete.getPlaceFromIntent(data);

            editTextAddress.setText(place.getAddress());
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
    private void loginUser() {
        showLoading();
        String userEmail = getIntent().getStringExtra("userEmail");
        String displayName = getIntent().getStringExtra("displayName");
        String photoUriString = getIntent().getStringExtra("photoUri");
        Uri photoUri = (photoUriString != null) ? Uri.parse(photoUriString) : null;

        if (userEmail != null) {
            editTextEmail.setText(userEmail);
        }
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String phone = editTextPhoneNumber.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();

        buttonLogin.setEnabled(false);

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Registr.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            hideLoading();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String userType = getUserType();
                        String role = "покупатель";



                        uploadImageToStorage(uid, email, name, role, password, address, phone);
                        sendVerificationCode(email, password, name, address, phone);
                        // После успешной регистрации и отправки письма с кодом, перенаправляем пользователя на экран входа
                        startActivity(new Intent(Registr.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Registr.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        hideLoading();
                    }
                });
    }

    private void sendVerificationCode(String email, String password, String name, String address, String phone) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Registr.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            // После успешной отправки кода верификации можно выполнить какие-либо дополнительные действия, если необходимо
                        } else {
                            Toast.makeText(Registr.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    private void showLoading() {
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();
        buttonLogin.setEnabled(false);

    }

    private void hideLoading() {
        lottieAnimationView.cancelAnimation();
        lottieAnimationView.setVisibility(View.GONE);
        buttonLogin.setEnabled(true);
    }

    private String getUserType() {
        int selectedRadioButtonId = radioGroupUserType.getCheckedRadioButtonId();

        switch (selectedRadioButtonId) {
            case R.id.radioButtonBuyer:
                return "покупатель";
            case R.id.radioButtonSeller:
                return "продавец";
            default:
                return "покупатель"; // По умолчанию
        }
    }

    private void uploadImageToStorage(String uid, String email,String name, String userType, String password, String address, String phone) {
        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("avatars").child(uid + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveUserToDatabase(uid, email,name, userType, password, address, phone, imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        // Обработка ошибок загрузки изображения
                    });
        } else {
            // Обработка случая, когда imageUri равен null
            saveUserToDatabase(uid, email,name, userType, password, address, phone, null);

        }
    }


    private void saveUserToDatabase(String uid, String email,String name, String userType, String password, String address, String phone, String imageUrl) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        users user = new users(uid, email,name, userType, password, address, phone,null, imageUrl);

        userReference.setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("RegisterActivity", "User data saved successfully");

                    } else {
                        Log.e("RegisterActivity", "Error saving user data: " + task.getException().getMessage());
                    }
                });
    }

    private void clearCart() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("CartProduct").child(currentUser.getUid());
            cartRef.removeValue(); // Удаляем все продукты из корзины
        }
    }

}
