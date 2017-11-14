package com.example.dips.smartscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.provider.ContactsContract;

import java.util.GregorianCalendar;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FAB onclick
        findViewById(R.id.refreshFloat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncDB();
            }
        });
        findViewById(R.id.addFloat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CreateTask.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LogIn.class);
                startActivity(intent);
            }
        });



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("Your Task");
        //setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Pending Task"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed Task"));


        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        TaskPagerAdapter adapter = new TaskPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        //set alert to trigger daily
        Intent i = new Intent(this, AlertReceiver.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(pi); // cancel any existing alarms
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(), 1000 * 60 * 1, pi);
    }

    private void syncDB() {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            new SyncDB(this).execute(dbHelper.getReadableDatabase(), null, null);
        } catch (Exception e) {
            Log.e("SyncDB", "error at SyncDB " + e.toString());
        }

    }

    public void onSyncComplete() {
        Toast.makeText(this, "Sync Complete", Toast.LENGTH_SHORT).show();
        //restart current screen
        finish();
        startActivity(getIntent());
    }

}
