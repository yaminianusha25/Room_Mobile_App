package com.example.dips.smartscheduler;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.database.Cursor;
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class CompleteTask extends AppCompatActivity {

    private Bitmap curimage;
    private ArrayList imageList = new ArrayList();
    private List<Bitmap> completedImages; // just the ones that were added on this screen
    DatabaseHelper dbhelper;
    private ArrayList phoneNumbers = new ArrayList(); // people that are added
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_task);

        SharedPreferences prefs = getSharedPreferences("Data", MODE_PRIVATE);
        int taskID = prefs.getInt("eventID", 1);

        dbhelper= new DatabaseHelper(this);
        String[] listGroupItems = dbhelper.GetTaskInfo(taskID);
        ((TextView) findViewById(R.id.completeTaskTitle)).setText(listGroupItems[0]);
        ((TextView) findViewById(R.id.completeTaskDesc)).setText(listGroupItems[1]);
        ((TextView) findViewById(R.id.completeTaskDue)).setText(listGroupItems[2]);

        //get todays date
        Calendar calendar = Calendar.getInstance();
        String finishDate = (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR);

        ((TextView) findViewById(R.id.completeTaskFinish)).setText(finishDate);

        //GET PICTURES
        List<Bitmap> images = dbhelper.GetImages(taskID);
        for (Bitmap image : images) {
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(image);
            iv.setPadding(10, 10, 10, 10);
            ((LinearLayout) findViewById(R.id.completeTaskImageLayout)).addView(iv);
        }
    }

    public void UploadImage(View view) {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                curimage = null;
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    curimage = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);

                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        curimage.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {

                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                curimage = (BitmapFactory.decodeFile(picturePath));
                Log.w("CompleteTask", picturePath + "");
            }
            //if image is selected
            if (curimage != null) {
                curimage = Bitmap.createScaledBitmap(curimage, 500, 500, true);
                ImageView iv = new ImageView(this);
                iv.setImageBitmap(curimage);
                iv.setPadding(10, 10, 10, 10);
                imageList.add(curimage);
                ((LinearLayout) findViewById(R.id.completeTaskImageLayout)).addView(iv);
            }

        }
    }

     public void CompleteTest(View view) {
        SharedPreferences prefs = getSharedPreferences("Data", MODE_PRIVATE);
        String comments = ((TextView) findViewById(R.id.completeTaskComments)).getText().toString();
        int taskID = prefs.getInt("eventID", 1);
        String phone = prefs.getString("phoneNumber", "");
        //date
        Calendar calendar = Calendar.getInstance();
        String completeDate = (calendar.get(Calendar.MONTH) + 1) + "/" +
                calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR);

        //getphoneNumbers for event
        phoneNumbers= (ArrayList) dbhelper.getEventPhoneNumbers(taskID);
        for(int i=0;i<phoneNumbers.size();i++){
            Log.d("|PhoneNumbers| ", String.valueOf(phoneNumbers.get(i)));
        }
        sendSMS();
        new CompleteTaskDB(this, imageList).execute(new String[]{comments, completeDate, phone, taskID + ""}, null, null);
    }

    private void sendSMS() {
        Log.i("Send SMS", "");
      //  String phoneNo = phoneNumber;
   //     String taskName = title;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (int i = 0; i < phoneNumbers.size(); i++) {
                smsManager.sendTextMessage(String.valueOf(phoneNumbers.get(i)), null, "Hi, " + phoneNumbers.get(i) + ". The Task that you were added to is finished. '"
                        , null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Results() {
        Toast.makeText(this, "Task Completed", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}

class CompleteTaskDB extends AsyncTask<String[], String, Integer> {

    CompleteTask context;
    ProgressDialog progDailog;
    ArrayList imageList;


    CompleteTaskDB(CompleteTask context, ArrayList il) {
        this.context = context;
        imageList = il;
    }

    protected void onPreExecute() {
        progDailog = ProgressDialog.show(context, "Connecting To Database",
                "....please wait....", true);
    }

    protected Integer doInBackground(String[]... strings) {
        /*
        eventTitle,eventDescription, duedate, startdate
         */
        InputStream is = null;
        String sqlCall = "UPDATE `EventTable` SET `comment`= '" +
                strings[0][0] + "'" +
                ",`completedDate`= '" +
                strings[0][1] + "'" +
                ",`completedBy`= '" +
                strings[0][2] + "'" +
                "WHERE eventID = '" +
                strings[0][3] + "'";
        try {
            //POST the sql command you want
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("sqlCode", sqlCall));
            HttpPost httppost = new HttpPost("http://androidiit.x10host.com/GenericInsert.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            Log.e("OurDB", "CompleteTaskDB Error in http connection " + e.toString());
            return 1;
        }
        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            String line = reader.readLine();
            Log.i("OurDB", line);
            if (line.equals("true")) {
                Log.i("OurDB", "CompleteTaskDB updateds event");
            } else {
                Log.i("OurDB", "CompleteTaskDB" + line);
                is.close();
                return 3;
            }
        } catch (Exception e) {
            Log.e("OurDB", "CompleteTaskDB Error converting result " + e.toString());
            return 2;
        }

        //EVENT PICTURES
        for (int i = 0; i < imageList.size(); i++) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ((Bitmap)imageList.get(i)).compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            String pictureAsString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

            sqlCall = "INSERT INTO `EventPictureTable`(`eventID`, `picture`) VALUES ('" +
                    strings[0][3] + "','" +
                    pictureAsString +
                    "')";

            try {
                //POST the sql command you want
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
                nameValuePairs.add(new BasicNameValuePair("sqlCode", sqlCall));
                HttpPost httppost = new HttpPost("http://androidiit.x10host.com/GenericInsert.php");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            } catch (Exception e) {
                Log.e("OurDB", "CompleteTaskDB Error in http connection " + e.toString());
                return 1;
            }
            //convert response to string
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                if (reader.readLine().equals("true")) {
                    Log.i("OurDB", "CompleteTaskDB added a image");
                } else {
                    Log.i("OurDB", "CompleteTaskDB failed to insert user probably exists already");
                    is.close();
                    return 3;
                }
            } catch (Exception e) {
                Log.e("OurDB", "CompleteTaskDB Error converting result " + e.toString());
                return 2;
            }
        }

        try {
            is.close();
        } catch (Exception e) {

        }
        //finally finished
        return 0;
    }

    protected void onPostExecute(Integer integer) {
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
                    Toast.makeText(context, "Error at external Database. try again", Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {

        }
    }
}
