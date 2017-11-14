package com.example.dips.smartscheduler;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
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

/**
 * Created by Jorge on 4/15/2016.
 */
class SyncDB extends AsyncTask<SQLiteDatabase, String, Integer> {

    MainActivity context;
    ProgressDialog progDailog;

    SyncDB(MainActivity context) {
        this.context = context;
    }

    protected void onPreExecute(){
        progDailog = ProgressDialog.show(context, "Connecting To Database",
                "....please wait....", true);
    }

    protected Integer doInBackground(SQLiteDatabase... db) {

        InputStream is = null;
        String result = "";


        //SYNC USERTABLE
        try{
            //POST the sql command you want
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("sqlCode","SELECT * FROM  `UserTable`"));
            HttpPost httppost = new HttpPost("http://androidiit.x10host.com/Generic.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        }catch(Exception e){
            Log.e("OurDB", "SYNC USERTABLE Error in http connection " + e.toString());
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
            Log.e("OurDB", "SYNC USERTABLE Error converting result " + e.toString());
            return 2;
        }
        //parse json data
        try{
            //Log.e("OurDB", "Data from site "+result);
            JSONArray jArray = new JSONArray(result);
            //empty userTable
            db[0].execSQL("DELETE FROM UserTable");
            db[0].execSQL("VACUUM");
            //write to sqlite
            for(int i=0;i<jArray.length();i++){
                JSONObject json_data = jArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("phoneNumber", json_data.getString("phoneNumber"));
                values.put("password", json_data.getString("password"));
                values.put("firstName", json_data.getString("firstName"));
                values.put("lastName", json_data.getString("lastName"));
                values.put("email", json_data.getString("email"));
                // Inserting Row
                db[0].insert("UserTable", null, values);
            }
        }catch(Exception e){
            Log.e("OurDB", "SYNC USERTABLE Error parsing data " + e.toString());
            return 3;
        }


        //SYNC EventTable
        try{
            //POST the sql command you want
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("sqlCode","SELECT * FROM EventTable"));
            HttpPost httppost = new HttpPost("http://androidiit.x10host.com/Generic.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        }catch(Exception e){
            Log.e("OurDB", "SYNC EventTable Error in http connection " + e.toString());
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
            Log.e("OurDB", "SYNC EventTable Error converting result " + e.toString());
            return 2;
        }
        //parse json data
        try{
            //Log.e("OurDB", "Data from site "+result);
            JSONArray jArray = new JSONArray(result);
            //empty userTable
            db[0].execSQL("DELETE FROM EventTable");
            db[0].execSQL("VACUUM");
            //write to sqlite
            for(int i=0;i<jArray.length();i++){
                JSONObject json_data = jArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("eventID", json_data.getString("eventID"));
                values.put("eventTitle", json_data.getString("eventTitle"));
                values.put("eventDescription", json_data.getString("eventDescription"));
                values.put("dueDate", json_data.getString("dueDate"));
                values.put("startDate", json_data.getString("startDate"));
                values.put("comment", json_data.getString("comment"));
                if(!json_data.getString( "completedDate").equals("")){
                    values.put("completedDate", json_data.getString("completedDate"));
                }
                values.put("completedBy", json_data.getString("completedBy"));
                // Inserting Row
                db[0].insert("EventTable", null, values);
            }
        }catch(Exception e){
            Log.e("OurDB", "SYNC EventTable Error parsing data " + e.toString());
            return 3;
        }


        //SYNC EventPictureTable
        try{
            //POST the sql command you want
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("sqlCode","SELECT * FROM EventPictureTable"));
            HttpPost httppost = new HttpPost("http://androidiit.x10host.com/Generic.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        }catch(Exception e){
            Log.e("OurDB", "SYNC EventPictureTable Error in http connection " + e.toString());
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
            Log.e("OurDB", "SYNC EventPictureTable Error converting result " + e.toString());
            return 2;
        }
        //parse json data
        try{
            //Log.e("OurDB", "Data from site "+result);
            JSONArray jArray = new JSONArray(result);
            //empty userTable
            db[0].execSQL("DELETE FROM EventPictureTable");
            db[0].execSQL("VACUUM");
            //write to sqlite
            for(int i=0;i<jArray.length();i++){
                JSONObject json_data = jArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("eventID", json_data.getString("eventID"));
                values.put("picture", json_data.getString("picture"));
                // Inserting Row
                db[0].insert("EventPictureTable", null, values);
            }
        }catch(Exception e){
            Log.e("OurDB", "SYNC EventPictureTable Error parsing data " + e.toString());
            return 3;
        }


        //SYNC EventUserTable
        try{
            //POST the sql command you want
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("sqlCode","SELECT * FROM EventUserTable"));
            HttpPost httppost = new HttpPost("http://androidiit.x10host.com/Generic.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        }catch(Exception e){
            Log.e("OurDB", "SYNC EventUserTable Error in http connection " + e.toString());
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
            Log.e("OurDB", "SYNC EventUserTable Error converting result " + e.toString());
            return 2;
        }
        //parse json data
        try{
            //Log.e("OurDB", "Data from site "+result);
            JSONArray jArray = new JSONArray(result);
            //empty userTable
            db[0].execSQL("DELETE FROM EventUserTable");
            db[0].execSQL("VACUUM");
            //write to sqlite
            for(int i=0;i<jArray.length();i++){
                JSONObject json_data = jArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put("eventID", json_data.getString("eventID"));
                values.put("phoneNumber", json_data.getString("phoneNumber"));
                // Inserting Row
                db[0].insert("EventUserTable", null, values);
            }
        }catch(Exception e){
            Log.e("OurDB", "SYNC EventUserTable Error parsing data " + e.toString());
            return 3;
        }




        Log.i("OurDB", "SYNC complete");
        db[0].close(); // Closing database connection
        return 0;
    }

    protected void onPostExecute(Integer integer)
    {
        try {
            progDailog.dismiss();
            switch (integer) {
                case 0:
                    context.onSyncComplete();
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
            }
        }catch (Exception e){

        }
    }
}

