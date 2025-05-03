// NewCartAdapter.java
package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class NewCartAdapter extends RecyclerView.Adapter<NewCartAdapter.NewCartViewHolder> {
    private CartProduct.OnQuantityChangedListener onQuantityChangedListener;
    private TextView totalAmountTextView;
    private OnRemoveProductClickListener removeProductClickListener;

    public void setRemoveProductClickListener(OnRemoveProductClickListener listener) {
        this.removeProductClickListener = listener;
    }

    public NewCartAdapter(List<CartProduct> cartProductList, TextView totalAmountTextView) {
        this.cartProductList = cartProductList;
        this.totalAmountTextView = totalAmountTextView;
    }
    public interface OnRemoveProductClickListener {
        void onRemoveProductClick(int position);
    }

    private List<CartProduct> cartProductList;

    public NewCartAdapter(List<CartProduct> cartProductList) {
        this.cartProductList = cartProductList;
    }

    @NonNull
    @Override
    public NewCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_cart_product_item, parent, false);
        return new NewCartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewCartViewHolder holder, int position) {
        CartProduct cartProduct = cartProductList.get(position);

        holder.bind(cartProduct);
        holder.removeButton.setOnClickListener(v -> {
            if (removeProductClickListener != null) {
                removeProductClickListener.onRemoveProductClick(position);
            }
        });

        holder.quantityTextView1.setText(String.valueOf(cartProduct.getQuantity()));
        holder.quantityMinusButton.setOnClickListener(v -> {
            int quantity = cartProduct.getQuantity();
            if (quantity > 1) {
                cartProduct.setQuantity(quantity - 1);
                notifyItemChanged(position);
                if (onQuantityChangedListener != null) {
                    onQuantityChangedListener.onQuantityChanged(quantity - 1);
                }
                updateTotalAmount(); // Обновление общей суммы после уменьшения количества продукта
            }
        });

        holder.quantityPlusButton.setOnClickListener(v -> {
            int quantity = cartProduct.getQuantity();
            cartProduct.setQuantity(quantity + 1);
            notifyItemChanged(position);
            if (onQuantityChangedListener != null) {
                onQuantityChangedListener.onQuantityChanged(quantity + 1);
            }
            updateTotalAmount(); // Обновление общей суммы после увеличения количества продукта
        });


        // Загрузка и отображение изображения продукта
        Glide.with(holder.itemView.getContext())
                .load(cartProduct.getUrl()) // Замените это на URL изображения продукта из вашей базы данных
                .into(holder.productImageView);
    }
    private void updateTotalAmount() {
        double totalAmount = 0;
        for (CartProduct cartProduct : cartProductList) {
            totalAmount += cartProduct.getProductPrice() * cartProduct.getQuantity();
        }
        totalAmountTextView.setText(String.format("Общая сумма: %.2f ₸", totalAmount));
    }
    public void removeItem(int position) {
        cartProductList.remove(position); // Удаляем из списка
        notifyItemRemoved(position); // Сообщаем адаптеру об изменении
    }



    @Override
    public int getItemCount() {
        return cartProductList.size();
    }

    public class NewCartViewHolder extends RecyclerView.ViewHolder {

        private TextView productNameTextView1;
        private TextView quantityTextView1;
        private TextView priceTextView1;
        private Button quantityMinusButton;
        private Button removeButton;
        private Button quantityPlusButton;
        private ImageView productImageView;

        public NewCartViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView1 = itemView.findViewById(R.id.textViewProductName1);
            quantityTextView1 = itemView.findViewById(R.id.quantityTextView1);
            priceTextView1 = itemView.findViewById(R.id.textViewProductPrice1);
            quantityMinusButton = itemView.findViewById(R.id.quantity_minus); // ID кнопки "Минус"
            quantityPlusButton = itemView.findViewById(R.id.quantity_plus); // ID кнопки "Плюс"
            removeButton = itemView.findViewById(R.id.cart_delete); // ID кнопки "Плюс"
            productImageView = itemView.findViewById(R.id.product_image); // Инициализируйте ImageView
        }

        public void bind(CartProduct cartProduct) {
            productNameTextView1.setText(cartProduct.getProductName());
            quantityTextView1.setText(String.format(Locale.getDefault(), "Количество: %d", cartProduct.getQuantity()));
            priceTextView1.setText(String.format(Locale.getDefault(), "Цена: %.2f ₸", cartProduct.getProductPrice()));
        }
    }
}
