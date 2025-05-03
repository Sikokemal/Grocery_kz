package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private List<SearchResultItem> resultList;

    public SearchResultAdapter(List<SearchResultItem> resultList) {
        this.resultList = resultList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultItem item = resultList.get(position);
        holder.itemNameTextView.setText(item.getItemName());
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
