package com.example.myapplication;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ActivityCartBinding;
import com.example.myapplication.util.PaymentsUtil;
import com.example.myapplication.viewmodel.CheckoutViewModel;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.button.ButtonOptions;
import com.google.android.gms.wallet.button.PayButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private TextView textUserAddress;
    private Button buttonChangeAddress;
    private DatabaseReference usersRef;
    private NewCartAdapter cartAdapter;
    private List<CartProduct> cartProductsList = new ArrayList<>();
    private DatabaseReference cartRef;
    private TextView totalAmountTextView;
    private String selectedDeliveryMethod = "Доставка"; // По умолчанию, можно изменить по необходимости
    private PayButton googlePayButton;
    private CheckoutViewModel model;
    private static final int SHARE_PDF_REQUEST = 2;

    private static final int PICK_PDF_REQUEST = 1;
    private Uri pdfUri;

    // Handle potential conflict from calling loadPaymentData.
     ActivityResultLauncher<IntentSenderRequest> resolvePaymentForResult = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                switch (result.getResultCode()) {
                    case Activity.RESULT_OK:
                        Intent resultData = result.getData();
                        if (resultData != null) {
                            PaymentData paymentData = PaymentData.getFromIntent(result.getData());
                            if (paymentData != null) {
                                handlePaymentSuccess(paymentData);
                            }
                        }
                        break;

                    case Activity.RESULT_CANCELED:
                        // The user cancelled the payment attempt
                        break;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUi();

        // Check Google Pay availability
        model = new ViewModelProvider(this).get(CheckoutViewModel.class);
        model.canUseGooglePay.observe(this, this::setGooglePayAvailable);
        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null) {
            if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
                if ("application/pdf".equals(intent.getType())) {
                    Uri pdfUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (pdfUri != null) {
                        // Здесь вы можете обработать полученный PDF файл
                        handlePdfUri(pdfUri);
                    }
                }
            }
        }
        // Initialize RecyclerView and other UI components
        recyclerView = findViewById(R.id.recyclerViewCartProducts);
        totalAmountTextView = findViewById(R.id.textViewTotal);
        Button buttonCheckout = findViewById(R.id.buttonCheckout);
        buttonCheckout.setEnabled(false);
        buttonCheckout.setAlpha(0.5f);
        cartAdapter = new NewCartAdapter(cartProductsList, totalAmountTextView);

        recyclerView.setAdapter(cartAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter.setRemoveProductClickListener(position -> {
            if (position >= 0 && position < cartProductsList.size()) {
                CartProduct product = cartProductsList.get(position);
                String userId = product.getUserId();
                String productId = product.getProductId();

                Log.d("FirebaseDelete", "Удаляем товар: userId=" + userId + ", productId=" + productId);

                if (userId != null && productId != null) {
                    DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("CartProduct")
                            .child(userId).child(productId);

                    cartRef.removeValue().addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseDelete", "Товар успешно удален из Firebase");

                        // ✅ Проверяем, что элемент еще существует в списке перед удалением
                        if (position < cartProductsList.size()) {
                            cartProductsList.remove(position);
                            cartAdapter.notifyItemRemoved(position);
                            cartAdapter.notifyItemRangeChanged(position, cartProductsList.size());
                        } else {
                            Log.w("FirebaseDelete", "Попытка удалить несуществующий элемент из списка");
                        }

                        updateTotalAmount();

                    }).addOnFailureListener(e -> {
                        Log.e("FirebaseDelete", "Ошибка удаления", e);
                     });
                } else {
                    Log.e("FirebaseDelete", "Ошибка: userId или productId == null");
                }
            } else {
                Log.w("FirebaseDelete", "Попытка удалить элемент с некорректным индексом: " + position);
            }
        });



        textUserAddress = findViewById(R.id.textUserAddress);



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Действие при нажатии на стрелку (например, переход на предыдущую активность)
                onBackPressed();
            }
        });

        // Добавьте обработчик нажатия кнопки "назад", если нужно
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.img_vector); // Установите свой собственный значок "назад"
        }

        Button bb = findViewById(R.id.bb);
        bb.setOnClickListener(v -> {

        });

        RadioGroup radioGroupDelivery = findViewById(R.id.radioGroupDelivery);
        radioGroupDelivery.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonPickup:
                    selectedDeliveryMethod = "Подобрать";
                    break;
                case R.id.radioButtonDelivery:
                    selectedDeliveryMethod = "Доставка";
                    break;
                // Добавьте дополнительные варианты, если необходимо
            }
        });

        // Инициализация DatabaseReference для корзины
        // Замените "user_id" на текущего пользователя или используйте свой способ идентификации пользователя


        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        loadCurrentUserAddress();
        loadCartProducts();
    }

    private void loadCurrentUserAddress() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    users user = snapshot.getValue(users.class);
                    if (user != null && user.getAddress() != null) {
                        textUserAddress.setText("Ваш адрес доставки: " +user.getAddress());
                    } else {
                        textUserAddress.setText("Адрес не найден");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textUserAddress.setText("Ошибка получения данных");
            }
        });


    }
    private void updateTotalAmount() {
        double totalAmount = 0;

        for (CartProduct cartProduct : cartProductsList) {
            totalAmount += cartProduct.getProductPrice() * cartProduct.getQuantity();
        }

        totalAmountTextView.setText(String.format("Общая сумма: %.2f тг", totalAmount));

    }




    private void initializeUi() {
        setContentView(R.layout.activity_cart);

        // Use view binding to access the UI elements
        ActivityCartBinding layoutBinding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());

        // The Google Pay button is a layout file – take the root view
        googlePayButton = layoutBinding.googlePayButton;
        try {
            googlePayButton.initialize(
                    ButtonOptions.newBuilder()
                            .setAllowedPaymentMethods(PaymentsUtil.getAllowedPaymentMethods().toString())
                            .build()
            );
            googlePayButton.setOnClickListener(this::requestPayment);
        } catch (Exception e) {
            // Keep Google Pay button hidden (consider logging this to your app analytics service)
        }

        Button buttonChoosePdf = findViewById(R.id.buttonqq1);
        loadKaspiNumber();

        buttonChoosePdf.setOnClickListener(v -> pickPDFFile());
    }
    private void loadKaspiNumber() {
        Button buttonChoosePdf = findViewById(R.id.buttonqq1);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    users user = dataSnapshot.getValue(users.class); // Загружаем объект пользователя

                    if (user != null) {
                        String kaspi = user.getKaspinumber(); // Получаем номер через геттер

                        if (kaspi != null && !kaspi.isEmpty()) {
                            buttonChoosePdf.setText(kaspi); // Устанавливаем текст кнопки
                        } else {
                            buttonChoosePdf.setText("Номер не найден");
                        }

                        Log.d("FirebaseData", "KaspiNumber: " + kaspi); // Логируем номер
                    }
                } else {
                    buttonChoosePdf.setText("Пользователь не найден");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                buttonChoosePdf.setText("Ошибка загрузки");
            }
        });




    }


    private void setGooglePayAvailable(boolean available) {
        if (available) {
            googlePayButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.google_pay_status_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    public void requestPayment(View view) {

         googlePayButton.setClickable(false);

        long totalPriceCents = getCartTotalCents(); // ✅ Динамически получаем сумму корзины
        final Task<PaymentData> task = model.getLoadPaymentDataTask(totalPriceCents);

        task.addOnCompleteListener(completedTask -> {
            if (completedTask.isSuccessful()) {
                handlePaymentSuccess(completedTask.getResult());
            } else {
                Exception exception = completedTask.getException();
                if (exception instanceof ResolvableApiException) {
                    PendingIntent resolution = ((ResolvableApiException) exception).getResolution();
                    resolvePaymentForResult.launch(new IntentSenderRequest.Builder(resolution).build());

                } else if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    handleError(apiException.getStatusCode(), apiException.getMessage());

                } else {
                    handleError(CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                            " exception when trying to deliver the task result to an activity!");
                }
            }

            // Re-enables the Google Pay payment button.
            googlePayButton.setClickable(true);
        });
    }
    private long getCartTotalCents() {
        double total = 0;
        for (CartProduct product : cartProductsList) {
            total += product.getProductPrice() * product.getQuantity();
        }
        return (long) (total * 100); // ✅ Переводим в центы
    }


    private void handlePaymentSuccess(PaymentData paymentData) {
        final String paymentInfo = paymentData.toJson();

        try {
            JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".

            final JSONObject info = paymentMethodData.getJSONObject("info");
            final String billingName = info.getJSONObject("billingAddress").getString("name");
            Toast.makeText(
                    this, getString(R.string.payments_show_name, billingName),
                    Toast.LENGTH_LONG).show();

            // Logging token string.
            Log.d("Google Pay token", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token"));

            Button buttonCheckout = findViewById(R.id.buttonCheckout);
            buttonCheckout.setEnabled(true);
            buttonCheckout.setAlpha(1.0f);
            buttonCheckout.setOnClickListener(v -> placeOrder(null));




            startActivity(new Intent(this, CheckoutSuccessActivity.class));

        } catch (JSONException e) {
            Log.e("handlePaymentSuccess", "Error: " + e);
        }
    }
    private void handleError(int statusCode, @Nullable String message) {
        Log.e("loadPaymentData failed",
                String.format(Locale.getDefault(), "Error code: %d, Message: %s", statusCode, message));
    }

    private double getTotalAmount() {
        double totalAmount = 0;

        for (CartProduct cartProduct : cartProductsList) {
            totalAmount += cartProduct.getProductPrice() * cartProduct.getQuantity();
        }

        return totalAmount;
    }

    private void clearCart() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("CartProduct").child(currentUser.getUid());
            cartRef.removeValue();
        }
    }
    private void checkCartState() {
        Log.d("DEBUG", "cartProductsList size: " + cartProductsList.size());
        boolean isCartEmpty = cartProductsList.isEmpty();
        Button buttonChoosePdf = findViewById(R.id.buttonqq1);
        if (isCartEmpty) {
            // Отключаем кнопку, если корзина пустая
            buttonChoosePdf.setEnabled(false);
            buttonChoosePdf.setAlpha(0.5f);
            buttonChoosePdf.setClickable(false);
            buttonChoosePdf.setFocusable(false);
            buttonChoosePdf.setOnClickListener(null);
            buttonChoosePdf.setOnTouchListener((v, event) -> true); // Блокирует любые касания
        } else {
            // Включаем кнопку, если корзина не пустая
            buttonChoosePdf.setEnabled(true);
            buttonChoosePdf.setAlpha(1.0f);
            buttonChoosePdf.setClickable(true);
            buttonChoosePdf.setFocusable(true);
            buttonChoosePdf.setOnTouchListener(null);
        }
        if (isCartEmpty) {
            googlePayButton.setEnabled(false);
            googlePayButton.setAlpha(0.5f);
            googlePayButton.setClickable(false);
            googlePayButton.setFocusable(false);
            googlePayButton.setOnClickListener(null);
            googlePayButton.setOnTouchListener((v, event) -> true); // Полностью блокирует касания

        } else {
            googlePayButton.setEnabled(true);
            googlePayButton.setAlpha(1.0f);
            googlePayButton.setClickable(true);
            googlePayButton.setFocusable(true);
            googlePayButton.setOnTouchListener(null);


        }



    }


    private void loadCartProducts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            cartRef = FirebaseDatabase.getInstance().getReference().child("CartProduct").child(currentUser.getUid());
        } else {
            return;
        }

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartProductsList.clear();
                double totalAmount = 0;

                for (DataSnapshot cartSnapshot : dataSnapshot.getChildren()) {
                    CartProduct cartProduct = cartSnapshot.getValue(CartProduct.class);
                    if (cartProduct != null) {
                        cartProductsList.add(cartProduct);
                        totalAmount += (cartProduct.getProductPrice() * cartProduct.getQuantity());
                    }
                }

                totalAmountTextView.setText(String.format("Общая сумма: %.2f тг", totalAmount));
                cartAdapter.notifyDataSetChanged();
                checkCartState(); // Вызов после загрузки данных

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CartActivity.this, "Ошибка загрузки корзины", Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHARE_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            // Получите данные из Intent.ACTION_SEND
            handleSharedPdf(data);
        }
    }

    private void handleSharedPdf(Intent intent) {
        // Ваш код для обработки данных, полученных через "поделиться"
        // Например, извлечение URI файла и выполнение действий с ним
        Uri sharedPdfUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        if (sharedPdfUri != null) {
            // Обработайте URI PDF
            handlePdfUri(sharedPdfUri);
        }
    }
    private void handlePdfUri(Uri pdfUri) {
        // Здесь вы можете выполнить дополнительные действия с полученным URI
;
        savePdfToDatabase(pdfUri);
        if (isShareActionAvailable(pdfUri)) {
            // Если "Поделиться" доступно, открываем диалог "Поделиться"

            // Отображаем путь к файлу в TextView
            TextView pdfPathTextView = findViewById(R.id.textView);
            pdfPathTextView.setText(pdfUri.getPath());
        } else {
            // Если "Поделиться" недоступно, вы можете выполнить другие действия
        }

    }

    private void savePdfToDatabase(Uri pdfUri) {
        // Ваш код для сохранения URI PDF в базе данных
        // Например, использование Firebase Realtime Database

        // Пример (псевдокод):
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference pdfStorageRef = storageRef.child("pdfs").child(pdfUri.getLastPathSegment());

        pdfStorageRef.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Файл успешно загружен
                    // Получите URI загруженного файла и сохраните его в базе данных
                    pdfStorageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        Button buttonCheckout = findViewById(R.id.buttonCheckout);
                        buttonCheckout.setEnabled(true);
                        buttonCheckout.setAlpha(1.0f);
                        buttonCheckout.setOnClickListener(v -> placeOrder(uri.toString()));
                        // uri - URI загруженного файла
                    });
                })
                .addOnFailureListener(exception -> {
                    // Обработка ошибок при загрузке файла
                    Toast.makeText(CartActivity.this, "Ошибка при загрузке PDF", Toast.LENGTH_SHORT).show();
                });

    }

    private boolean isShareActionAvailable(Uri pdfUri) {
        // Ваш код для проверки, доступно ли действие "Поделиться" для данного URI
        // Например, проверка типа MIME или других параметров файла

        // Пример (псевдокод):
        String mimeType = getContentResolver().getType(pdfUri);
        return "application/pdf".equals(mimeType);
    }



    private void pickPDFFile() {
        try {
            Uri uri = Uri.parse("kaspi://pay/transfer?recipient=77470614872");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("kz.kaspi.mobile");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Каспи не установлен!", Toast.LENGTH_SHORT).show();
            redirectToPlayStore();
        }
    }

    private void redirectToPlayStore() {
        // Открываем магазин Google Play с страницей Каспи Мобайл
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=kz.kaspi.mobile")));
        } catch (ActivityNotFoundException e) {
            // Если магазин Google Play не установлен, открываем веб-версию
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=kz.kaspi.mobile")));
        }
    }

    private void placeOrder(String pdfDownloadUrl) {
        if (cartProductsList.isEmpty()) {
            Toast.makeText(CartActivity.this, "Нет продуктов для заказа", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orderclass");
        String orderId = ordersRef.push().getKey();

        for (CartProduct cartProduct : cartProductsList) {
            cartProduct.setStatus("Placed");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(CartActivity.this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentUserName = dataSnapshot.child("name").getValue(String.class);
                    String currentUserAddress = dataSnapshot.child("address").getValue(String.class);
                    String currentUserPhone = dataSnapshot.child("phonenumber").getValue(String.class);

                    Random random = new Random();
                    int clientCode = 1000 + random.nextInt(9000);

                    Set<String> sellerIds = new HashSet<>();
                    for (CartProduct product : cartProductsList) {
                        sellerIds.add(product.getSellerId());
                    }
                    String sellerIdString = TextUtils.join(",", sellerIds);

                    // 🟢 Добавляем текущее время как дату заказа
// Получаем текущую дату
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String currentDate = dateFormat.format(new Date());

                    Orderclass order = new Orderclass(orderId, currentUserName, currentUserAddress, currentUserPhone,
                            cartProductsList, getTotalAmount(), selectedDeliveryMethod, currentUser.getUid(),
                            String.valueOf(clientCode), sellerIdString,currentDate);

                    order.setStatus("Ожидаемый");
                    order.setUserName(currentUserName);
                    order.setPhoneNumber(currentUserPhone);
                    order.setUserAddress(currentUserAddress);
                    order.setPdfUri(pdfDownloadUrl);
                    order.setOrderDate(currentDate); // 🟢 Устанавливаем дату заказа

                    ordersRef.child(orderId).setValue(order)
                            .addOnSuccessListener(aVoid -> {
                                clearCart();
                                Toast.makeText(CartActivity.this, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(CartActivity.this, "Ошибка оформления заказа", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(CartActivity.this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CartActivity.this, "Ошибка доступа к данным пользователя", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Закрывает текущую активность
    }



}

