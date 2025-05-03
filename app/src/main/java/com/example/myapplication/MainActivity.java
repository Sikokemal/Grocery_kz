package com.example.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private EditText editTextEmail;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private TextInputLayout textInputLayoutName2;
    private Button button;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private RadioGroup radioGroupUserType;

    // Имя файла SharedPreferences
    private static final String SHARED_PREFS = "sharedPrefs";
    // Ключи для данных в SharedPreferences
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final int RC_SIGN_IN = 123; // Код запроса для Google Sign-In
     private BiometricPrompt biometricPrompt;
     private static final String REMEMBER_ME_KEY = "rememberMe";
    public static final String PREF_NAME = "my_app_prefs"; // используем один файл




    private CheckBox rememberMeCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextEmail = findViewById(R.id.editTextText);
        textInputLayoutName2 = findViewById(R.id.textInputLayoutName2);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        textInputLayoutName2.setCounterEnabled(false);

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() < 6) {
                    textInputLayoutName2.setCounterEnabled(true);
                    // Устанавливаем красный цвет текста хелпертекста и сообщаем об ошибке
                    textInputLayoutName2.setHelperTextColor(ColorStateList.valueOf(Color.RED));
                    textInputLayoutName2.setError("Пароль должен быть минимум из 6 символов");
                } else {
                    // Убираем красный цвет и сообщение об ошибке, если введенный текст удовлетворяет условию
                    textInputLayoutName2.setHelperTextColor(ColorStateList.valueOf(Color.BLACK)); // Или другой цвет текста хелпертекста
                    textInputLayoutName2.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputLayoutName2.setEndIconDrawable(R.drawable.img_eye);
        textInputLayoutName2.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переключение видимости пароля
                if (editTextPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    textInputLayoutName2.setEndIconDrawable(R.drawable.img_eye); // Установите иконку для скрытого пароля
                } else {
                    editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    textInputLayoutName2.setEndIconDrawable(R.drawable.img_eye1); // Установите иконку для видимого пароля
                }
                editTextPassword.setSelection(editTextPassword.getText().length()); // Перемещение курсора в конец текста
            }
        });
        buttonLogin = findViewById(R.id.button);
        button = findViewById(R.id.button3);
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        biometricPrompt = createBiometricPrompt();
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        // Load remember me state
        loadRememberMeState();

        // Listen for changes in the checkbox state
         rememberMeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 saveRememberMeState(isChecked);
            }
        });


        // Проверка: первый запуск или нет
        SharedPreferences prefs = getSharedPreferences("onboarding_prefs", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("first_time", true);

        if (isFirstTime) {
            // Первый запуск — запускаем онбординг и сохраняем, что он пройден
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("first_time", false);
            editor.apply();

            // Переход к онбордингу
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            finish(); // Закрываем MainActivity, чтобы не возвращаться к нему
            return;
        }


        TextView textView = findViewById(R.id.txtForgotpassword);

        // Установка обработчика событий нажатия на TextView
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создание интента для запуска новой активности
                Intent intent = new Intent(MainActivity.this, forgotpass.class);

                // Добавление дополнительной информации в интент, если необходимо
                // intent.putExtra("key", "value");

                // Запуск новой активности
                startActivity(intent);
            }
        });
        checkRememberMe();
        auth = FirebaseAuth.getInstance();
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем диалоговое окно для выбора роли
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Выберите роль")
                        .setItems(new String[]{"Продавец", "Покупатель"}, (dialog, which) -> {
                            // Обработка выбора роли
                            if (which == 0) { // Если выбрали "Продавец"
                                Intent sellerIntent = new Intent(MainActivity.this, Sellerregistr.class);
                                sellerIntent.putExtra("role", "продавец"); // Передача роли
                                startActivity(sellerIntent);
                            } else if (which == 1) { // Если выбрали "Клиент"
                                Intent clientIntent = new Intent(MainActivity.this, Registr.class);
                                clientIntent.putExtra("role", "покупатель"); // Передача роли
                                startActivity(clientIntent);
                            }

                            // Сохраняем аутентификацию в SharedPreferences
                            SharedPreferences sharedPref = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("is_authenticated", true);
                            editor.apply();
                        });

                // Показ диалогового окна
                builder.create().show();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                logUser(email,password);
             }
        });

        // Настройка кнопки Google Sign-In
        SignInButton googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        // Проверка, зарегистрирован ли пользователь

    }

    private void checkRememberMe() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean rememberMe = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false);
        boolean isAuthenticated = sharedPreferences.getBoolean("is_authenticated", false); // Проверяем, авторизован ли пользователь

        if (rememberMe && isAuthenticated) {
            // Если пользователь авторизован и выбран чекбокс "Remember Me", то показываем биометрическую аутентификацию
            if (isBiometricAvailable()) {
                showBiometricPrompt();
            }
        } else {
            // Здесь показывается форма для входа без биометрической аутентификации
        }
    }

    private BiometricPrompt createBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Пользователь успешно прошел аутентификацию
                // Выполните необходимые действия
                // Например, перейдите к следующему экрану
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Authenticating...");
                progressDialog.setCancelable(true);
                progressDialog.show();

                loadUserData();

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                loadUserData();

                // Аутентификация не удалась
                // Обработайте ситуацию соответствующим образом
            }
        };

        return new BiometricPrompt(this, executor, callback);
    }

    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Биометрическая аутентификация")
                .setSubtitle("Приложите палец к сенсору")
                .setNegativeButtonText(" ")
                .build();

        biometricPrompt = createBiometricPrompt(); // ← не забудь создать перед этим
        biometricPrompt.authenticate(promptInfo);
    }


    private boolean isBiometricAvailable() {
        return true;  // Убедитесь, что это удовлетворяет вашим требованиям
    }
    private void saveRememberMeState(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(REMEMBER_ME_KEY, isChecked);
        editor.apply();
    }


    private void loadRememberMeState() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean rememberMe = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false);
        rememberMeCheckbox.setChecked(rememberMe); // Устанавливаем состояние чекбокса
    }


    private void saveUserData(String username, String password, boolean rememberMe) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(REMEMBER_ME_KEY, rememberMe);
        editor.putBoolean("is_authenticated", true); // ← важно
        editor.apply();
    }


    // Добавленный метод для загрузки данных из SharedPreferences

    private void loadUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String password = sharedPreferences.getString(KEY_PASSWORD, "");

        editTextEmail.setText(username);
        editTextPassword.setText(password);

        logUser(username, password); // авторизуем пользователя
    }


    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                // Аккаунт Google выбран успешно
                String email = account.getEmail();
                String displayName = account.getDisplayName();
                Uri photoUri = account.getPhotoUrl();

                // Передать данные в вашу активити регистрации
                checkIfUserExistsInDatabase(email, displayName, photoUri);

            }
        } catch (ApiException e) {
            Log.e("GoogleSignIn", "signInResult: failed code=" + e.getStatusCode());
        }
    }

    private void checkIfUserExistsInDatabase(String email, String displayName, Uri photoUri) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users");

        userReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Аккаунт с таким email уже существует в базе данных
                    // Возможно, выполните здесь дополнительные действия в зависимости от вашего случая

                    // Пример: вы можете получить информацию о пользователе и передать ее в активити регистрации
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        users existingUser = userSnapshot.getValue(users.class);
                        if (existingUser != null) {
                            logUser(email, existingUser.getPassword());
                            return;
                        }
                    }
                } else {
                    // Аккаунта с таким email еще нет в базе данных
                    // Можете выполнить необходимые действия, например, перейти к активити регистрации
                    startRegistrationActivity(email, displayName, photoUri);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибки запроса к базе данных, если необходимо
                Log.e("DatabaseError", "Error checking user existence: " + databaseError.getMessage());
            }
        });
    }
    private void startRegistrationActivity(String email, String displayName, Uri photoUri) {
        Intent registrationIntent = new Intent(this, Registr.class);

        // Передать данные в активити регистрации
        registrationIntent.putExtra("userEmail", email);
        registrationIntent.putExtra("displayName", displayName);
        registrationIntent.putExtra("photoUri", photoUri.toString());

        // Запустить активити регистрации
        startActivity(registrationIntent);
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            // Пользователь успешно вошел с использованием Google Sign-In

                        }
                    } else {
                        // Аутентификация не удалась
                        Log.e("GoogleSignIn", "signInWithCredential: failure", task.getException());
                        Toast.makeText(MainActivity.this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void logUser(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                saveUserData(email, password, rememberMeCheckbox.isChecked());

                                // Адрес электронной почты подтвержден, пользователь может войти
                                clearCart();
                                checkUserType(user.getUid());
                            } else {
                                // Адрес электронной почты не подтвержден, выведите сообщение и не позволяйте пользователю войти
                                FirebaseAuth.getInstance().signOut(); // Выход пользователя
                                Toast.makeText(MainActivity.this, "Please verify your email address.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // Обработка ошибок аутентификации
                        String errorMessage = "Authentication failed: " + task.getException().getMessage();
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("LoginActivity", errorMessage);
                    }
                });
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Регистрация успешна
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            sendEmailVerification(user);
                        }
                    } else {
                        // Ошибка регистрации
                        Toast.makeText(MainActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (user.isEmailVerified()) {

                            Intent intent = new Intent(MainActivity.this, Registr.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Адрес электронной почты не подтвержден
                            Toast.makeText(MainActivity.this, "Email not verified. Please check your email for verification instructions.", Toast.LENGTH_LONG).show();
                            // Опционально: Переслать электронное письмо для подтверждения
                        }

                        finish();
                        Toast.makeText(MainActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    } else {
                        // Обработка ошибок отправки верификационного письма
                        Toast.makeText(MainActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void checkUserType(String uid) {
        databaseReference.child(uid).child("type").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String userType = dataSnapshot.getValue(String.class);
                    if ("admin".equals(userType)) {
                        // Пользователь является админом, перейдите в админ-панель
                        startActivity(new Intent(MainActivity.this, seller_profile.class));
                        finish();
                    } else if ("продавец".equals(userType)) {
                        // Пользователь является продавцом, перейдите в экран продавца
                        startActivity(new Intent(MainActivity.this, seller_profile.class));
                        finish();
                    } else {

                        startActivity(new Intent(MainActivity.this, buyeractivity.class));
                        finish();

                    }
                } else {
                    // Ошибка: информация о типе пользователя отсутствует в базе данных
                    Toast.makeText(MainActivity.this, "User type information not found", Toast.LENGTH_SHORT).show();
                    Log.e("LoginActivity", "User type information not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Ошибка при чтении из базы данных
                Toast.makeText(MainActivity.this, "Error reading user type", Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Error reading user type: " + databaseError.getMessage());
            }
        });
    }
    private void markFirstTimeLogin(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("rememberMe", isChecked);
        editor.apply();
    }


    private void clearCart() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("CartProduct").child(currentUser.getUid());
            cartRef.removeValue(); // Удаляем все продукты из корзины
        }
    }

}