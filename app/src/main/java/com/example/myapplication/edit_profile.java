package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;

public class edit_profile extends AppCompatActivity {

    private EditText editTextName;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private EditText editTextAddress;
    private EditText editTextPhoneNumber;
    private TextInputEditText editText;
    private TextInputLayout textInputLayout;
    private Button buttonSave;
    private ImageView imageViewAvatar;

    private FirebaseAuth auth;
    private DatabaseReference userReference;
    private StorageReference storageReference;
    private ActivityResultLauncher<Intent> placePickerLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri newAvatarUri;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_pprofile);

        editText = findViewById(R.id.editTextKaspiNumber);
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
        }
        if (editText != null) {
            editText.setText("+7 "); // Автоматически добавляем "+7 "
            editText.setSelection(editText.getText().length()); // Курсор в конец

            editText.addTextChangedListener(new TextWatcher() {
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

                    editText.setText(formatted);
                    editText.setSelection(formatted.length()); // Ставим курсор в конец

                    isEditing = false;
                }
            });
        }
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyC9OvphEbPlhbe-cCgKTfbk8v2oWZUToJc");
        }
        placesClient = Places.createClient(this);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        userReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference().child("avatars");

        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonSave = findViewById(R.id.buttonRegister);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        textInputLayout = findViewById(R.id.textInputLayoutKaspiNumber);
        buttonSave.setOnClickListener(view -> saveChanges());
        imageViewAvatar.setOnClickListener(view -> showImagePickerDialog());
        editTextAddress.setOnClickListener(view -> openPlacePicker());

        // Регистрация ActivityResultLauncher для обработки результата выбора места
        placePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        editTextAddress.setText(place.getAddress());
                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                        Status status = Autocomplete.getStatusFromIntent(result.getData());
                        Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    newAvatarUri = imageUri;
                    Glide.with(this)
                            .load(imageUri)
                            .circleCrop() // делает изображение круглым
                            .into(imageViewAvatar);
                }
            }
        });

        loadUserData();
    }


    private void loadUserData() {

        userReference.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                users user = snapshot.getValue(users.class);
                if (user != null) {
                    // Проверяем роль пользователя
                    if ("продавец".equalsIgnoreCase(user.getType())) {
                        textInputLayout.setVisibility(View.VISIBLE);
                    } else {
                        textInputLayout.setVisibility(View.GONE);
                    }
                    editTextName.setText(user.getname());
                    editTextAddress.setText(user.getAddress());
                    editTextPhoneNumber.setText(user.getPhonenumber());
                    editText.setText(user.getKaspinumber());

                    String avatarUrl = user.getAvatarUrl();
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(this)
                                .load(avatarUrl)
                                .circleCrop() // делает изображение круглым
                                .into(imageViewAvatar);
                    }


                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveChanges() {
        String name = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String kaspiNumber = editText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Please enter your name");
            editTextName.requestFocus();
            return;
        }

        userReference.child("name").setValue(name);
        userReference.child("address").setValue(address);
        userReference.child("phonenumber").setValue(phoneNumber);
        userReference.child("kaspinumber").setValue(kaspiNumber)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Успех!")
                                .setMessage("Ваш профиль успешно обновлён.")
                                .setPositiveButton("ОК", (dialog, which) -> finish())
                                .show();



                    } else {
                        Toast.makeText(this, "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        if (newAvatarUri != null) {
            uploadNewAvatar(newAvatarUri);
        }
    }

    private void chooseNewAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            imagePickerLauncher.launch(takePictureIntent);
        }
    }

    private void openPlacePicker() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
        placePickerLauncher.launch(intent);
    }

    private void uploadNewAvatar(Uri avatarUri) {
        StorageReference newAvatarRef = storageReference.child(auth.getCurrentUser().getUid() + ".jpg");

        UploadTask uploadTask = newAvatarRef.putFile(avatarUri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return newAvatarRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                if (downloadUri != null) {
                    userReference.child("avatarUrl").setValue(downloadUri.toString());
                }
            } else {
                Toast.makeText(this, "Failed to upload new avatar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source")
                .setItems(new CharSequence[]{"Gallery", "Camera"}, (dialogInterface, which) -> {
                    switch (which) {
                        case 0:
                            chooseNewAvatar();
                            break;
                        case 1:
                            takePhoto();
                            break;
                    }
                })
                .show();
    }
}
