package com.example.dips.smartscheduler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Jorge on 3/31/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOGTAG = "OurDB";
    private static final String DATABASE_NAME = "Room.db";
    private static final int DATABASE_VERSION = 2;

    // SQL Statement to create UserTable.
    private static final String USERTABLE_CREATE =
            "CREATE TABLE UserTable( " +
                    "phoneNumber TEXT PRIMARY KEY," +
                    "password TEXT, " +
                    "firstName TEXT, " +
                    "lastName TEXT, " +
                    "email TEXT);";

    // SQL Statement to create EventUserTable.
    private static final String EVENTUSERTABLE_CREATE =
            "CREATE TABLE EVENTUSERTABLE( " +
                    "eventID INTEGER," +
                    "phoneNumber TEXT);";

    // SQL Statement to create EventTable.
    private static final String EVENTTABLE_CREATE =
            "CREATE TABLE EventTable( " +
                    "eventID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "eventTitle TEXT," +
                    "eventDescription TEXT," +
                    "dueDate TEXT," +
                    "startDate TEXT," +
                    "comment TEXT," +
                    "completedDate TEXT," +
                    "completedBy TEXT);";

    // SQL Statement to create EventPictureTable.
    private static final String EVENTPICTURETABLE_CREATE =
            "CREATE TABLE EventPictureTable( " +
                    "eventID INTEGER," +
                    "picture TEXT);";


    //SQL Statement to get GroupNames Details.
    private static final String GROUPTABLE_DETAILS =
            "SELECT groupName FROM GroupTable " +
                    "INNER JOIN UserGroupTable on GroupTable.groupID = UserGroupTable.groupID " +
                    "WHERE phoneNumber=";

    // SQL Statement to get Event Details
    private static final String EVENTTABLE_DETAILS =
            "SELECT eventTitle, eventDescription, assignTo, dueDate from EventTable where eventID IN " +
                    "(SELECT eventID FROM GroupEventTable NATURAL JOIN UserGroupTable " +
                    "WHERE phoneNumber=";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create all tables
        db.execSQL(USERTABLE_CREATE);
        db.execSQL(EVENTUSERTABLE_CREATE);
        db.execSQL(EVENTTABLE_CREATE);
        db.execSQL(EVENTPICTURETABLE_CREATE);
        Log.i(LOGTAG, "TABLES CREATED");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(LOGTAG, "Upgrading");
        //onUpgrade Drop all old tables and remake
        db.execSQL("DROP TABLE IF EXISTS " + USERTABLE_CREATE);
        db.execSQL("DROP TABLE IF EXISTS " + EVENTTABLE_CREATE);
        db.execSQL("DROP TABLE IF EXISTS " + EVENTPICTURETABLE_CREATE);
        onCreate(db);
    }

    //used in createTask to build out task and add pictures and put in group
    public int InsertNewTask(int groupID, String title, String desc, String assignedTo, String date, String startDate, ArrayList imageList) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            //fill values to prevent injection
            ContentValues values = new ContentValues();
            values.put("eventTitle", title);
            values.put("eventDescription", desc);
            values.put("assignTo", assignedTo);
            values.put("DueDate", date);
            values.put("StartDate", startDate);
            // Inserting Row
            db.insert("EventTable", null, values);

            //get the inserted events ID
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid();", null);
            cursor.moveToFirst();
            int EventID = cursor.getInt(0);

            //add images
            for (int i = 0; i < imageList.size(); i++) {
                //get byte array for image aka blob
                byte[] data = getBitmapAsByteArray((Bitmap) imageList.get(i));
                values = new ContentValues();
                values.put("eventID", EventID);
                values.put("picture", data);
                db.insert("EventPictureTable", null, values);
            }

            //add event to group
            values = new ContentValues();
            values.put("eventID", EventID);
            values.put("groupID", groupID);
            db.insert("GroupEventTable", null, values);

            db.close(); // Closing database connection
            Log.i(LOGTAG, "Successfully Inserted New task");
            return 1;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to Insert new task " + e.toString());
        }
        return -1;
    }

    //used in createTask to populate groupMemberDropDown
    public ArrayList<String> GetGroupMembersName(int groupID) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ArrayList<String> al = new ArrayList<>();

            Cursor cursor = db.rawQuery("SELECT firstName, LastName FROM UserTable " +
                    "INNER JOIN UserGroupTable on Usertable.phoneNumber " +
                    "= UserGroupTable.phoneNumber WHERE groupID = " + groupID, null);

            while (cursor.moveToNext()) {
                al.add(cursor.getString(0) + " " + cursor.getString(1));
            }
            cursor.close();
            Log.i(LOGTAG, "successfully got group member ");
            return al;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to get group member " + e.toString());
        }
        return null;
    }

    //used in completeTask and view Single Task to return task data
    public String[] GetTaskInfo(int taskID) {
        try {
            String[] sa = new String[5];

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM EventTable where eventID = " + taskID, null);

            cursor.moveToFirst();
            sa[0] = cursor.getString(1); // event Title
            sa[1] = cursor.getString(2); // event Desc
            sa[2] = cursor.getString(3); // due date
            sa[3] = cursor.getString(4); // start Date
            sa[4] = cursor.getString(5); // comment
            cursor.close();
            Log.i(LOGTAG, "Successfully Got Task Info");
            return sa;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to get TaskInfo " + e.toString());
            return null;
        }
    }

    //used in view Single Task
    public ArrayList GetPeople(int taskID) {
        try {
            ArrayList phoneNumbers = new ArrayList();

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT phoneNumber FROM EventUserTable where eventID = " + taskID, null);

            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(0);
                Cursor cursor1 = db.rawQuery("SELECT firstName, lastName FROM`UserTable`where " +
                        "phoneNumber = " + phoneNumber, null);
                if (cursor1.moveToFirst()) { // they have account
                    phoneNumbers.add(cursor1.getString(0) + " " + cursor1.getString(1) + " " + phoneNumber);
                } else { // is just phoneNumber
                    phoneNumbers.add(phoneNumber);
                }
            }

            cursor.close();
            Log.i(LOGTAG, "Successfully Got Poeple");
            return phoneNumbers;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to get Poeple " + e.toString());
            return null;
        }
    }

    public int completeTask(int EventID, String comments, ArrayList imageList) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            SQLiteStatement insertStatement = db.compileStatement("UPDATE EventTable SET comment = ? WHERE eventID = ?");
            insertStatement.bindString(1, comments);
            insertStatement.bindString(2, EventID + "");
            insertStatement.executeInsert();

            //add images
            for (int i = 0; i < imageList.size(); i++) {
                //get byte array for image aka blob
                byte[] data = getBitmapAsByteArray((Bitmap) imageList.get(i));
                ContentValues values = new ContentValues();
                values.put("eventID", EventID);
                values.put("picture", data);
                db.insert("EventPictureTable", null, values);
            }
            Log.i(LOGTAG, "successfully edited task ");
            return 1;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to edited task " + e.toString());
        }
        return -1;
    }

    //used in complete Task
    public ArrayList GetImages(int taskID) {
        try {
            ArrayList<Bitmap> bl = new ArrayList<>();

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT picture FROM EventPictureTable where eventID = " + taskID, null);

            while (cursor.moveToNext()) {
                String pictureAsString = cursor.getString(0);
                Log.i("test",pictureAsString);
                byte[] imgByte =  Base64.decode(pictureAsString, Base64.DEFAULT);
                bl.add(BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length));
            }
            cursor.close();
            Log.i(LOGTAG, "Successfully Got images");
            return bl;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to get images " + e.toString());
            return null;
        }
    }

    //used to convert bitmaps to byteArray for storage
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }


    //used in GroupList.java to display list of all groups
    public String[] getGroupList(int phnNumber) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            //get the cursor to the GroupTable
            Cursor cursor = db.rawQuery(GROUPTABLE_DETAILS + String.valueOf(phnNumber), null);
            //count the number of rows
            int rowNum = cursor.getCount();

            String[] groupNames = new String[rowNum];

            int i = 0;
            while (cursor.moveToNext()) {  // get the data into array, or class variable
                groupNames[i] = cursor.getString(0);
                i++;
            }

            cursor.close();
            db.close();
            Log.i(LOGTAG, "Successfully Fetched GroupNames");
            return groupNames;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to Fetch GroupNames " + e.toString());
            return null;
        }
    }


    //used in ViewTask.java to display list of all groups
    public List<String[]> getTaskList(String phnNumber) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            //get the cursor
            //SQL Statement to get GroupNames Details.
            String SQLCALL =
                    "SELECT eventTitle,eventID from EventTable where eventID IN " +
                            "(SELECT eventID FROM EventUserTable NATURAL JOIN EventTable " +
                            "WHERE phoneNumber=";
            Cursor cursor = db.rawQuery(SQLCALL + String.valueOf(phnNumber) + " and completedDate IS NULL)", null);

            //count the number of rows
            int rowNum = cursor.getCount();

            List<String[]> eventDetails = new ArrayList<>();

            String[] eventNames = new String[rowNum];

            String[] eventIds = new String[rowNum];

            int i = 0;
            while (cursor.moveToNext()) {  // get the data into array, or class variable
                eventNames[i] = cursor.getString(0);
                eventIds[i] = cursor.getString(1);
                eventDetails.add(new String[]{eventIds[i], eventNames[i]});
                i++;
            }

            cursor.close();
            db.close();
            Log.i(LOGTAG, "Successfully Fetched EventNames");
            return eventDetails;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to Fetch EventNames " + e.toString());
            return null;
        }
    }

    //used in CompleteTaskList.java to display list of all Completed tasks
    public List<String> getEventPhoneNumbers(int eventId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            //get the cursor
            //SQL Statement to get PhoneNumber.
            String SQLQuery ="SELECT phoneNumber FROM EventUserTable " +
                            "WHERE eventID=";

            Cursor cursor = db.rawQuery(SQLQuery + eventId, null);

            //count the number of rows
            int rowNum = cursor.getCount();

            List<String> eventPhoneNumbers = new ArrayList<>();

            String[] phoneNumbers=new String[rowNum];

            int i = 0;
            while (cursor.moveToNext()) {  // get the data into array, or class variable
                phoneNumbers[i] = cursor.getString(0);
                Log.d("eventPhoneNumbers",phoneNumbers[i]);
                eventPhoneNumbers.add(phoneNumbers[i]);
                i++;
            }

            return eventPhoneNumbers;
        } catch (Exception e) {
        Log.i(LOGTAG, "Failed to Fetch eventPhoneNumbers " + e.toString());
        return null;
        }
    }

            //used in CompleteTaskList.java to display list of all Completed tasks
    public List<String[]> getCmpltTaskList(String phnNumber) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            //get the cursor
            //SQL Statement to get GroupNames Details.
            String SQLQuery =
                    "SELECT eventTitle,eventID from EventTable where eventID IN " +
                            "(SELECT eventID FROM EventUserTable NATURAL JOIN EventTable " +
                            "WHERE phoneNumber=";
            Cursor cursor = db.rawQuery(SQLQuery + String.valueOf(phnNumber) + " and completedDate IS NOT NULL)", null);

            //count the number of rows
            int rowNum = cursor.getCount();

            List<String[]> eventDetails = new ArrayList<>();

            String[] eventNames = new String[rowNum];

            String[] eventIds = new String[rowNum];

            int i = 0;
            while (cursor.moveToNext()) {  // get the data into array, or class variable
                eventNames[i] = cursor.getString(0);
                eventIds[i] = cursor.getString(1);
                eventDetails.add(new String[]{eventIds[i], eventNames[i]});
                i++;
            }

            cursor.close();
            db.close();
            Log.i(LOGTAG, "Successfully Fetched CompletedEventNames");
            return eventDetails;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to Fetch CompletedEventNames " + e.toString());
            return null;
        }
    }


    public int CreateGroup(String name, String desc, String phoneNumber) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("groupName", name);
            values.put("groupDesp", desc);
            db.insert("GroupTable", null, values);

            //get the inserted events ID
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid();", null);
            cursor.moveToFirst();
            int groupID = cursor.getInt(0);

            values = new ContentValues();
            values.put("groupID", groupID);
            values.put("phoneNumber", phoneNumber);
            db.insert("UserGroupTable", null, values);

            Log.i(LOGTAG, "Successfully created group ");
            return 1;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to create group " + e.toString());
        }
        return -1;
    }

    //used in ViewSingleTask.java to display the details of single task
    public String[] getTaskDetails(String phnNumber, int position) {
        try {
            Log.i("IN DBHelper", "Fetching data for viewSingleTask");

            SQLiteDatabase db = this.getReadableDatabase();

            //get the cursor
            //SQL Statement to get GroupNames Details.
            String SQLCALL =
                    "SELECT eventID, eventTitle, eventDescription, DueDate, startDate from EventTable where eventID IN " +
                            "(SELECT eventID FROM EventUserTable NATURAL JOIN EventTable " +
                            "WHERE phoneNumber=";
            Cursor cursor = db.rawQuery(SQLCALL + String.valueOf(phnNumber) + " and completedDate IS NULL)", null);


            String taskTitle;
            String taskDescription;
            String dueDate;
            String startDate;
            String[] taskDetails;


            int i = 0;
            if (position != 0) {
                while (cursor.moveToNext()) {  // get the data into array, or class variable
                    i++;
                    if (position == i) {
                        break;
                    }
                }
            }
            cursor.moveToNext();


            String eventId = cursor.getString(0);

            String SQLCALL_FOR_NUM =
                    "SELECT phoneNumber FROM EventUserTable " +
                            "WHERE eventID=";
            Cursor cursor2 = db.rawQuery(SQLCALL_FOR_NUM + eventId, null);


            String SQLCALL_FOR_NAME =
                    "SELECT firstName, lastName FROM UserTable " +
                            "WHERE phoneNumber=";

            String personsInvolved = "";
            int count = cursor2.getCount();
            String[] numbersInvolved = new String[cursor2.getCount()];
            //cursor2.moveToFirst();
            int temp = 0;


            Cursor cursor_Names = null;
            while (cursor2.moveToNext()) {
                Log.i("Phone #", cursor2.getString(0));
                cursor_Names = db.rawQuery(SQLCALL_FOR_NAME + cursor2.getString(0), null);
                cursor_Names.moveToNext();
                personsInvolved = personsInvolved + cursor_Names.getString(0) + " " + cursor_Names.getString(1) + ", ";
                Log.i("Phone #", personsInvolved);
            }

            personsInvolved = personsInvolved.substring(0, personsInvolved.length() - 2);

            if (i == position) {
                taskTitle = cursor.getString(1);
                taskDescription = cursor.getString(2);
                dueDate = cursor.getString(3);
                startDate = cursor.getString(4);

                taskDetails = new String[]{taskTitle, taskDescription, dueDate, startDate, personsInvolved, eventId};
                cursor.close();
                db.close();
                Log.i(LOGTAG, "Successfully Fetched Event Details");
                return taskDetails;
            } else {
                Log.i(LOGTAG, "Problem in fetching event details");
            }

        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to Fetch event details " + e.toString());
            return null;
        }
        return null;
    }

    public String getPendingJobs(String phoneNumber) {
        try {
            String eventName = null;
            SQLiteDatabase db = this.getWritableDatabase();

            //get the cursor
            //SQL Statement to get GroupNames Details.
            String SQLQuery =
                    "SELECT eventTitle,dueDate from EventTable where eventID IN " +
                            "(SELECT eventID FROM EventUserTable NATURAL JOIN EventTable " +
                            "WHERE phoneNumber=";
            Cursor cursor = db.rawQuery(SQLQuery + String.valueOf(phoneNumber) + " and completedDate IS NULL)", null);

            //count the number of rows
            Calendar calendar = Calendar.getInstance();
            String today = (calendar.get(Calendar.MONTH) + 1) + "/" +
                    calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.YEAR);

            while (cursor.moveToNext()) {  // get the data into array, or class variable
                if (cursor.getString(1).equals(today))
                    eventName = cursor.getString(0);
            }

            cursor.close();
            db.close();
            Log.i(LOGTAG, "Successfully Fetched pending Jobs");
            return eventName;
        } catch (Exception e) {
            Log.i(LOGTAG, "Failed to Fetch pending Jobs " + e.toString());
            return null;
        }
    }
}


