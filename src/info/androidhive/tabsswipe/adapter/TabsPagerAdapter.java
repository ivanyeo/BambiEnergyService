package info.androidhive.tabsswipe.adapter;

import com.ndnxr.bambi.EnergyMeterFragment;
import com.ndnxr.bambi.TaskListFragment;
import com.ndnxr.bambi.AppListFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
	public EnergyMeterFragment energyMeterFragment;
	public TaskListFragment taskListFragment;
	public AppListFragment appListFragment;
 
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
        // energy meter fragment activity
        energyMeterFragment = new EnergyMeterFragment();
        // task list fragment activity
    	taskListFragment = new TaskListFragment();
    	// app list fragment activity
    	appListFragment = new AppListFragment();
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:          
            return energyMeterFragment;
        case 1:          
            return taskListFragment;
        case 2:         
            return appListFragment;
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
 
}
