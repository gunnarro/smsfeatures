package com.gunnarro.android.ughme.ui.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.ui.fragment.BackupFragment;
import com.gunnarro.android.ughme.ui.fragment.BarChartFragment;
import com.gunnarro.android.ughme.ui.fragment.SmsSearchFragment;
import com.gunnarro.android.ughme.ui.fragment.WordCloudFragment;

/**
 * FragmentPagerAdapter - Use this when navigating between a fixed, small number of sibling screens.
 * FragmentStatePagerAdapter - Use this when paging across an unknown number of pages.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_title_backup, R.string.tab_title_sms_search, R.string.tab_title_chart, R.string.tab_title_word_cloud};

    private final Context mContext;

    public TabsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return SmsSearchFragment.newInstance(null);
            case 2:
                return BarChartFragment.newInstance(null);
            case 3:
                return WordCloudFragment.newInstance(null);
            default:
                return BackupFragment.newInstance(null);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}
