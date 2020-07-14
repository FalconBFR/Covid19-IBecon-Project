package com.clement.ibtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.service.BeaconService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class all_contacts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);
    }

    public String datettimeprocessingselfwrite(long currenttimeinms) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:MM");
        Date date = new Date(currenttimeinms);
        String time = simpleDateFormat.format(date);
        Log.d("datetimeprocessing", time);
        return time;
    }

    public void loaddbview(Context context) {
        //Context context = this.context;
        StringBuilder dbinstr = new StringBuilder("All Detected Close Contacts** : \nID , Secs.in contact , Date-&-time-of-last-contact \n\n");
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

                    dbinstr.append("\n" + concatbeaconid.toString() + ":   " + occurrence + ":   " + beacondistprocessed + "m  " + processeddatetime);
                } while (query.moveToNext());

            }
        } catch (SQLiteException e) {
            dbinstr.append("Welcome to the app. This app is created by Clement Tong, a 14 year old from HKSAR, China. Please note that this app is provided for us as is and is not a replacement for any measures you are taking now"); //todo:legal instructions and quick start guide
        }

        System.out.println("actually");
        TextView dbtextview = findViewById(R.id.dbtextview);
        dbtextview.setText("");
        dbtextview.setText(dbinstr);

    }
}