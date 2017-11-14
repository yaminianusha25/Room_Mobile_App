package com.example.dips.smartscheduler;

/**
 * Created by DipS on 4/8/2016.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TaskPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public TaskPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            //case 0:
            //GroupList tab1 = new GroupList();
            //return tab1;
            case 0:
                ViewTask tab1 = new ViewTask();
                return tab1;

            case 1:
                CompleteTaskList tab2 = new CompleteTaskList();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}