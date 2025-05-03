package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Completed_user_order extends Fragment {
    private RecyclerView recyclerView;
    private List<Orderclass> completedOrdersList = new ArrayList<>();
    private UserCompletedOrdersAdapter completedOrdersAdapter;

    public Completed_user_order() {
        // Пустой конструктор обязателен
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed_orders, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUserOrders1);

        completedOrdersAdapter = new UserCompletedOrdersAdapter(completedOrdersList, getContext());
        recyclerView.setAdapter(completedOrdersAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        loadUserOrders();

        return view;
    }

    private void loadUserOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userOrdersRef = FirebaseDatabase.getInstance().getReference()
                    .child("Orderclass").orderByChild("userId").equalTo(currentUser.getUid()).getRef();
            userOrdersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    completedOrdersList.clear();
                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        Orderclass order = orderSnapshot.getValue(Orderclass.class);
                        if (order != null && "Завершенный".equals(order.getStatus())) {
                            completedOrdersList.add(order);
                        }
                    }
                    completedOrdersAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "Ошибка загрузки заказов: " + databaseError.getMessage());
                }
            });
        }
    }
}
