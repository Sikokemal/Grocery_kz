package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class OnboardingAdapter extends FragmentStateAdapter {
    private final List<OnboardingItem> items;


    public OnboardingAdapter(@NonNull OnboardingActivity fa, List<OnboardingItem> items) {
        super(fa);
        this.items = items;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        OnboardingItem item = items.get(position);
        return OnboardingFragment.newInstance(item.getAnimationResId(), item.getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
