package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;

public class OnboardingFragment extends Fragment {
    private static final String ARG_ANIM = "anim";
    private static final String ARG_TITLE = "title";

    public static OnboardingFragment newInstance(int animRes, String title) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ANIM, animRes);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);

        LottieAnimationView animationView = view.findViewById(R.id.lottieAnim);
        TextView textView = view.findViewById(R.id.textTitle);

        if (getArguments() != null) {
            animationView.setAnimation(getArguments().getInt(ARG_ANIM));
            textView.setText(getArguments().getString(ARG_TITLE));
        }

        return view;
    }
}
