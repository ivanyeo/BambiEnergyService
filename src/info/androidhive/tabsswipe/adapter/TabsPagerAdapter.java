package info.androidhive.tabsswipe.adapter;

import com.ndnxr.bambi.EnergyMeterFragment;
import com.ndnxr.bambi.TaskListFragment;
import com.ndnxr.bambi.AppListFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
 
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // Top Rated fragment activity
            return new EnergyMeterFragment();
        case 1:
            // Games fragment activity
            return new TaskListFragment();
        case 2:
            // Movies fragment activity
            return new AppListFragment();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
 
}
