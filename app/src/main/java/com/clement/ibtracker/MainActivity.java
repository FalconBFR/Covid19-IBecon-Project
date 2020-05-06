package com.clement.ibtracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.BeaconService;
import org.w3c.dom.Text;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    //initialize
    private static final String TAG = "MainActivity";

    public Context context;

    private Button startButton;
    private Button stopButton;
    private Button dbupdate;
    private Button loadtheill;
    public Boolean ViewingDB = true; //else viewing close contacts


    public BeaconManager beaconManager = null;
    //public Region beaconRegion = null; //monitoring region

    //private static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"; //Todo: switch to IBeacon Later
    private static final String IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    //

    //UUID management
    final String BEACONIDTXT = "beaconid.txt";
    EditText mEditText;
    TextView yourassigneduuid;


    Context basecontext = null;

    //main functions

    //KEEP THIS SNIPPET PLEASE!
    public void ShowAlert(final String title, final String message) {
        runOnUiThread(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(message);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> {
                dialog.dismiss();
            });
            alertDialog.show();
        });
    }
    //DO NOT REMOVE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File myFile = new File("closecontacts.txt");
        try {
            myFile.createNewFile(); // if file already exists will do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("PRINTING EXAMPLE");
        System.out.println("APP STARTED");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //error due to api version issues. Debugger explains. Ignore for now. (Api 23 , API 21)
        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1234); //check min api error


        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        loadtheill = findViewById(R.id.loadtheill);
        dbupdate = findViewById(R.id.dbupdate);
        //startButton.setOnClickListener((v) -> { startBeaconMonitoring(); });
        startButton.setOnClickListener((v) -> {
            //making sure that a UUID is present, else: grabbing one from server
            autoloadtocheckuuid();
            //Turning On Bluetooth to avoid errors:
            enableBT();
            startService(new Intent (this, beaconservice.class));
            //which view to load at start (ill patients or full uuid view
            loaddbview(this);
            //loadillclosecontactsview(getBaseContext());
            //loaddbview(this);
            loadillclosecontactsview(this);
        });
        System.out.println("OMGOMGOMG");
        //stopButton.setOnClickListener((v) -> { stopBeaconMonitoring(); });
        stopButton.setOnClickListener((v) -> {
            stopService(new Intent(this, com.clement.ibtracker.beaconservice.class));
        });

        dbupdate.setOnClickListener((v) -> {
            loaddbview(this);
            loaduuid();
        });

        loadtheill.setOnClickListener((v) -> {
            loadillclosecontactsview(this);
        });


        TextView dbtextview = findViewById(R.id.dbtextview);

        //beaconManager = BeaconManager.getInstanceForApplication(this);
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout((IBEACON_LAYOUT))); //SWITCH TO IBEACON LATER //Todo: switch to IBeacon
        //beaconManager.bind(this);

        //SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        //sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(beaconid TEXT, occurrence INTEGER)"); //a table in the database named 'contacts' will be created if it does not exist
        //sqLiteDatabase.execSQL

        //related to the UUID TExt Box Below
        //mEditText = findViewById(R.id.edit_text);
        yourassigneduuid = findViewById(R.id.yourassigneduuid);

        System.out.println("getBaseContextOnCreate" + getBaseContext());
        basecontext = getBaseContext();

        //loaddbview(this);
        loadillclosecontactsview(this);

        DownoladData downloadData = new DownoladData();
        downloadData.execute("");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loaduuid(); //for the view


    }

    public void enableBT(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }

    @Override
    public Context getApplicationContext() {
        System.out.println("ever here at getappcontext");
        return super.getApplicationContext();
    }

    @Override
    public Context getBaseContext() {
        return super.getBaseContext();
    }

    public String datettimeprocessingselfwrite(long currenttimeinms){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:MM");
        Date date = new Date(currenttimeinms);
        String time = simpleDateFormat.format(date);
        Log.d("datetimeprocessing",time);
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
        }catch(SQLiteException e) {
            dbinstr.append("Welcome to the app. This app is created by Clement Tong, a 14 year old from HKSAR, China. Please note that this app is provided for us as is and is not a replacement for any measures you are taking now"); //todo:legal instructions and quick start guide
        }
        //final String BEACONIDTXT = "beaconid.txt";
        //setContentView(R.layout.activity_main);
        //EditText mEditText;
        //setContentView(R.layout.activity_main);
        System.out.println("actually");
        TextView dbtextview = findViewById(R.id.dbtextview);
        dbtextview.setText("");
        dbtextview.setText(dbinstr);


    }

    public void loadillclosecontactsview(Context context) {
        //String text = mEditText.getText().toString();
        //Context context = this.context;
        StringBuilder datainstr = new StringBuilder("ILL Close Contacts Table \n : Beaconid ");
        System.out.println("loadingillclose");

        //EditText mEditText = null;
        //System.out.println("actually");
        //FileInputStream fis = null;
        try {
            //String fileName = "closecontacts.txt";
            String file_name = this.getFilesDir()
                    .getAbsolutePath() + "/closecontacts.txt";
            File file = new File(file_name);
            System.out.println("the file line 418 file"+file);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            //MUST NOT DEBUG BY PRINTING OUT BR.READLINE() since it can only be done once
            String line;
            Boolean lastlinewasempty = false;
            Integer counter = 0;
            while((line = br.readLine()) != null | counter>3 ){ // '|'means or
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
                System.out.println("datainstr line"+line);
                datainstr.append("\n"+ line);
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
        TextView dbtextview = findViewById(R.id.dbtextview);
        dbtextview.setText(datainstr);

    }

    public void saveuuid(View v) {
        String text = mEditText.getText().toString();
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(BEACONIDTXT, MODE_PRIVATE);
            fos.write(text.getBytes());

            mEditText.getText().clear();
            //BEACONUUID = text.getBytes().toString();
            //System.out.println(BEACONUUID);
            //System.out.println("*******************************");
            Toast.makeText(this, "Saved your UUID", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void loaduuid() {
        FileInputStream fis = null;

        try {
            fis = openFileInput(BEACONIDTXT);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            //mEditText.setText(sb.toString());
            yourassigneduuid.setText("Your assigned UUID is: " + sb.toString());

            System.out.println(sb.toString());// sb.toString is correct
            System.out.println("+++++++++");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        @SuppressLint({"NewApi", "LocalSuppress"}) ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    private class DownoladData extends AsyncTask<String,Void, List> {
        private static final String TAG = "DownloadedData";
        @Override
        protected void onPostExecute(List list) {
            Log.d(TAG,"onPostExecuted:Param is" + list);
            super.onPostExecute(list);
        }

        @Override
        protected List doInBackground(String... strings) {
            List<com.clement.ibtracker.Patientdata> patientsdata = new ArrayList<>();
            List<String> patientsdatauuid = new ArrayList<>();
            Log.d(TAG,"Arrived in do in background (Async Task)");
            //getting data in background from link
            InputStream input = null;
            try {
                Log.d(TAG,"do In Background: Starts download");
                input = new URL("http://206.189.39.40/static/cases.csv").openStream();
                System.out.println("input"+input);
            } catch (IOException e) {
                //todo: A better feedback to the user about the issue of lack of internet connection
                //Means that the server is either down or the user doesn't have internet connection
                Log.e(TAG, "download error");
                //e.printStackTrace();
                List<String> faileddownloaduserfeedback = new ArrayList<String>();
                faileddownloaduserfeedback.add("!!!!!! ***** Check Your Internet Connection." +
                        " Or else, the server is currently down. Sorry for the inconvinience caused ****** !!!");
                ShowAlert("Nothing to worry about but No Internet Connection/Server is Down "
                        ,"Please continue to turn on this app. Again, let me repeat that we don't upload your data. However, " +
                        "I don't know who is sick yet. Please let me repeat that this app will not detect every single close contact. " +
                        "Take necessary precautions please. This is not a replacement for Social Distancing or any other measures you are " +
                        "currently taking/asked by the goverment to take.");
                savingcctotxt(faileddownloaduserfeedback);
                return faileddownloaduserfeedback;
            }
            //reading the data into input stream reader
            Reader reader1 = null;
            reader1 = new InputStreamReader(input, StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(reader1);
            String dataline;
            StringBuilder problematicpeole = new StringBuilder();
            while (true) {
                try {
                    if (!((dataline = reader.readLine()) != null)) break;
                    //split by ','
                    String[] tokens = dataline.split(",");

                    //read the data
                    Context context = getBaseContext();
                    SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
                    Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM contacts;", null);

                    if (query.moveToFirst()) { //means equals to true, no need to specify
                        Log.d(TAG,"Querey-ing");
                        do {
                            String beaconid = query.getString(0);
                            Integer occurrence = query.getInt(1);
                            Double beacondist = query.getDouble(2);
                            if(beaconid.equals(tokens[0])){ // never use string == string. Use string.equals(string) beaconid == tokens[0] is wrong
                                Log.e(TAG,"Problem here. Confimed case" + patientsdata);
                                //problematicpeole.append("\n" + beaconid + ":   " + occurrence + ":   " + beacondist + "m");
                                com.clement.ibtracker.Patientdata patientdata = new com.clement.ibtracker.Patientdata(); //** Patient data is data for a single patient. Patients Data is data for all the close contact confirmed cases
                                patientdata.setUuid(tokens[0]);
                                patientdata.setSituation(tokens[1]);
                                patientdata.setDate(tokens[2]);
                                System.out.println("patientdata"+patientdata.getUuid());
                                // todo: limitation : If multiple records of the Same UUID exists on the server,
                                //  it cannot be handeled. It will print out all the uuids. However, if there are multiple detections of the Same UUID locally, it is not a problem
                                System.out.println("Contains patient???"+ Arrays.asList(patientsdata).contains(patientdata));
                                if (!Arrays.asList(patientsdata).contains(patientdata)){
                                    patientsdata.add(patientdata);
                                    patientsdatauuid.add(patientdata.getUuid());
                                    System.out.println("patientsdatalist"+patientsdata);
                                    /*if(!Arrays.asList(patientsdatauuid).contains(patientdata.getUuid())) {
                                        System.out.println("patientsdatalist - uuid" + patientsdatauuid);
                                        Log.e(TAG, "Close Contact Patients Data" + patientsdata);
                                    }*/
                                } else {
                                    System.out.println("Already Recorded");
                                }
                            }
                        } while (query.moveToNext());
                    }
                }catch (SQLiteException e){
                   break;
                } catch (ArrayIndexOutOfBoundsException e){
                    continue;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //saving file to txt
            savingcctotxt(patientsdata);

            return patientsdata;
        }
    }

    public void savingcctotxt(List patientsdata){
        FileOutputStream fos = null;
        try {
            Context context = this;
            String file_name = MainActivity.this.getFilesDir()
                    .getAbsolutePath() + "/closecontacts.txt";
            //fos = openFileOutput("closecontacts.txt", MODE_PRIVATE);

            fos = new FileOutputStream(file_name,false);
            //fos.write(text.getBytes());
            String tobewrittendata;
            tobewrittendata = "\n";
            fos.write(tobewrittendata.getBytes());
            tobewrittendata = patientsdata.toString();
            tobewrittendata = tobewrittendata.replace("}","\n");
            fos.write(tobewrittendata.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class NewUUID extends AsyncTask<String,Void, List> {
        private static final String TAG = "DownloadedData";
        @Override
        protected void onPostExecute(List list) {
            Log.d(TAG,"onPostExecuted:Param is" + list);
            super.onPostExecute(list);
        }

        @Override
        protected List doInBackground(String... strings) {
            List<com.clement.ibtracker.Patientdata> patientsdata = new ArrayList<>();
            Log.d(TAG,"Arrived in do in background (Async Task)");
            //getting data in background from link
            InputStream input = null;
            try {
                Log.d(TAG,"do In Background: NEWUUID!!!!!!!!!");
                input = new URL("http://206.189.39.40/uuid/new").openStream();
                System.out.println("new uuid buffer input"+input);
            } catch (IOException e) {
                //todo: A better feedback to the user about the issue of lack of internet connection
                //Means that the server is either down or the user doesn't have internet connection
                Log.e(TAG, "download error");
                //e.printStackTrace();
                List<String> faileddownloaduserfeedback = new ArrayList<String>();
                faileddownloaduserfeedback.add("!!!!!! ***** Check Your Internet Connection." +
                        " Or else, the server is currently down. Sorry for the inconvinience caused ****** !!!");
                ShowAlert("Nothing to worry about but No Internet Connection/Server is Down "
                        ,"Please try to ensure that internet connection can be guranteed and " +
                                "be patient while we solve this technical issue. Many thanks.");
                return faileddownloaduserfeedback;
            }
            //reading the data into input stream reader
            Reader reader1 = null;
            reader1 = new InputStreamReader(input, StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(reader1);
            String dataline = null;
            //StringBuilder problematicpeole = new StringBuilder("");
            try {
                dataline = reader.readLine();
                System.out.println("newuuid dataline!!!" + dataline);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //saving file to txt
            savinguuidtotxt(dataline);

            return patientsdata;
        }

        void savinguuidtotxt(String newuuid){
            FileOutputStream fos = null;
            try {
                Context context = MainActivity.this;
                String file_name = MainActivity.this.getFilesDir()
                        .getAbsolutePath() + "/beaconid.txt";
                //fos = openFileOutput("closecontacts.txt", MODE_PRIVATE);

                fos = new FileOutputStream(file_name,false);
                fos.write(newuuid.getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String autoloadtocheckuuid() {
        // Function is Existant beacuse it is for first time users
        String BEACONUUID = "";
        FileInputStream fis = null;
        try {
            fis = openFileInput(BEACONIDTXT);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            //mEditText.setText(sb.toString());
            BEACONUUID = sb.toString(); //sb.toString gives you a string output of what is on the .txt document
            //System.out.println("\nheyhey");
            //System.out.println(sb.toString());// sb.toString is correct
            //System.out.println("10000000-0000-0000-0000-000000000000");
            //System.out.println(BEACONUUID=="10000000-0000-0000-0000-000000000000");
            BEACONUUID = BEACONUUID.replaceAll("\\s+", "");
            //System.out.println(BEACONUUID=="10000000-0000-0000-0000-000000000000");
            System.out.println("+++++++++");

        } catch (FileNotFoundException e) {
            //Implement cannot find UUID function:
            System.out.println("FNF e");
            Log.e("beaconservice","CanotFindUUID.txt file. Running choose uuid and saving from server");
            NewUUID newUUID = new NewUUID();
            newUUID.execute("");
            autoloadtocheckuuid(); //run the function again since the UUID needs to be loaded into the system
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //if (fis!= null){
            try {
                //BEACONUUID = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa6";
                fis.close();
            } catch (NullPointerException e){
                //another spot to implement UUID not found function but I am choosing another place to run the newuuid func
                Log.d("beaconservice","This should not happen! in autoload function Null Pointer Exception e. Should be rulled out by FNe");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //}
        }
        if(BEACONUUID.length()<36){
            //beacon id is some how messed up
            Log.d(TAG,"CRAP AUTOLOAD BACKUP UUID HAPPENED!!! THIS IS AMAZING");
            //BEACONUUID="00000000-0000-0000-0000-000000000000";

        }
        return BEACONUUID;
    }
}
