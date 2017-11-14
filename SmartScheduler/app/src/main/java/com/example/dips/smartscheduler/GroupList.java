package com.example.dips.smartscheduler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GroupList extends Fragment {

    FragmentActivity GrpTabFrgActivity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //fetch phoneNumber through SharedPreferences
        RelativeLayout GrpTabRelLayout    = (RelativeLayout)inflater.inflate(R.layout.activity_group_list, container, false);
        GrpTabFrgActivity = super.getActivity();

        SharedPreferences prefs = GrpTabFrgActivity.getSharedPreferences("Data", 0x0000);

        int phoneNumber = prefs.getInt("phoneNumber", 1);

        //create DatabaseHelper
        DatabaseHelper dbHelper=new DatabaseHelper(this.getContext());

        String[] groupNames=dbHelper.getGroupList(phoneNumber);

        //set NOGROUPDATA text view visibilty property
        TextView txtNoGroup= (TextView)GrpTabRelLayout.findViewById(R.id.txtViewGroupData);
        if(groupNames.length==0){
            txtNoGroup.setVisibility(View.VISIBLE);
        }

        //populate list
        ListView viewListTask=(ListView)GrpTabRelLayout.findViewById(R.id.viewGroupListView);
        List<String> groupList=new ArrayList<>();

        int gLength=groupNames.length;
        //add groupNames to the list
        for(int i=0;i<gLength;i++) {
            groupList.add(groupNames[i]);
        }


        ArrayAdapter tasksAdapter= new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_expandable_list_item_1, groupList);
        viewListTask.setAdapter(tasksAdapter);

        //set on click true
        viewListTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {

                //TODO FILL THIS IN WITH REAL Group DATA
                SharedPreferences.Editor editor = GrpTabFrgActivity.getSharedPreferences("Data", 0x0000).edit();
                //TODO NOTICE THAT WE START COUNTING FROM 1 NOT 0
                editor.putInt("groupID", 1);
                editor.commit();

                Intent intent = new Intent(GrpTabFrgActivity.getApplicationContext(), ViewTask.class);
                startActivity(intent);
            }
        });

        Button NewGroupBtn= (Button) GrpTabRelLayout.findViewById(R.id.btnNewGroup);

        NewGroupBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(GrpTabFrgActivity.getApplicationContext(), CreateGroup.class);
                startActivity(intent);
            }
        });

        return GrpTabRelLayout;
    }


}
