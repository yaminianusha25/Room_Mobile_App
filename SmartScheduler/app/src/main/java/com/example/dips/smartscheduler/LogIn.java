package com.example.dips.smartscheduler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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


public class LogIn extends AppCompatActivity {
    EditText phnNumberTxt;
    EditText passTextLogin;
    String phnNumber;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        findViewsById();
        TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        phnNumberTxt.setText(tMgr.getLine1Number());

    }

    private void findViewsById() {
        phnNumberTxt= (EditText) findViewById(R.id.phoneTextLogIn);
        passTextLogin = (EditText) findViewById(R.id.passTextLogin);
    }

    public void LogIn(View v){
        try {
            phnNumber = phnNumberTxt.getText().toString();
            password = passTextLogin.getText().toString();
            String[] credentials = new String[] { phnNumber+"", password};

            SharedPreferences.Editor editor = getSharedPreferences("Data", MODE_PRIVATE).edit();
            editor.putString("phoneNumber", phnNumber);
            editor.commit();

            //check user on external db
            new CheckUser(this).execute(credentials, null, null);

        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"Enter valid Login Credentials",Toast.LENGTH_LONG).show();
        }
    }

    public void Results(boolean login){
        if(login){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this,"User credential is invalid",Toast.LENGTH_SHORT).show();
        }
    }

    public void CreateAccount(View v){
        Intent intent = new Intent(v.getContext(),CreateAccount.class);
        startActivity(intent);
    }

}

class CheckUser extends AsyncTask<String[], String, Integer> {

    LogIn context;
    ProgressDialog progDailog;

    CheckUser(LogIn context) {
        this.context = context;
    }
    protected void onPreExecute(){
        progDailog = ProgressDialog.show(context, "Connecting To Database",
                "....please wait....", true);
    }

    protected Integer doInBackground(String[]... strings) {
        InputStream is = null;
        String result = "";
        try{
            //POST the sql command you want
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("sqlCode","SELECT * FROM UserTable"));
            HttpPost httppost = new HttpPost("http://androidiit.x10host.com/Generic.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        }catch(Exception e){
            Log.e("OurDB", "CheckUser Error in http connection " + e.toString());
            return 1;
        }
        //convert response to string
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result=sb.toString();
        }catch(Exception e){
            Log.e("OurDB", "CheckUser Error converting result " + e.toString());
            return 2;
        }
        //parse json data
        try{
            //Log.e("OurDB", "Data from site "+result);
            JSONArray jArray = new JSONArray(result);
            //write to sqlite
            for(int i=0;i<jArray.length();i++){
                JSONObject json_data = jArray.getJSONObject(i);

                //if login correct
                if(json_data.getString("phoneNumber").equals(strings[0][0]) &&
                        json_data.getString("password").equals(strings[0][1])){
                    Log.i("OurDB", "CheckUser complete found match");
                    return 0;
                }
            }
        }catch(Exception e){
            Log.e("OurDB", "CheckUser Error parsing data " + e.toString());
            return 3;
        }

        Log.i("OurDB", "CheckUser complete no match");
        return 4;
    }

    protected void onPostExecute(Integer integer)
    {
        try {
            progDailog.dismiss();
            switch (integer) {
                case 0:
                    context.Results(true);
                    break;
                case 1:
                    Toast.makeText(context, "Failed To Access The Internet. Check Connection", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(context, "Error at external Database. try again", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(context, "Error Sync data please try again", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    context.Results(false);
                    break;
            }
        }catch (Exception e){

        }
    }
}


