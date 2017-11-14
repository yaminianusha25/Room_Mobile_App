package com.example.dips.smartscheduler;

/**
 * Created by DipS on 4/8/2016.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter1 extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter1(FragmentManager fm, int NumOfTabs) {
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
                ViewTask tab2 = new ViewTask();
                return tab2;
            /*case 2:
                Tab3 tab3 = new Tab3();
                return tab3;*/
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}