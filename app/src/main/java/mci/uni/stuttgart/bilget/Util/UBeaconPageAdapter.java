package mci.uni.stuttgart.bilget.Util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import mci.uni.stuttgart.bilget.scan.ScanListFragment;
import mci.uni.stuttgart.bilget.search.SearchListFragment;

public class UBeaconPageAdapter extends FragmentPagerAdapter {

    ScanListFragment scanFragment;
    SearchListFragment searchFragment;

    public UBeaconPageAdapter(FragmentManager fm) {
        super(fm);
        scanFragment = new ScanListFragment();
        searchFragment = new SearchListFragment();
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return scanFragment;
            case 1:
                return searchFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
}
