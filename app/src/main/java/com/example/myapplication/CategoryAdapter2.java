package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CategoryAdapter2 extends RecyclerView.Adapter<CategoryAdapter2.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;

    public CategoryAdapter2(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item2, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        // Установка данных категории в элементы макета
        holder.textViewCategoryName.setText(category.getCategoryName());
        // Дополнительно: Используйте Glide или другую библиотеку для загрузки изображений
        Glide.with(context).load(category.getCategoryIconUrl()).into(holder.imageViewCategoryIcon);
        // Обработчик клика по элементу списка
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductCategoryList.class);
            intent.putExtra("categoryName", category.getCategoryName()); // Передача данных
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCategoryName;
        ImageView imageViewCategoryIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoryName = itemView.findViewById(R.id.textViewCategoryName);
            imageViewCategoryIcon = itemView.findViewById(R.id.imageViewCategoryIcon);
        }
    }
}
