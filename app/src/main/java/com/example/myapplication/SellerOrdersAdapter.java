package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SellerOrdersAdapter extends RecyclerView.Adapter<SellerOrdersAdapter.ViewHolder> {

    private static List<Orderclass> ordersList;

    public SellerOrdersAdapter(List<Orderclass> ordersList) {
        this.ordersList = ordersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Orderclass order = ordersList.get(position);

        holder.orderIdTextView.setText("Дата заказа: " + order.getOrderDate());
        holder.totalAmountTextView.setText("Общая сумма: " + order.getTotalAmount()+" тг");
        holder.deliveryMethodTextView.setText("Способо доставки: " + order.getSelectedDeliveryMethod());
        holder.status.setText("Статус: " + order.getStatus());

        // Устанавливаем имя и адрес пользователя
        holder.userNameTextView.setText("Имя: " + order.getUserName());
        holder.userAddressTextView.setText("Адресс: " + order.getUserAddress());
        holder.callButton.setText(" " + order.getPhoneNumber());


        // Настройка вложенного RecyclerView для отображения списка продуктов
        CartProductsAdapter cartProductsAdapter = new CartProductsAdapter(order.getCartProductsList());
        holder.cartProductsRecyclerView.setAdapter(cartProductsAdapter);

        holder.completedbutton.setOnClickListener(v -> {
            // Создаем диалог для ввода кода
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle("Complete Order");

            // Добавляем поле для ввода кода
            final EditText input = new EditText(holder.itemView.getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            // Кнопка подтверждения
            builder.setPositiveButton("Submit", (dialog, which) -> {
                String enteredCode = input.getText().toString();

                // Сравниваем введенный код с кодом клиента
                if (enteredCode.equals(order.getClientCode())) {
                    // Если код правильный, обновляем статус заказа
                    updateOrderStatus(order.getOrderId(), "Завершенный");
                    Toast.makeText(holder.itemView.getContext(), "Order completed", Toast.LENGTH_SHORT).show();
                } else {
                    // Неправильный код
                    Toast.makeText(holder.itemView.getContext(), "Incorrect code", Toast.LENGTH_SHORT).show();
                }
            });

            // Кнопка отмены
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        // Начальная видимость кнопок
        holder.openPdfButton.setVisibility(View.VISIBLE);
        holder.acceptButton.setVisibility(View.VISIBLE);
        holder.addressButton.setVisibility(View.GONE);
        holder.callButton.setVisibility(View.GONE);

        // Начальная видимость деталей заказа
        holder.detailsLayout.setVisibility(View.GONE); // Изначально скрываем детали
        holder.toggleDetailsButton.setImageResource(R.drawable.ic_arrowdown); // Стрелка вверх
        // Настройка кнопки для переключения деталей заказа
        holder.toggleDetailsButton.setOnClickListener(v -> {
            if (holder.detailsLayout.getVisibility() == View.GONE) {
                // Показываем детали заказа
                holder.detailsLayout.setVisibility(View.VISIBLE);
                holder.toggleDetailsButton.setImageResource(R.drawable.ic_arrowup); // Стрелка вверх


            } else {
                // Скрываем детали заказа
                holder.detailsLayout.setVisibility(View.GONE);
                holder.toggleDetailsButton.setImageResource(R.drawable.ic_arrowdown); // Стрелка вверх
            }
        });


// LottieAnimationView для анимации доставки
        LottieAnimationView animationView = holder.itemView.findViewById(R.id.animationView);

        String status = order.getStatus();

        switch (status) {
            case "Ожидаемый":
                animationView.setVisibility(View.VISIBLE);
                animationView.setAnimation(R.raw.waiting); // замените на свой файл
                animationView.playAnimation();
                holder.acceptButton.setText("Принять заказ");
                break;

            case "Заказ принят":
                animationView.setVisibility(View.VISIBLE);
                animationView.setAnimation(R.raw.delivery); // замените на свой файл
                animationView.playAnimation();
                holder.acceptButton.setText("Детали заказа");
                break;

            case "Завершенный":
                animationView.setVisibility(View.VISIBLE);
                animationView.setAnimation(R.raw.waiting); // замените на свой файл
                animationView.playAnimation();
                 break;

            default:
                animationView.setVisibility(View.INVISIBLE);
                animationView.cancelAnimation();
                holder.acceptButton.setText("Обработать");
                break;
        }




        holder.acceptButton.setOnClickListener(v -> {
            updateOrderStatus(order.getOrderId(), "Заказ принят");

            // Меняем текст и скрываем кнопку
            holder.acceptButton.setVisibility(View.GONE);
            holder.openPdfButton.setVisibility(View.GONE);
            holder.completedbutton.setVisibility(View.GONE);

            // Показываем другие кнопки
            holder.addressButton.setVisibility(View.VISIBLE);
            holder.callButton.setVisibility(View.VISIBLE);
            holder.backButton.setVisibility(View.VISIBLE);
        });

        // Логика кнопки "Address"
        holder.addressButton.setOnClickListener(v -> {
            String address = order.getUserAddress();
            Uri location = Uri.parse("geo:0,0?q=" + address);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                holder.itemView.getContext().startActivity(mapIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(holder.itemView.getContext(), "No map application found", Toast.LENGTH_SHORT).show();
            }
        });
// Логика для кнопки "Назад"
        holder.backButton.setOnClickListener(v -> {
            // Скрываем кнопки "Address" и "Call"
            holder.addressButton.setVisibility(View.GONE);
            holder.callButton.setVisibility(View.GONE);

            // Показываем кнопки "Open PDF" и "Accept"
            holder.openPdfButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.completedbutton.setVisibility(View.VISIBLE);

            // Скрываем кнопку "Назад"
            holder.backButton.setVisibility(View.GONE);
        });
        // Логика кнопки "Call"
        holder.callButton.setOnClickListener(v -> {
            String phoneNumber = order.getPhoneNumber();
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            holder.itemView.getContext().startActivity(dialIntent);
        });

        // Запускаем анимацию
        animationView.playAnimation();
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView orderIdTextView;
        TextView totalAmountTextView;
        ImageButton toggleDetailsButton,addressButton;
        LinearLayout detailsLayout;
        TextView deliveryMethodTextView;
        TextView status;
        TextView userNameTextView; // Добавлено поле для имени пользователя
        TextView userAddressTextView; // Добавлено поле для адреса пользователя
        Button openPdfButton;
        ImageButton backButton;
        RecyclerView cartProductsRecyclerView;

        Button acceptButton, callButton, completedbutton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.textViewOrderId);
            totalAmountTextView = itemView.findViewById(R.id.textViewTotalAmount);
            deliveryMethodTextView = itemView.findViewById(R.id.textViewDeliveryMethod);
            status = itemView.findViewById(R.id.textViewStatus);
            toggleDetailsButton = itemView.findViewById(R.id.toggleDetailsButton);
            backButton = itemView.findViewById(R.id.backButton);
            detailsLayout = itemView.findViewById(R.id.detailsLayout);
            userNameTextView = itemView.findViewById(R.id.textViewCustomerName); // Инициализация поля имени
            userAddressTextView = itemView.findViewById(R.id.textViewCustomerAddress); // Инициализация поля адреса
            openPdfButton = itemView.findViewById(R.id.openPdfButton);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            completedbutton = itemView.findViewById(R.id.completeButton);
            addressButton = itemView.findViewById(R.id.addressButton); // В layout item добавьте id для этой кнопки
            callButton = itemView.findViewById(R.id.callButton);
            cartProductsRecyclerView = itemView.findViewById(R.id.cartProductsRecyclerView);
            // Установка LayoutManager для вложенного RecyclerView
            cartProductsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            openPdfButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pdfUri = ordersList.get(getAdapterPosition()).getPdfUri();

                    // Проверяем, что pdfUri не равен null
                    if (pdfUri != null && !pdfUri.isEmpty()) {
                        openPdfExternal(pdfUri);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Оплата")
                                .setMessage("Оплата произведена через Google Pay")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                }

                private void openPdfExternal(String pdfUri) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(pdfUri), "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    try {
                        itemView.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // Если нет подходящего приложения для просмотра PDF, выведите сообщение об ошибке
                    }
                }
            });
        }
    }

    private void updateOrderStatus(String orderId, String newStatus) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference().child("Orderclass").child(orderId);
        orderRef.child("status").setValue(newStatus);
    }
}