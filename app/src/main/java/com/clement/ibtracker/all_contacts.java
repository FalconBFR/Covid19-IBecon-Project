package com.clement.ibtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.service.BeaconService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class all_contacts extends AppCompatActivity {
    private static final String TAG = "all_contacts";

    //recycling view
    private ArrayList<Contact_info> mContact_info = new ArrayList<>(); //student class is defined at the bottom of this activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);
        Log.d(TAG,"loaddbview started");
        loaddbview(this);
        Log.d(TAG,"initalize_RecyclerView started");
        initalize_recyclerView();

    }

    public String datettimeprocessingselfwrite(long currenttimeinms) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:MM");
        Date date = new Date(currenttimeinms);
        String time = simpleDateFormat.format(date);
        Log.d("datetimeprocessing", time);
        return time;
    }

    private void initalize_recyclerView(){
        Log.d(TAG,"initializing Recycler View");
        RecyclerView all_contacts_recyclerView = findViewById(R.id.all_contacts_recycler_view);
        all_contacts_RecyclerViewAdapter adapter = new all_contacts_RecyclerViewAdapter(this, mContact_info);
        all_contacts_recyclerView.setAdapter(adapter);
        all_contacts_recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void loaddbview(Context context) {
        //Context context = this.context;
        //StringBuilder dbinstr = new StringBuilder("All Detected Close Contacts** : \nID , Secs.in contact , Date-&-time-of-last-contact \n\n");
        //SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        BeaconService beaconService = new BeaconService();
        SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        try {
            Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM contacts;", null);
            if (query.moveToFirst()) { //means equals to true, no need to specify
                do {
                    String beaconid = query.getString(0);
                    Integer occurrence = query.getInt(1);
                    Double beacondist = query.getDouble(2);
                    Long datetime = query.getLong(3);

                    //processing beacondistance to round up
                    Long beacondistprocessed = Math.round(beacondist);


                    //processing Date and Time from MS since 1970 to a readable format
                    String processeddatetime = datettimeprocessingselfwrite(datetime);

                    StringBuilder concatbeaconid = new StringBuilder();
                    //String concatbeaconid ="";

                    //to only get the first few digits of the UUID
                    for (int characterno = 0; characterno < 5; characterno++) {
                        concatbeaconid.append(beaconid.charAt(characterno));
                    }
                    //todo: Change Display Major Minor values instead of uuid since uuid is the same

                    //dbinstr.append("\n" + concatbeaconid.toString() + ":   " + occurrence + ":   " + beacondistprocessed + "m  " + processeddatetime);
                    mContact_info.add(new Contact_info(beaconid, (occurrence/60), processeddatetime));
                } while (query.moveToNext());

            }
        } catch (SQLiteException e) {
            //dbinstr.append("Welcome to the app. This app is created by Clement Tong, a 14 year old from HKSAR, China. Please note that this app is provided for us as is and is not a replacement for any measures you are taking now"); //todo:legal instructions and quick start guide
            mContact_info.add(new Contact_info("Thank you so much for your contributions in limiting the spread of Covid-19 within hospitals. To start detecting for close contacts, press the'Start' Button",0,"N.A"));
        }

        System.out.println("actually");
        TextView dbtextview = findViewById(R.id.dbtextview);
        //dbtextview.setText("");
        //dbtextview.setText(dbinstr);

    }

    public class Contact_info {
        String UUID;
        Integer TimeInContact;
        String Date_time_last_Contact;

        Contact_info(String UUID, Integer TimeInContact, String Date_time_last_Contact) {
            this.UUID = UUID;
            this.TimeInContact = TimeInContact;
            this.Date_time_last_Contact = Date_time_last_Contact;
        }

    }
}