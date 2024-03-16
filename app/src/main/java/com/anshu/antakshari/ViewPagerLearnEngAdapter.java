package com.anshu.antakshari;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
public class ViewPagerLearnEngAdapter extends FragmentPagerAdapter {
    public ViewPagerLearnEngAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {
        if(i==0)
            return new FactsFragment();
        else if(i==1)
            return new JokesFragment();
        else
            return new QuotesFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position==0)
            return "Facts";
        else if(position==1)
            return "Jokes";
        else
            return "Quotes";
    }
}
