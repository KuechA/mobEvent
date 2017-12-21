package fr.eurecom.Ready2Meet.uiExtensions;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import fr.eurecom.Ready2Meet.AllEvents;
import fr.eurecom.Ready2Meet.DashboardFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch(position) {
            case 0:
                fragment = new DashboardFragment();
                break;
            case 1:
                fragment = new AllEvents();
                break;
            case 2:
                // TODO: My events
            default:
                Log.e("MainPagerAdapter", "Wrong position for fragment: " + position);
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
