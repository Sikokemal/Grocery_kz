package com.example.myapplication;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class search extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private SearchResultAdapter adapter;
    private List<SearchResultItem> resultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);

        resultList = new ArrayList<>();
        adapter = new SearchResultAdapter(resultList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Настройка слушателя для SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Обработка запроса поиска и обновление списка результатов
                // В данном случае просто добавим фиктивный результат
                resultList.add(new SearchResultItem("Результат поиска: " + query));
                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Обработка изменения текста в SearchView
                // Можете добавить функциональность для отображения подсказок или фильтрации результатов
                return true;
            }
        });
    }
}