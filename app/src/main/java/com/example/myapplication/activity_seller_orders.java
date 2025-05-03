package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class activity_seller_orders extends AppCompatActivity {
    private ViewPager viewPager;
    private SellerOrdersPagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SellerOrdersAdapter sellerOrdersAdapter;
    private List<Orderclass> pendingOrdersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_orders);
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new SellerOrdersPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        // Загрузка ожидающих заказов
     }

    private void loadPendingOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orderclass");

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingOrdersList.clear();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    Orderclass order = orderSnapshot.getValue(Orderclass.class);
                    if (order != null && ("Pending".equals(order.getStatus()) || "In Progress".equals(order.getStatus()))) {
                        pendingOrdersList.add(order);
                    }
                }

                sellerOrdersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SellerOrdersActivity", "Error loading pending orders: " + databaseError.getMessage());
                Toast.makeText(activity_seller_orders.this, "Error loading orders", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateOrderStatus(String orderId, String newStatus, String deliveryMethod) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference().child("Orderclass").child(orderId);

        // Обновляем статус
        orderRef.child("status").setValue(newStatus);

        // Если выбрана доставка, обновляем статус доставки
        if ("Доставка".equals(deliveryMethod)) {
            orderRef.child("deliveryStatus").setValue("В пути");
        }
    }
}
