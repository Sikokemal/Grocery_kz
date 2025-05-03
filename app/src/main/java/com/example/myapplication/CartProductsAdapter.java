package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CartProductsAdapter extends RecyclerView.Adapter<CartProductsAdapter.ProductViewHolder> {

    private List<CartProduct> products;

    public CartProductsAdapter(List<CartProduct> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller_productlist, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        CartProduct product = products.get(position);
        holder.productNameTextView.setText(product.getProductName());
        holder.productQuantityTextView.setText("Количество: " + product.getQuantity()+" кг");
        holder.productPriceTextView.setText(" " + product.getProductPrice()+" ₸/кг");

        // Загрузка и отображение изображения продукта
        Glide.with(holder.itemView.getContext())
                .load(product.getUrl()) // Замените это на URL изображения продукта из вашей базы данных
                .into(holder.productImageView);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView productQuantityTextView;
        TextView productPriceTextView;
        ImageView productImageView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.textViewProductName);
            productQuantityTextView = itemView.findViewById(R.id.quantility);
            productPriceTextView = itemView.findViewById(R.id.textViewProductPrice);
            productImageView = itemView.findViewById(R.id.product_image);
        }
    }
}
