package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class fragment_pending_orders extends Fragment {

    private RecyclerView recyclerView;
    private SellerOrdersAdapter sellerOrdersAdapter;
    private List<Orderclass> pendingOrdersList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Определяем разметку для фрагмента
        View view = inflater.inflate(R.layout.fragment_pending_orders, container, false);

        // Инициализируем RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewPendingOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Инициализируем адаптер и подключаем его к RecyclerView
        sellerOrdersAdapter = new SellerOrdersAdapter(pendingOrdersList);
        recyclerView.setAdapter(sellerOrdersAdapter);


        // Загружаем заказы
        loadPendingOrders();

        return view;
    }

    // Метод для загрузки ожидающих заказов из Firebase
    private void loadPendingOrders() {
        // Получаем текущий идентификатор продавца
        String currentSellerId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Используем текущий пользовательский ID для продавца

        // Ссылка на базу данных
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orderclass");

        // Слушатель изменений в базе данных
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingOrdersList.clear();

                // Проходим по всем заказам
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    Orderclass order = orderSnapshot.getValue(Orderclass.class);
                    if (order != null
                            && ("Ожидаемый".equals(order.getStatus()) || "Заказ принят".equals(order.getStatus()))) {

                        // Получаем строку с sellerId
                        String sellerIdString = order.getSellerIdString();

                        if (sellerIdString != null && !sellerIdString.isEmpty()) {
                            // Разбиваем строку на массив sellerId
                            String[] sellerIds = sellerIdString.split(",");

                            // Проверяем, если текущий sellerId присутствует в списке
                            for (String sellerId : sellerIds) {
                                if (currentSellerId.equals(sellerId)) {
                                    pendingOrdersList.add(order);
                                    break;  // Прерываем цикл, так как нашли совпадение
                                }
                            }
                        } else {
                            // Обработка случая, когда sellerIdString пустой или null
                            Log.e("Error", "Seller ID string is null or empty");
                        }
                    }
                }

                // Обновляем адаптер
                sellerOrdersAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок
            }
        });
    }

}
