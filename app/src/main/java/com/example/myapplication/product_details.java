package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.relex.circleindicator.CircleIndicator3;

public class product_details extends AppCompatActivity {
    private DatabaseReference reviewsRef;
    private String currentUserId;
    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private List<review> reviewList = new ArrayList<>();
    private List<users> userList = new ArrayList<>();
    private String currentProductId;
    private FirebaseAuth auth;
    private int cartItemCount = 0;

    private FirebaseUser currentUser;
    Button button;
    private int quantity = 0; // начальное количество
    private ViewPager2 viewPagerImages;
    private List<String> imageUrls = new ArrayList<>();
    private ImageSliderAdapter imageSliderAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_details);
        View quantitySelector = findViewById(R.id.quantitySelector);
        ImageButton buttonMinus = quantitySelector.findViewById(R.id.minusButton);
        ImageButton buttonPlus = quantitySelector.findViewById(R.id.plusButton);

        TextView textViewQuantity = quantitySelector.findViewById(R.id.quantityTextView);
        getProductDetailsFromFirebase();
        updateCartBadge();
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    quantity--;
                    textViewQuantity.setText(String.valueOf(quantity));
                }
            }
        });

        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                textViewQuantity.setText(String.valueOf(quantity));
            }
        });

        // getting reference of ExpandableTextView
        ExpandableTextView expTv = (ExpandableTextView) findViewById(R.id.expand_text_view).findViewById(R.id.expand_text_view);



        NestedScrollView nestedScrollView = findViewById(com.arlib.floatingsearchview.R.id.scroll);
        FloatingActionButton fab = findViewById(R.id.imageViewShoppingCart123);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    // Scrolling down
                    fab.hide();
                } else {
                    // Scrolling up
                    fab.show();
                }
            }
        });

        FloatingActionButton imageViewShoppingCart = findViewById(R.id.imageViewShoppingCart123);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageViewShoppingCart.getLayoutParams();
        imageViewShoppingCart.setLayoutParams(layoutParams);

        imageViewShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    addToCart(currentProductId, quantity, currentUser.getUid());
                } else {
                    // Обработка случая, когда пользователь не аутентифицирован
                }
            }
        });
        imageViewShoppingCart.setLayoutParams(layoutParams);

        imageViewShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    addToCart(currentProductId, quantity, currentUser.getUid());
                } else {

                }
            }
        });



        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerView.setAdapter(reviewAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
// Получение данных о продукте из Intent
        currentProductId = getIntent().getStringExtra("productId");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем интент для перехода на предыдущий экран (или активность)
                Intent intent = new Intent(product_details.this, buyeractivity.class);

                // Запускаем активность с помощью созданного интента
                startActivity(intent);

                // Закрываем текущую активность (если требуется)
                finish();
            }
        });


        ImageView imageViewCart = findViewById(R.id.imageViewCart);
        imageViewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработчик клика на изображение корзины
                Intent intent = new Intent(product_details.this, CartActivity.class);
                startActivity(intent);
            }
        });


// Получаем ссылку на базу данных Firebase для отзывов
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference().child("reviews").child(currentProductId);

// Добавляем слушатель для получения данных об отзывах
        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalRatings = 0; // Общее количество оценок
                float averageRating = 0; // Средний рейтинг

                // Перебираем все отзывы для вычисления общего количества оценок и суммы всех рейтингов
                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    review review = reviewSnapshot.getValue(review.class);
                    if (review != null) {
                        totalRatings++;
                        averageRating += review.getRating();
                    }
                }

                // Вычисляем средний рейтинг, предварительно проверив, что общее количество оценок больше 0
                if (totalRatings > 0) {
                    averageRating /= totalRatings;
                }

                // Отображаем средний рейтинг и количество оценок на вашем макете интерфейса
                RatingBar ratingBar = findViewById(R.id.productRatingBar);
                ratingBar.setRating(averageRating); // Устанавливаем средний рейтинг

                TextView ratingCountTextView = findViewById(R.id.ratingCountTextView);
                ratingCountTextView.setText("(" + totalRatings + ")");
                updateProductRatingsInDatabase(currentProductId, averageRating, totalRatings);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибки при загрузке отзывов
                Log.e("ReviewActivity", "Error loading reviews: " + databaseError.getMessage());
            }
        });


        // Получение данных о продукте из Intent
        currentProductId = getIntent().getStringExtra("productId");

        // Загрузка отзывов из базы данных
        loadReviewsFromFirebase();

        Button submitButton = findViewById(R.id.buttonSubmitReview);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText reviewEditText = findViewById(R.id.editTextComment);
                String reviewText = reviewEditText.getText().toString();

                RatingBar ratingBar = findViewById(R.id.ratingBar);
                float rating = ratingBar.getRating();

                if (!TextUtils.isEmpty(reviewText)) {
                    if (currentProductId != null && currentUser != null) {
                        if (currentUser.getEmail() != null) {
                            // Получаем данные пользователя из базы данных
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                                    .child("users")
                                    .child(currentUser.getUid());

                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        users user = dataSnapshot.getValue(users.class);
                                        String avatarUrl = user != null ? user.getAvatarUrl() : "";
                                        String name = user != null ? user.getname() : "";

                                         // Добавляем отзыв
                                        addReview(currentUser.getUid(), currentUser.getEmail(), currentProductId, reviewText, rating, avatarUrl,name);

                                        // Очистка полей
                                        reviewEditText.getText().clear();
                                        ratingBar.setRating(0);
                                    } else {
                                        Log.e("order", "Пользователь не найден в базе данных");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("order", "Ошибка при чтении данных пользователя: " + databaseError.getMessage());
                                }
                            });

                        } else {
                            Log.e("order", "currentUser email is null");
                        }
                    } else {
                        Log.e("order", "currentProductId or currentUser is null");
                    }
                } else {
                    Toast.makeText(product_details.this, "Введите отзыв", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        loadReviewsFromFirebase();
        updateCartBadge();
    }
    private void getProductDetailsFromFirebase() {
        // Получение ссылки на вашу базу данных Firebase
        TextView textViewProductName = findViewById(R.id.textViewProductName111);
        TextView textpr = findViewById(R.id.prices);

// getting reference of ExpandableTextView
        ExpandableTextView expTv = (ExpandableTextView) findViewById(R.id.expand_text_view).findViewById(R.id.expand_text_view);

// calling setText on the ExpandableTextView so that
// text content will be displayed to the user

        // Получение productId из Intent
        String productId = getIntent().getStringExtra("productId");
        String displayName = getIntent().getStringExtra("productName");
        String description = getIntent().getStringExtra("description");
        double price = getIntent().getDoubleExtra("productPrice", 0.0); // 0.0 - значение по умолчанию
        String unit = getIntent().getStringExtra("unit");

        textViewProductName.setText(displayName);
        expTv.setText(description);
        String priceText = price + " ₸/" + unit;
        textpr.setText(priceText);
        // Получаем массив строк из Intent
        String[] imageUrls = getIntent().getStringArrayExtra("getImageUrls");

        if (imageUrls != null && imageUrls.length > 0) {
            // Преобразуем массив в список
            List<String> imageList = Arrays.asList(imageUrls);

            CircleIndicator3 indicator = findViewById(R.id.indicator);

// Используем ViewPager2
            // Используем ViewPager2
            ViewPager2 viewPagerImages = findViewById(R.id.viewPagerImages);
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(this, imageList);
            viewPagerImages.setAdapter(imageSliderAdapter);
            indicator.setViewPager(viewPagerImages);


        } else {
         }


    }



    private void loadReviewsFromFirebase() {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference().child("reviews").child(currentProductId);

        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviewList.clear();

                for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                    review review = reviewSnapshot.getValue(review.class);
                    if (review != null) {
                        reviewList.add(review);
                    }
                }

                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ReviewActivity", "Error loading reviews: " + databaseError.getMessage());
            }
        });
    }


    private void addToCart(String productId, int quantity,String userId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Если пользователь не аутентифицирован, решите, что делать в этом случае
            // Например, можно перенаправить пользователя на экран входа
            return;
        }

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("CartProduct").child(currentUser.getUid());

        // Получение данных о продукте из Intent
        String productName = getIntent().getStringExtra("productName");
        String sellerid = getIntent().getStringExtra("sellerid");
        String unit = getIntent().getStringExtra("unit");

        String[] imageUrls = getIntent().getStringArrayExtra("getImageUrls");

// Берем первое изображение
        String firstImageUrl = (imageUrls != null && imageUrls.length > 0) ? imageUrls[0] : null;

        double productPrice = getIntent().getDoubleExtra("productPrice", 0.0);

        CartProduct cartProduct = new CartProduct(firstImageUrl,currentUserId, productId, quantity, productName, productPrice, "Pending",sellerid,unit);
        cartRef.child(productId).setValue(cartProduct);

         cartItemCount++;
        // Обновляем бейдж
        updateCartBadge();

        // Обновление счетчика заказов для продукта в базе данных
        // Получаем ссылку на продукты
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");

// Создаем запрос к базе данных Firebase с условием, что значение поля "productId" равно заданному productId
        Query query = productsRef.orderByChild("productId").equalTo(productId);

// Добавляем слушатель для получения данных о продукте
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    // Получаем объект продукта
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        // Получаем текущее значение счетчика заказов
                        int orderCount = product.getOrderCount();
                        Log.d("order", "Current order count: " + orderCount); // Добавляем отладочную информацию

                        // Обновляем счетчик заказов
                        productSnapshot.getRef().child("orderCount").setValue(orderCount + 1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                     }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                     }
                                });
                    } else {
                        // Если продукт не найден в базе данных, выводим сообщение об ошибке
                        Log.e("order", "Product not found in database");
                     }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("order", "Error updating order count: " + databaseError.getMessage());
            }
        });


    }



    private void updateCartBadge() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("CartProduct").child(currentUser.getUid());
            cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Set<String> uniqueProductIds = new HashSet<>();

                    for (DataSnapshot cartSnapshot : dataSnapshot.getChildren()) {
                        CartProduct cartProduct = cartSnapshot.getValue(CartProduct.class);
                        if (cartProduct != null) {
                            uniqueProductIds.add(cartProduct.getProductId());
                        }
                    }

                    // Устанавливаем количество уникальных продуктов в бейдж
                    NotificationBadge badge = findViewById(R.id.badge);
                    badge.setNumber(uniqueProductIds.size());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("order", "Error loading cart products: " + databaseError.getMessage());
                }
            });
        }
    }

    private void addReview(String userId, String userEmail, String currentProductId, String reviewText, float rating,String avatarUrl,String name) {
        Log.d("order", "Adding review with userEmail: " + userEmail);

        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference().child("reviews").child(currentProductId);
        String reviewId = reviewsRef.push().getKey();
        review review = new review(userId, userEmail, currentProductId, reviewText, rating,avatarUrl,name);
        reviewsRef.child(reviewId).setValue(review);
    }
    // Метод для обновления среднего рейтинга и общего количества отзывов в базе данных
    private void updateProductRatingsInDatabase(String productId, float averageRating, int totalRatings) {
        // Получаем ссылку на продукты
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");

        // Создаем запрос к базе данных Firebase с условием, что значение поля "productId" равно заданному productId
        Query query = productsRef.orderByChild("productId").equalTo(productId);

        // Добавляем слушатель для получения данных о продукте
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    // Обновляем значения рейтинга и количества отзывов
                    productSnapshot.getRef().child("averageRating").setValue(averageRating);
                    productSnapshot.getRef().child("totalRatings").setValue(totalRatings);

                  }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при обновлении данных
                Log.e("order", "Error updating product ratings: " + databaseError.getMessage());}
        });
    }


}
