package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
// ImageFragment.java
public class ImageFragment extends Fragment {
    private String imageUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);

        // Загрузите изображение из базы данных или другого источника
        // используя imageUrl
        Glide.with(this).load(imageUrl).into(imageView);

        // Обработка нажатия на изображение для открытия нового окна
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код для открытия нового окна
                // например, переход на новую активность
                // или отображение дополнительной информации
                // по выбранному изображению
            }
        });

        return view;
    }

    public static ImageFragment newInstance(String imageUrl) {
        ImageFragment fragment = new ImageFragment();
        fragment.imageUrl = imageUrl;
        return fragment;
    }
}
