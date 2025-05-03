// UserOrdersAdapter.java

package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UserOrdersAdapter extends RecyclerView.Adapter<UserOrdersAdapter.ViewHolder> {

    private List<Orderclass> userOrdersList;
    private Context context;

    public UserOrdersAdapter(List<Orderclass> userOrdersList) {
        this.userOrdersList = userOrdersList;
         this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Orderclass order = userOrdersList.get(position);
        Button completeButton = holder.itemView.findViewById(R.id.completeButton);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обновите статус заказа на "Completed"

                // Создаем кастомный диалог
                final Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.custom_dialog);
                dialog.setCancelable(false);

                // Получаем ссылки на элементы
                TextView clientCodeTextView = dialog.findViewById(R.id.textViewClientCode);
                AppCompatButton btnOK = dialog.findViewById(R.id.btnOk);

                // Создаем сообщение с кодом клиента
                String clientCodeMessage = "Код клиента: " + order.getClientCode();
                clientCodeTextView.setText(clientCodeMessage);

                // Устанавливаем обработчик для кнопки OK
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         dialog.dismiss();
                    }
                });

                // Показываем диалог
                dialog.show();
            }
        });

        // Настройка вложенного RecyclerView для отображения списка продуктов
        CartProductsAdapter cartProductsAdapter = new CartProductsAdapter(order.getCartProductsList());
        holder.cartProductsRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.cartProductsRecyclerView.setAdapter(cartProductsAdapter);
        // Изначально скрываем список продуктов
        holder.cartProductsRecyclerView.setVisibility(View.GONE);
        holder.list.setVisibility(View.GONE);

        // Обработка нажатия на кнопку "Посмотреть детали"
        holder.detailsButton.setOnClickListener(v -> {
            if (holder.cartProductsRecyclerView.getVisibility() == View.GONE) {
                holder.cartProductsRecyclerView.setVisibility(View.VISIBLE);
                holder.list.setVisibility(View.VISIBLE);
                holder.detailsButton.setText("Скрыть детали");
            } else {
                holder.cartProductsRecyclerView.setVisibility(View.GONE);
                holder.list.setVisibility(View.VISIBLE);
                holder.detailsButton.setText("Посмотреть детали");
            }
        });

        // Здесь устанавливаем данные заказа в соответствующие элементы интерфейса
        holder.textViewOrderDate.setText("Дата заказа: " + order.getOrderDate());
        holder.totalAmountTextView.setText("Общая сумма: " + order.getTotalAmount()+" тг");
        holder.deliveryMethodTextView.setText("Способ доставки: " + order.getSelectedDeliveryMethod());
        holder.statusTextView.setText("Статус: " + order.getStatus());
// LottieAnimationView для анимации доставки
        LottieAnimationView animationView = holder.itemView.findViewById(R.id.animationView);

        String status = order.getStatus();

        switch (status) {
            case "Ожидаемый":
                animationView.setVisibility(View.VISIBLE);
                animationView.setAnimation(R.raw.waiting); // замените на свою анимацию
                animationView.playAnimation();
                break;

            case "Заказ принят":
                animationView.setVisibility(View.VISIBLE);
                animationView.setAnimation(R.raw.delivery); // замените на свою анимацию
                animationView.playAnimation();
                break;

            case "Завершенный":
                animationView.setVisibility(View.VISIBLE);
                animationView.setAnimation(R.raw.waiting); // замените на свою анимацию
                animationView.playAnimation();
                break;


        }

    }

    private void updateOrderStatus(String orderId, String newStatus, String deliveryMethod) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference().child("Orderclass").child(orderId);

        // Обновляем статус
        orderRef.child("status").setValue(newStatus);

        // Если выбрана доставка и новый статус "Completed", обновляем статус доставки
        if ("Доставка".equals(deliveryMethod) && "Completed".equals(newStatus)) {
            orderRef.child("deliveryStatus").setValue("Доставлено");
        }
    }
    @Override
    public int getItemCount() {
        return userOrdersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView orderIdTextView,list;
        TextView totalAmountTextView;
        TextView textViewOrderDate;
        TextView deliveryMethodTextView;
        Button detailsButton;
        RecyclerView cartProductsRecyclerView;
        TextView statusTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            list = itemView.findViewById(R.id.list);

            orderIdTextView = itemView.findViewById(R.id.textViewOrderId);
            totalAmountTextView = itemView.findViewById(R.id.textViewTotalAmount);
            textViewOrderDate = itemView.findViewById(R.id.textViewOrderDate);
            deliveryMethodTextView = itemView.findViewById(R.id.textViewDeliveryMethod);
            statusTextView = itemView.findViewById(R.id.textViewStatus);
            detailsButton = itemView.findViewById(R.id.detailsButton);
            cartProductsRecyclerView = itemView.findViewById(R.id.recyclerViewProducts);
        }
    }
}
