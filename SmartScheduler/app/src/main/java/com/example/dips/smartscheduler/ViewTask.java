package com.example.dips.smartscheduler;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ViewTask extends Fragment{

    public static FragmentActivity TaskFragActivity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //fetch phoneNumber through SharedPreferences
        RelativeLayout TaskTabRelLayout = (RelativeLayout) inflater.inflate(R.layout.activity_view_task, container, false);
        TaskFragActivity = super.getActivity();

        SharedPreferences prefs = TaskFragActivity.getSharedPreferences("Data", 0x0000);
        String phoneNumber = prefs.getString("phoneNumber", "1");

        Log.d("Phone Number :", phoneNumber);
        //create DatabaseHelper
        DatabaseHelper dbHelper=new DatabaseHelper(this.getContext());

        final List<String[]> eventDetails = dbHelper.getTaskList(phoneNumber);

        //set NOTASKDATA text view visibilty property
        TextView txtNoGroup= (TextView) TaskTabRelLayout.findViewById(R.id.txtViewTaskData);
        if(eventDetails.size()==0){
            Log.d("Event Size:", String.valueOf(eventDetails.size()));
            txtNoGroup.setVisibility(View.VISIBLE);
        }

        //populate list
        ListView viewListTask=(ListView)TaskTabRelLayout.findViewById(R.id.viewTaskListView);
        List<String> eventList=new ArrayList<>();

        //add eventDetails to the list
        for (int i = 0; i < eventDetails.size(); i++) {
            eventList.add((i+1)+". "+eventDetails.get(i)[1]);
            Log.d("Event Names:", eventList.get(i));
        }

        ArrayAdapter tasksAdapter= new ArrayAdapter<String>(TaskFragActivity.getApplicationContext(), android.R.layout.simple_expandable_list_item_1,android.R.id.text1,eventList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.BLACK);
                return textView;
            }
        };

        viewListTask.setAdapter(tasksAdapter);
        viewListTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {

                SharedPreferences.Editor editor = TaskFragActivity.getSharedPreferences("Data", 0x0000).edit();
                editor.putInt("eventID", Integer.parseInt(eventDetails.get(position)[0]));
                editor.putInt("position", position);
                editor.commit();
                Intent intent = new Intent(TaskFragActivity.getApplicationContext(), ViewSingleTask.class);
                startActivity(intent);
            }
        });
        tasksAdapter.notifyDataSetChanged();
        return TaskTabRelLayout;
    }
}

