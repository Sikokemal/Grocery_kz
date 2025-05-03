package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.List;

public class UserCompletedOrdersAdapter extends RecyclerView.Adapter<UserCompletedOrdersAdapter.ViewHolder> {

    private List<Orderclass> orders;
    private Context context;

    public UserCompletedOrdersAdapter(List<Orderclass> orders, Context context) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_completed_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Orderclass order = orders.get(position);

        holder.textViewOrderId.setText("Дата заказа: " + order.getOrderDate());
        holder.textViewTotalAmount.setText("Общая сумма: " + order.getTotalAmount()+" тг");
        holder.textViewDeliveryMethod.setText("Способ доставки: " + order.getSelectedDeliveryMethod());
        holder.textViewStatus.setText("Статус: " + order.getStatus());

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
                holder.list.setVisibility(View.GONE);
                holder.detailsButton.setText("Посмотреть детали");
            }
        });
        if (order.getStatus().equalsIgnoreCase("Завершенный")) {
            holder.animationView2.setVisibility(View.VISIBLE);
            holder.animationView2.setAnimation(R.raw.load1);
            holder.animationView2.playAnimation();
        } else {
            holder.animationView2.setVisibility(View.GONE);
            holder.animationView2.cancelAnimation();
        }


    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LottieAnimationView animationView2;

        TextView textViewOrderId, textViewTotalAmount, textViewDeliveryMethod,list, textViewStatus;
        Button detailsButton;
        RecyclerView cartProductsRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewOrderId = itemView.findViewById(R.id.textViewOrderId);
            list = itemView.findViewById(R.id.list);
            textViewTotalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            textViewDeliveryMethod = itemView.findViewById(R.id.textViewDeliveryMethod);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            detailsButton = itemView.findViewById(R.id.detailsButton);
            cartProductsRecyclerView = itemView.findViewById(R.id.recyclerViewProducts);
            animationView2 = itemView.findViewById(R.id.animationView2); // добавлено

        }
    }
}
