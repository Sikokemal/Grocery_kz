// UserOrdersFragment.java
package com.example.myapplication;

import android.os.Bundle;
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

public class UserOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserOrdersAdapter userOrdersAdapter;
    private List<Orderclass> userOrdersList = new ArrayList<>();

    public UserOrdersFragment() {
        // Пустой конструктор обязателен
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_orders, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewUserOrders);
        userOrdersAdapter = new UserOrdersAdapter(userOrdersList);
        recyclerView.setAdapter(userOrdersAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Здесь можно вызвать метод загрузки данных, если это необходимо
        loadUserOrders();

        return view;
    }

    // Другие методы фрагмента, если они нужны

    private void loadUserOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userOrdersRef = FirebaseDatabase.getInstance().getReference().child("Orderclass").orderByChild("userId").equalTo(currentUser.getUid()).getRef();
            userOrdersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userOrdersList.clear();
                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        Orderclass order = orderSnapshot.getValue(Orderclass.class);
                        if (order != null && ("Ожидаемый".equals(order.getStatus()) || "Заказ принят".equals(order.getStatus()))) {
                            userOrdersList.add(order);
                        }
                    }

                    userOrdersAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Обработка ошибок, если не удается загрузить заказы пользователя
                }
            });
        }
    }
 }
