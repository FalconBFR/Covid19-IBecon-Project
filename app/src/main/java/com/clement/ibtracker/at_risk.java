package com.clement.ibtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class at_risk extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at_risk);
        loadillclosecontactsview(this);
    }

    public void loadillclosecontactsview(Context context) {
        StringBuilder datainstr = new StringBuilder("ILL Close Contacts Table \n : Beaconid ");
        System.out.println("loadingillclose");

        try {
            String file_name = this.getFilesDir()
                    .getAbsolutePath() + "/closecontacts.txt";
            File file = new File(file_name);
            System.out.println("the file line 418 file" + file);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            //MUST NOT DEBUG BY PRINTING OUT BR.READLINE() since it can only be done once
            String line;
            Boolean lastlinewasempty = false;
            Integer counter = 0;
            while ((line = br.readLine()) != null | counter > 3) { // '|'means or
                System.out.println("entered 428 while loop");
                //process the line
                /*//stop reading logic: if 3 consecutive lines are empty
                lastlinewasempty = false;
                if (br.readLine() == null){
                    counter +=1;
                    lastlinewasempty = true;
                    System.out.println("crap file line empty");
                } else {
                    lastlinewasempty = false;
                    counter=0;
                }*/
                System.out.println("datainstr line" + line);
                datainstr.append("\n" + line);
                System.out.println("datainstr" + datainstr);
            }
            System.out.println("FINAL DATAINSTR" + datainstr);
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("loadingfilenotfound431");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOE449");
            e.printStackTrace();
        }
        TextView dbtextview = findViewById(R.id.all_at_risk_textview);
        dbtextview.setText(datainstr);

    }

}