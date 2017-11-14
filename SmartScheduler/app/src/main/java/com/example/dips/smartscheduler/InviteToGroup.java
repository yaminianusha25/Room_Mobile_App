package com.example.dips.smartscheduler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class InviteToGroup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_to_group);

        //populate dropdowns people
        List<String> listFriendItems = new ArrayList<>();
        listFriendItems.add("Veeresh");
        listFriendItems.add("Jorge");
        listFriendItems.add("Yamini");
        listFriendItems.add("Harsh");
        Spinner dropdown = (Spinner)findViewById(R.id.dropDownPerson);
        ArrayAdapter<String> adapterGroupName = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listFriendItems);
        dropdown.setAdapter(adapterGroupName);
        //
        listFriendItems = new ArrayList<>();
        listFriendItems.add("Group 1");
        listFriendItems.add("Group 2");
        listFriendItems.add("Group 3");
        dropdown = (Spinner)findViewById(R.id.dropDownGroup);
        adapterGroupName = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listFriendItems);
        dropdown.setAdapter(adapterGroupName);

    }

    public void InviteToGroup (View view){
        Intent intent = new Intent(getApplicationContext(), GroupList.class);
        startActivity(intent);
    }
}
