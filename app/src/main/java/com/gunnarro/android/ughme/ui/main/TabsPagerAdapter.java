package com.gunnarro.android.ughme.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.ui.fragment.BarChartFragment;
import com.gunnarro.android.ughme.ui.fragment.ListItemFragment;
import com.gunnarro.android.ughme.ui.fragment.LocationFragment;
import com.gunnarro.android.ughme.ui.fragment.SmsFragment;
import com.gunnarro.android.ughme.ui.view.WordCloudFragment;

import java.util.stream.IntStream;

/**
 * FragmentPagerAdapter - Use this when navigating between a fixed, small number of sibling screens.
 * FragmentStatePagerAdapter - Use this when paging across an unknown number of pages.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_title_sms, R.string.tab_title_location, R.string.tab_title_chart, R.string.tab_search_result, R.string.tab_title_word_cloud};

    private final Context mContext;

    TabsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    public static int getTabNumber(int tabId) {
        return IntStream.range(0, TAB_TITLES.length)
                .filter(i -> TAB_TITLES[i] == tabId)
                .findFirst()
                .orElse(-1);
    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return LocationFragment.newInstance(null, null);
            case 2:
                return BarChartFragment.newInstance();
            case 3:
                return ListItemFragment.newInstance("SMS Search Result");
            case 4:
                return WordCloudFragment.newInstance();
            default:
                return SmsFragment.newInstance(null, null);
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
