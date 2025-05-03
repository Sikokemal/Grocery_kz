package com.example.myapplication;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SellerOrdersPagerAdapter extends FragmentPagerAdapter {

    private String[] tabTitles = new String[]{"Ожидающие заказы", "Завершенные заказы"};

    public SellerOrdersPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new fragment_pending_orders();  // Фрагмент для ожидающих заказов
            case 1:
                return new CompletedOrdersFragment();  // Фрагмент для завершенных заказов
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;  // Количество вкладок (2 вкладки)
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Ожидающие заказы";
        } else {
            return "Завершенные заказы";
        }
    }
}
