package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductCategoryList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapterall productAdapter;
    private List<Product> productList;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_category_list);

        // Получаем переданную категорию
        categoryName = getIntent().getStringExtra("categoryName");

        // Устанавливаем заголовок
        TextView textView = findViewById(R.id.textViewCategory);
        textView.setText(categoryName);

        // Настройка RecyclerView
        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        productAdapter = new ProductAdapterall(this, productList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 столбца
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(productAdapter);

        ImageView imageVectorOne = findViewById(R.id.imageVectorOne);
        imageVectorOne.setOnClickListener(view -> onBackPressed());

        // Загрузка товаров по категории
        loadProducts();
    }

    private void loadProducts() {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");
        productsRef.orderByChild("category").equalTo(categoryName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NextActivity", "Ошибка загрузки продуктов: " + error.getMessage());
            }
        });
    }
}
