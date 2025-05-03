package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class buyeractivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewall;
    private boolean isDescending = true;

    private RecyclerView recyclerView2;
    private ProductAdapter productAdapter;
    private ProductAdapterall productAdapterall;
    private ImageButton btnSearch;
    private long backPressedTime;
    private Toast backToast;
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private List<Product> productList;
    private List<Product> productList2;
    private FrameLayout frameSearchResults;
    private DatabaseReference databaseReference;

    private SearchView searchView;

    private static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyeractivity);
        updateCartBadge();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("products"); // "products" - ваш узел базы данных

        // Найдите ImageView по id
        ImageView imgArrow = findViewById(R.id.imgArrow);

        // Установите обработчик кликов
        imgArrow.setOnClickListener(view -> {
            // Создайте Intent для перехода к следующему активити
            Intent intent = new Intent(buyeractivity.this, categoryAll.class);
            // Запустите активити
            startActivity(intent);
        });

        // Чтение данных из базы данных
         frameSearchResults = findViewById(R.id.frameSearchResults);

        // Initialize RecyclerView for categories
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList);
        recyclerViewCategories.setAdapter(categoryAdapter);

        // ... (other code)

        loadCategories();
        searchView = findViewById(R.id.searchView1);
        searchView.setQueryHint("Введите текст для поиска");

        // Настройка слушателя изменения текста поиска
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Обработка отправки поискового запроса
                // Можно выполнять поиск или обрабатывать запрос здесь
                if (frameSearchResults.getVisibility() == View.VISIBLE) {
                    // Если окно результатов поиска уже отображается, применяем фильтрацию и сортировку
                    filterProducts(query, "price", true);  // сортировка по цене по убыванию
                } else {
                    // Иначе показываем результаты поиска
                    showSearchResults();
                }

                return true;
            }



            @Override
            public boolean onQueryTextChange(String newText) {
                // Обработка изменения текста поиска
                filterProducts(newText, "",true); // Передача пустой строки вторым аргументом
                return true;
            }
        });
// Устанавливаем слушателя для события фокуса на SearchView
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // SearchView получил фокус, выполняем необходимые действия
                    // Например, показать определенный UI или обработать фокус
                }
            }
        });


        // Устанавливаем слушателя для события закрытия поиска
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Обработка закрытия поиска (нажатие на кнопку "х")
                // Можете добавить здесь свой код
                frameSearchResults.setVisibility(View.GONE);

                return false;
            }
        });

        btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showSearchResults();
                // Установить фокус и открыть клавиатуру для searchView

                searchView.setIconifiedByDefault(true);
                searchView.setFocusable(true);
                searchView.setIconified(false);
                searchView.requestFocusFromTouch();
            }
        });


        // Инициализация Firebase Database
        FirebaseDatabase database1 = FirebaseDatabase.getInstance();
        databaseReference = database1.getReference("BannerAd"); // "banners" - ваш узел базы данных с баннерами

        // Добавление данных о баннерах в базу данных
        readBannerDataFromDatabase();

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerViewall = findViewById(R.id.recyclerViewProducts2);

        recyclerView2 = findViewById(R.id.recyclerViewResults);

        int numberOfColumns = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        Toolbar toolbar = findViewById(R.id.toolbarToolbar);
        setSupportActionBar(toolbar);
        // Установка адаптера для первого RecyclerView (горизонтальный)
        productList = new ArrayList<>(); // Заполните этот список данными
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(productAdapter);
        recyclerView.setAdapter(productAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

// Установка адаптера для второго RecyclerView (вертикальный с 2 столбцами)
        productList2 = new ArrayList<>(); // Заполните этот список данными
        productAdapterall = new ProductAdapterall(this, productList2);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 столбца
        recyclerViewall.setLayoutManager(gridLayoutManager);
        recyclerViewall.setAdapter(productAdapterall);
        recyclerView2.setAdapter(productAdapterall);

        // Установка GridLayoutManager с 2 столбцами

        RecyclerView recyclerView = findViewById(R.id.banner);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager1);

// Создаем PagerSnapHelper и привязываем его к RecyclerView
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recyclerView);

        ImageView imageShare = findViewById(R.id.imageShare);
        imageShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Показать диалоговое окно с вариантами сортировки
                showSortOptionsDialog();
            }
        });


        ImageView imageViewCart = findViewById(R.id.imageViewCart);
        imageViewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработчик клика на изображение корзины
                Intent intent = new Intent(buyeractivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
        loadProducts();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setSelectedItemId(-1);  // Сбросить выбор по умолчанию
        bottomNavigationView.setSelectedItemId(R.id.menu_shop); // Убедитесь, что это не вызывается

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_shop:
                        break;

                    case R.id.menu_cart:
                        Intent profile2Intent = new Intent(buyeractivity.this,UserOrdersActivity.class);
                        profile2Intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(profile2Intent);
                        break;

                    case R.id.menu_profile:

                        Intent profileIntent = new Intent(buyeractivity.this, profile_client.class);
                        profileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(profileIntent);
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
        bottomNavigationView.setSelectedItemId(R.id.menu_shop);
    updateCartBadge();
    }

    // bannnerrr adddd

    private void addBannerDataToDatabase() {
        DatabaseReference bannersRef = FirebaseDatabase.getInstance().getReference().child("BannerAd");

        // Создаем объекты для баннеров и добавляем их в базу данных
        BannerAd banner1 = new BannerAd("https://avatars.mds.yandex.net/get-altay/1705560/2a0000016f4ddd359433fd55367b30b13f65/XXL", "1");
        bannersRef.push().setValue(banner1);

        BannerAd banner2 = new BannerAd("https://darwindiose.wordpress.com/wp-content/uploads/2014/10/fruits.jpg", "2");
        bannersRef.push().setValue(banner2);

        BannerAd banner3 = new BannerAd("https://storage.googleapis.com/easygrocery/2018/10/homepage-main-baner.jpg", "3");
        bannersRef.push().setValue(banner3);

        // Добавьте столько баннеров, сколько вам нужно
    }


    private void readBannerDataFromDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<BannerAd> bannerAds = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BannerAd bannerAd = snapshot.getValue(BannerAd.class);
                    bannerAds.add(bannerAd);
                }

                setupBannerRecyclerView(bannerAds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибок при чтении данных из базы данных
            }
        });

    }

    private void setupBannerRecyclerView(List<BannerAd> bannerAds) {
        RecyclerView recyclerViewBanner = findViewById(R.id.banner);
        BannerAdapter bannerAdapter = new BannerAdapter(this, bannerAds);
        recyclerViewBanner.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewBanner.setAdapter(bannerAdapter); // Устанавливаем адаптер для RecyclerView
    }



    private void showSortOptionsDialog() {
        String[] sortOptions = {"По цене", "По популярности", "По названию"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите тип сортировки")
                .setItems(sortOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String query = searchView.getQuery().toString(); // Текущий запрос
                        String sortBy = "";

                        switch (which) {
                            case 0:
                                sortBy = "price";
                                break;
                            case 1:
                                sortBy = "popularity";
                                break;
                            case 2:
                                sortBy = "name";
                                break;
                        }

                        // Передаём направление сортировки
                        filterProducts(query, sortBy, isDescending);

                        // Меняем направление на противоположное
                        isDescending = !isDescending;
                    }
                });

        builder.create().show();
    }


    private void showSearchResults() {
        // Сделайте окно результатов видимым
        frameSearchResults.setVisibility(View.VISIBLE);

        // Убедитесь, что RecyclerView внутри frameSearchResults тоже видим
        RecyclerView recyclerViewResults = findViewById(R.id.recyclerViewResults);
        recyclerViewResults.setVisibility(View.VISIBLE);
        // Установить фокус и открыть клавиатуру для ввода
        searchView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
        // Дополнительная настройка, например, заполнение RecyclerView результатами
        // ...
    }
    private void filterProducts(String query, String sortBy, boolean isDescending) {
        List<Product> filteredList = new ArrayList<>();

        // Фильтрация по запросу
        if (frameSearchResults.getVisibility() == View.VISIBLE) {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }

            // Сортировка
            switch (sortBy) {
                case "name":
                    Collections.sort(filteredList, new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return isDescending
                                    ? p2.getName().compareToIgnoreCase(p1.getName())
                                    : p1.getName().compareToIgnoreCase(p2.getName());
                        }
                    });
                    break;
                case "price":
                    Collections.sort(filteredList, new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return isDescending
                                    ? Double.compare(p2.getPrice(), p1.getPrice())
                                    : Double.compare(p1.getPrice(), p2.getPrice());
                        }
                    });
                    break;
                case "popularity":
                    Collections.sort(filteredList, new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return isDescending
                                    ? Integer.compare(p2.getOrderCount(), p1.getOrderCount())
                                    : Integer.compare(p1.getOrderCount(), p2.getOrderCount());
                        }
                    });
                    break;
                default:
                    // Ничего не делать
                    break;
            }
        }

        // Обновляем адаптер
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                productAdapter.setProducts(filteredList);
                productAdapter.notifyDataSetChanged();
                productAdapterall.setProducts(filteredList);
                productAdapterall.notifyDataSetChanged();
            }
        });
    }





    // ...


    private void loadCategories() {
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference().child("categories");
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryList.clear();

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    if (category != null) {
                        categoryList.add(category);
                    }
                }

                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("BuyerActivity", "Error reading categories from Firebase: " + databaseError.getMessage());
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
    private void loadProducts() {
        productAdapter.notifyDataSetChanged();

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("products");
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                        productList2.add(product);
                    }
                }

                // Уведомляем адаптер об изменении данных только после полной загрузки
                productAdapter.notifyDataSetChanged();
                productAdapterall.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("BuyerActivity", "Error reading products from Firebase: " + databaseError.getMessage());
            }
        });
    }
    public void onBackPressed() {
        // Если прошло меньше 2 секунд с последнего нажатия, выходим из приложения
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null) backToast.cancel(); // Закрываем прошлый Toast
            super.onBackPressed(); // Завершаем активность и выходим
            return;
        } else {
            // Показываем предупреждение
            backToast = Toast.makeText(getBaseContext(), "Нажмите еще раз, чтобы выйти", Toast.LENGTH_SHORT);
            backToast.show();
        }

        // Запоминаем время нажатия
        backPressedTime = System.currentTimeMillis();
    }




}