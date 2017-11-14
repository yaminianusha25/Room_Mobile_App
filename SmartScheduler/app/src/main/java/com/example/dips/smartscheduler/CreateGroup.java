package com.example.dips.smartscheduler;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class CreateGroup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        //populate dropdowns
        List<String> listFriendItems = new ArrayList<>();
        listFriendItems.add("Veeresh");
        listFriendItems.add("Jorge");
        listFriendItems.add("Yamini");
        listFriendItems.add("Harsh");
        //view
        Spinner dropdown = (Spinner)findViewById(R.id.dropDownViewName);
        ArrayAdapter<String> adapterGroupName = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listFriendItems);
        dropdown.setAdapter(adapterGroupName);
        //post
        dropdown = (Spinner)findViewById(R.id.dropDownPostName);
        adapterGroupName = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listFriendItems);
        dropdown.setAdapter(adapterGroupName);
    }

    public void CreateGroup (View view){
        Intent intent = new Intent(getApplicationContext(),GroupList.class);
        startActivity(intent);
    }
}
