package com.example.dips.smartscheduler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CreateAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        ((TextView)findViewById(R.id.phoneText)).setText(tMgr.getLine1Number());
    }

    public void CreateAccount(View v) {
        //getdata
        String pNum = "";
        String pass = "";
        String passCon = "";
        String fName = "";
        String lName = "";
        String email = "";
        pNum = ((TextView) findViewById(R.id.phoneText)).getText().toString();
        pass = ((TextView) findViewById(R.id.passText)).getText().toString();
        passCon = ((TextView) findViewById(R.id.passConText)).getText().toString();
        fName = ((TextView) findViewById(R.id.fNameText)).getText().toString();
        lName = ((TextView) findViewById(R.id.lNameText)).getText().toString();
        email = ((TextView) findViewById(R.id.emailText)).getText().toString();

        //validate Data
        //Removes all non numbers from phonenumber
        int foo = pNum.length();
        //check phonenumber length
        if (pNum.length() < 1) {
            Toast.makeText(this, "Phone number can not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        pNum = pNum.replaceAll("[^\\d.]", "");
        ((TextView) findViewById(R.id.phoneText)).setText(pNum);
        if(foo < pNum.length()) {
            Toast.makeText(this, "Phone number changed to " + pNum, Toast.LENGTH_SHORT).show();
            return;
        }
        //check name
        if (fName == null || fName.length() < 1) {
            Toast.makeText(this, "Name can not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pass == null || pass.length()<1){
            Toast.makeText(this, "Passwords can not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        //check if password match
        if (!pass.equals(passCon)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        new CreateAccountDB(this).execute(new String[] {pNum, pass, fName, lName, email}, null, null);
    }

    public void Results(){
        Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show();

        //passing intent
        Intent intent = new Intent(this, LogIn.class);
        startActivity(intent);
    }
}

class CreateAccountDB extends AsyncTask<String[], String, Integer> {

    CreateAccount context;
    ProgressDialog progDailog;

    CreateAccountDB(CreateAccount context) {
        this.context = context;
    }

    protected void onPreExecute(){
        progDailog = ProgressDialog.show(context, "Connecting To Database",
                "....please wait....", true);
    }

    protected Integer doInBackground(String[]... strings) {
        InputStream is = null;
        String sqlCall = "INSERT INTO `UserTable`(`phoneNumber`, `password`, `firstName`, " +
                "`lastName`, `email`) VALUES ('" +
                strings[0][0] + "','" +
                strings[0][1] + "','" +
                strings[0][2] + "','" +
                strings[0][3] + "','" +
                strings[0][4] +
                "')";
        try{
            //POST the sql command you want
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("sqlCode",sqlCall));
            HttpPost httppost = new HttpPost("http://androidiit.x10host.com/GenericInsert.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        }catch(Exception e){
            Log.e("OurDB", "CreateAccountDB Error in http connection " + e.toString());
            return 1;
        }
        //convert response to string
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            String line = reader.readLine();
            Log.i("OurDB", line);
            String foo;
            while( (foo = reader.readLine()) != null){

                Log.i("OurDB", foo);
            }
            if(line.equals("true")){
                Log.i("OurDB", "CreateAccountDB created account");
                is.close();
                return 0;
            }else {
                Log.i("OurDB", "CreateAccountDB failed to insert user probably exists already");
                is.close();
                return 3;
            }
        }catch(Exception e){
            Log.e("OurDB", "CreateAccountDB Error converting result " + e.toString());
            return 2;
        }
    }

    protected void onPostExecute(Integer integer)
    {
        try {
            progDailog.dismiss();
            switch (integer) {
                case 0:
                    context.Results();
                    break;
                case 1:
                    Toast.makeText(context, "Failed To Access The Internet. Check Connection", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(context, "Error at external Database. try again", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(context, "User Already Exist", Toast.LENGTH_SHORT).show();
                    break;
            }
        }catch (Exception e){

        }
    }
}
