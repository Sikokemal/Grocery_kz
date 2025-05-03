package com.example.myapplication;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class UserOrdersPagerAdapter extends FragmentPagerAdapter {

    public UserOrdersPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new UserOrdersFragment();
        } else {
            return new Completed_user_order();
        }
    }

    @Override
    public int getCount() {
        return 2; // Всего два фрагмента
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
