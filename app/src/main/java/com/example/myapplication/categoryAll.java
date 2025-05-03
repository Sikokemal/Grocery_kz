package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class categoryAll extends AppCompatActivity {
    private CategoryAdapter2 categoryAdapter;
    private List<Category> categoryList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_category);
        // Получение RecyclerView из макета
        RecyclerView recyclerViewCategories = findViewById(R.id.categoryRecyclerView);

// Создание и установка LayoutManager для RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2); // 2 столбца
        recyclerViewCategories.setLayoutManager(layoutManager);

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter2(this, categoryList);
        recyclerViewCategories.setAdapter(categoryAdapter);

        ImageView imageVectorOne = findViewById(R.id.imageVectorOne);
        imageVectorOne.setOnClickListener(view -> onBackPressed());


        loadCategories();
// Создание адаптера для RecyclerView


    }

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
}