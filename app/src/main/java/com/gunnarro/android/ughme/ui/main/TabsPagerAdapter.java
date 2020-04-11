package com.gunnarro.android.ughme.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.ui.fragment.BarChartFragment;
import com.gunnarro.android.ughme.ui.fragment.LocationFragment;
import com.gunnarro.android.ughme.ui.fragment.SmsFragment;

/**
 * FragmentPagerAdapter - Use this when navigating between a fixed, small number of sibling screens.
 * FragmentStatePagerAdapter - Use this when paging across an unknown number of pages.
 */
class TabsPagerAdapter extends FragmentPagerAdapter {
    @StringRes
    private static final int[] TAB_TITLES =
            new int[]{R.string.tab_title_sms, R.string.tab_title_location, R.string.tab_title_chart};

    private final Context mContext;

    public TabsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return SmsFragment.newInstance(null, null);
            case 1:
                return LocationFragment.newInstance(null, null);
            case 2:
                return BarChartFragment.newInstance();
            default:
                return SmsFragment.newInstance(null, null);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}
