package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

// Ваш адаптер (ProductAdapter)
public class ProductAdapterall2 extends RecyclerView.Adapter<ProductAdapterall2.ViewHolder> {
    private List<Product> productList;
    private Context context;
    private FirebaseAuth auth; // добавлено
    private List<Product> filteredProducts; // Список продуктов, отфильтрованный по поисковому запросу


    public ProductAdapterall2(Context context, List<Product> products) {
        this.productList = products;
        this.context = context;

        this.filteredProducts = new ArrayList<>(products); // Инициализируем отфильтрованный список копией оригинала

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardviewall, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        this.auth = FirebaseAuth.getInstance(); // добавлено

        holder.textViewProductName.setText(product.getName());
        holder.textViewProductPrice.setText(String.valueOf(product.getPrice()) + " ₸");
        holder.productRatingBar.setRating(product.getAverageRating());
        holder.textViewRatingCount.setText("(" + product.getTotalRatings() + ")");


// Добавление слушателя нажатия на элемент списка
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Создание Intent для перехода в другую активити
                Intent intent = new Intent(context, EditProductActivity.class);

                // Передача данных о продукте в Intent
                intent.putExtra("productId", product.getProductId());
                intent.putExtra("getImageUrls", product.getImageUrls().toArray(new String[0]));
                intent.putExtra("description", product.getDescription());
                Log.d("IntentDebug", "Unit: " + product.getUnit());
                intent.putExtra("unit", product.getUnit());
                intent.putExtra("productName", product.getName());
                intent.putExtra("productPrice", product.getPrice());
                intent.putExtra("orderCount", product.getOrderCount());


                // Запуск новой активити
                context.startActivity(intent);
            }
        });
        // Здесь вы можете использовать библиотеку Glide для загрузки изображения в ImageView
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrls().get(0)) // Берем первое изображение
                .into(holder.imageViewProduct);
    }
    // Метод для установки нового списка продуктов и обновления отфильтрованного списка
    public void setProducts(List<Product> products) {
        this.productList = products;
        filterProducts(""); // Фильтруем по пустому запросу, чтобы показать все продукты
    }

    // Метод для фильтрации продуктов по поисковому запросу
    public void filterProducts(String query) {
        filteredProducts.clear();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredProducts.add(product);
            }
        }
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ViewHolder для хранения ссылок на компоненты
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewProductName;
        TextView textViewProductPrice;
        ImageView imageViewProduct;
        RatingBar productRatingBar;
        TextView textViewRatingCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            productRatingBar = itemView.findViewById(R.id.prRatingBar);
            textViewRatingCount = itemView.findViewById(R.id.textViewRatingCount);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
        }
    }
}

