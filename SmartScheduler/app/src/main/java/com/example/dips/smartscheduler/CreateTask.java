package com.example.dips.smartscheduler;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateTask extends AppCompatActivity {

    private DatePicker datePicker;
    private Calendar calendar;
    private int year, month, day;
    private int yearNew, monthNew, dayNew;
    private Bitmap curimage;
    private ArrayList imageList = new ArrayList();
    private ArrayList phoneNumbers = new ArrayList(); // people that are added
    private ArrayList people = new ArrayList(); // people that are added UI Only

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        //get todays date and put in select date text
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = (calendar.get(Calendar.MONTH) + 1);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month, day);
    }

    //used for Select Date
    @SuppressWarnings("deprecation")
    public void GetDate(View view) {
        showDialog(999);
    }

    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year, month - 1, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // arg1 = year
            // arg2 = month
            // arg3 = day
            showDate(arg1, arg2 + 1, arg3);
        }
    };

    private void showDate(int year, int month, int day) {
        yearNew = year;
        monthNew = month;
        dayNew = day;
        ((TextView) findViewById(R.id.createTaskDate)).setText(new StringBuilder().append(month).append("/")
                .append(day).append("/").append(year));
    }
    //end of Select Date

    //start of image upload
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
            switch (requestCode) {
                case 1:
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
                    break;
                case 2:
                    Uri selectedImage = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    String picturePath = c.getString(columnIndex);
                    c.close();
                    curimage = (BitmapFactory.decodeFile(picturePath));
                    Log.i("CompleteTask", picturePath + "");
                    break;
                case 3://contacts
                    Uri contactUri = data.getData();
                    String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER};
                    Cursor cursor = getContentResolver()
                            .query(contactUri, projection, null, null, null);
                    cursor.moveToFirst();

                    // Retrieve the phone number from the NUMBER column
                    int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String number = cursor.getString(column);
                    int indexName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    String name = cursor.getString(indexName);
                    number = number.replaceAll("[^\\d.]", "");
                    if (phoneNumbers.contains(number)) {
                        Toast.makeText(CreateTask.this, "Phone Number Already Added", Toast.LENGTH_SHORT).show();
                        Log.i("CompleteTask", "phonenumber already added" + number);
                        break;
                    }

                    people.add(name + " " + number);
                    phoneNumbers.add(number);
                    //set phonenumber UI

                    Spinner s = (Spinner) findViewById(R.id.spinnerCreateTask);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, people);
                    s.setAdapter(adapter);


                    //TextView tv = new TextView(this);
                    //tv.setTextColor(Color.parseColor("#000000"));
                    //tv.setText(number);
                    //((LinearLayout)findViewById(R.id.peopleLayout)).addView(tv);
                    Log.i("CompleteTask", "added phonenumber" + number);
                    break;
            }
            //if image is selected
            if (curimage != null) {
                curimage = Bitmap.createScaledBitmap(curimage, 500, 500, true);
                ImageView iv = new ImageView(this);
                iv.setImageBitmap(curimage);
                iv.setPadding(10, 10, 10, 10);
                imageList.add(curimage);
                ((LinearLayout) findViewById(R.id.createTaskImageLayout)).addView(iv);
            }
        }
    }
    //end of image upload

    public void CreateTask(View view) {
        String title = "";
        String desc = "";
        String date = "";
        String startdate = month + "/" + day + "/" + year;

        title = ((TextView) findViewById(R.id.createTaskName)).getText().toString();
        desc = ((TextView) findViewById(R.id.createTaskDesc)).getText().toString();
        date = ((TextView) findViewById(R.id.createTaskDate)).getText().toString();

        if (title.equals("")) {
            Toast.makeText(this, "Title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (desc.equals("")) {
            Toast.makeText(this, "Description can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(yearNew >= year && monthNew >= month && dayNew >= day)) {
            Toast.makeText(this, "Invalid complete by date", Toast.LENGTH_SHORT).show();
            return;
        }

        //get current group team members and fill spinner
        SharedPreferences prefs = getSharedPreferences("Data", MODE_PRIVATE);
        phoneNumbers.add(prefs.getString("phoneNumber", "0"));

        if(((Switch) findViewById(R.id.switch1)).isChecked())
            sendSMS(title, prefs.getString("phoneNumber", "0"));

        //CREATE EVENT IN DB
        new CreateTaskDB(this, phoneNumbers, imageList).execute(new String[]{title, desc, date, startdate}, null, null);
    }

    public void AddPeople(View v) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
        startActivityForResult(pickContactIntent, 3);
    }

    public void Results() {
        Log.i("OurDB", "CreateTaskDB created full event");
        Toast.makeText(this, "Task Created!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

    private void sendSMS(String title, String phoneNumber) {
        Log.i("Send SMS", "");
        String phoneNo = phoneNumber;
        String taskName = title;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (int i = 0; i < phoneNumbers.size(); i++) {
                smsManager.sendTextMessage(String.valueOf(phoneNumbers.get(i)), null, "Hi, " + people.get(i) + ". You were added to a new Task '"
                        + taskName + "'. Download SmartTask to see more!", null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class CreateTaskDB extends AsyncTask<String[], String, Integer> {

    CreateTask context;
    ProgressDialog progDailog;
    ArrayList phoneNumbers;
    ArrayList imageList;


    CreateTaskDB(CreateTask context, ArrayList pNum, ArrayList il) {
        this.context = context;
        phoneNumbers = pNum;
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
        String sqlCall = "INSERT INTO `EventTable`(`eventTitle`, `eventDescription`, " +
                "`dueDate`, `startDate`) VALUES ('" +
                strings[0][0] + "','" +
                strings[0][1] + "','" +
                strings[0][2] + "','" +
                strings[0][3] +
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
            Log.e("OurDB", "CreateTaskDB Error in http connection " + e.toString());
            return 1;
        }
        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            String line = reader.readLine();
            Log.i("OurDB", line);
            if (line.equals("true")) {
                Log.i("OurDB", "CreateTaskDB created event");
            } else {
                Log.i("OurDB", "CreateTaskDB" + line);
                is.close();
                return 3;
            }
        } catch (Exception e) {
            Log.e("OurDB", "CreateTaskDB Error converting result " + e.toString());
            return 2;
        }

        //GET EVENTID
        String eventID = "0";
        String result;
        try {
            //POST the sql command you want
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("sqlCode", "SELECT MAX( eventID ) FROM EventTable"));
            HttpPost httppost = new HttpPost("http://androidiit.x10host.com/Generic.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            Log.e("OurDB", "CreateTaskDB Error in http connection " + e.toString());
            return 1;
        }
        //convert response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.e("OurDB", "CreateTaskDB Error converting result " + e.toString());
            return 2;
        }
        //parse json data
        try {
            //Log.e("OurDB", "Data from site "+result);
            JSONArray jArray = new JSONArray(result);
            //write to sqlite
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject json_data = jArray.getJSONObject(i);

                eventID = json_data.getString("MAX( eventID )");
                Log.i("OurDB", "CreateTaskDB Got eventID " + eventID);
            }
        } catch (Exception e) {
            Log.e("OurDB", "CreateTaskDB Error parsing data " + e.toString());
            return 3;
        }

        for (int i = 0; i < phoneNumbers.size(); i++) {
            sqlCall = "INSERT INTO `EventUserTable`(`eventID`, `phoneNumber`) VALUES ('" +
                    eventID + "','" +
                    phoneNumbers.get(i) +
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
                Log.e("OurDB", "CreateTaskDB Error in http connection " + e.toString());
                return 1;
            }
            //convert response to string
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                if (reader.readLine().equals("true")) {
                    Log.i("OurDB", "CreateTaskDB added a phonenumber");
                } else {
                    Log.i("OurDB", "CreateTaskDB failed to insert user probably exists already");
                    is.close();
                    return 3;
                }
            } catch (Exception e) {
                Log.e("OurDB", "CreateTaskDB Error converting result " + e.toString());
                return 2;
            }
        }


        //EVENT PICTURES
        for (int i = 0; i < imageList.size(); i++) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ((Bitmap)imageList.get(i)).compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            String pictureAsString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

            sqlCall = "INSERT INTO `EventPictureTable`(`eventID`, `picture`) VALUES ('" +
                    eventID + "','" +
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
                Log.e("OurDB", "CreateTaskDB Error in http connection " + e.toString());
                return 1;
            }
            //convert response to string
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                if (reader.readLine().equals("true")) {
                    Log.i("OurDB", "CreateTaskDB added a image");
                } else {
                    Log.i("OurDB", "CreateTaskDB failed to insert user probably exists already");
                    is.close();
                    return 3;
                }
            } catch (Exception e) {
                Log.e("OurDB", "CreateTaskDB Error converting result " + e.toString());
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
                    Toast.makeText(context, "User Already Exist", Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {

        }
    }
}
