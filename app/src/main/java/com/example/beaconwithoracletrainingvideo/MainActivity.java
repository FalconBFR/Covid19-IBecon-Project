package com.clement.beaconwithoracletrainingvideo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {

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

    Context basecontext = null;

    //main functions
    public void ShowAlert(final String title, final String message) {
        runOnUiThread(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(message);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (DialogInterface.OnClickListener) (dialog, which) -> {
                dialog.dismiss();
            });
            alertDialog.show();
        });
    }

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

        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        loadtheill = (Button) findViewById(R.id.loadtheill);
        dbupdate = (Button)findViewById(R.id.dbupdate);
        //startButton.setOnClickListener((v) -> { startBeaconMonitoring(); });
        startButton.setOnClickListener((v) -> {
            startService(new Intent (this, beaconservice.class));
            loaddbview(this);
        });
        System.out.println("OMGOMGOMG");
        //stopButton.setOnClickListener((v) -> { stopBeaconMonitoring(); });
        stopButton.setOnClickListener((v) -> {
            stopService(new Intent(this, beaconservice.class));
        });

        dbupdate.setOnClickListener((v) -> {
            loaddbview(this);
        });

        loadtheill.setOnClickListener((v) -> {
            loadillclosecontactsview(this);
        });


        TextView dbtextview = (TextView) findViewById(R.id.dbtextview);

        //beaconManager = BeaconManager.getInstanceForApplication(this);
        //beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout((IBEACON_LAYOUT))); //SWITCH TO IBEACON LATER //Todo: switch to IBeacon
        //beaconManager.bind(this);

        //SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        //sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(beaconid TEXT, occurrence INTEGER)"); //a table in the database named 'contacts' will be created if it does not exist
        //sqLiteDatabase.execSQL

        //related to the UUID TExt Box Below
        mEditText = findViewById(R.id.edit_text);

        System.out.println("getBaseContextOnCreate" + getBaseContext());
        basecontext = getBaseContext();

        loaddbview(this);

        DownoladData downloadData = new DownoladData();
        downloadData.execute("");

    }


    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect called");

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                System.out.println("DID ENTER REGION");
                //if (!entryMessageRaised) {
                //todo: change logic later
                ShowAlert("did Enter Region", "Entering Region" + region.getUniqueId() +
                        "Beacon detected UUID/major/minor:" + region.getId1() + "/" + region.getId2() + "/" + region.getId3());
                //entryMessageRaised = true;
                //}
                //this is how you acess beacon  - System.out.println(beaconreal.getId1());
            }

            @Override
            public void didExitRegion(Region region) {
                System.out.println("DID EXIT REGION");
                //if (!exitMessageRaised) {
                //todo: change logic later
                ShowAlert("did Exit Region", "Exiting Region" + region.getUniqueId() +
                        "Beacon detected UUID/major/minor:" + region.getId1() + "/" + region.getId2() + "/" + region.getId3());
                //System.out.println(beaconRegion.getId1());
                //System.out.println(beacon.get);
                //System.out.println(beacon.getid1);
                //exitMessageRaised = true;
                //}
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                //if(!rangingMessageRaised && beacons != null && !beacons.isEmpty()) {
                //if(!beacons.isEmpty()){
                for (Beacon beacon : beacons) {
                    //ShowAlert("didExitRangeRegion", "Ranging region" + region.getUniqueId() +
                    //      " Beacon detected UUID/major/minor:" + beacon.getId1() + "/" +
                    //    beacon.getId2() + "/" + beacon.getId3());
                    //pubbeacon = beacon;
                    Double beacondist = beacons.iterator().next().getDistance();
                    Log.d(TAG, "didRangeBeaconsInRegion:" + beacondist + beacon);
                    ;
                    Log.d(TAG, "writing to database");
                    saveclosecontacts2(beacon, beacondist);
                    Log.d(TAG, "writing to databse complete");
                    //public pubbeacon = beacon.getId1();
                    //getClass(beacon);
                    //Region beaconRegionactual = new Region("MyBeaconStuff", beacon.getId1(), beacon.getId2(), beacon.getId3());
                    //}
                    System.out.println(beacons.isEmpty());
                    //rangingMessageRaised = true;
                    //beaconreal = beacon;
                    //}

                }

            }
        });
    }

    public void startBeaconMonitoring() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout((IBEACON_LAYOUT))); //SWITCH TO IBEACON LATER //Todo: switch to IBeacon
        beaconManager.bind(this);
        Region beaconRegion = new Region("MyBeaconStuff", null, null, null);
        System.out.println(beaconRegion);
        Log.d(TAG, "startBeaconMonitoring called");
        System.out.println("START BEACON MONITORING FUNCTION");
        try {
            System.out.println(beaconRegion);
            beaconManager.startMonitoringBeaconsInRegion(beaconRegion);
            beaconManager.startRangingBeaconsInRegion(beaconRegion);
            transmitbeacon();
        } catch (RemoteException e) {
            e.printStackTrace();
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

    public void stopBeaconMonitoring() {
        Region beaconRegion = new Region("MyBeaconStuff", null, null, null);
        System.out.println("Stop Beacon Monitoring");
        Log.d(TAG, "stopBeaconMonitoring called");
        try {
            beaconManager.stopMonitoringBeaconsInRegion(beaconRegion);
            beaconManager.stopRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //don't use, use saveCloseContacts2 instead
    public void saveCloseContacts(Beacon beacon) {
        SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        //for testing purposes only
        //
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS contacts;");
        //
        //

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(beaconid TEXT,occurrence INTEGER)"); //a table in the database named 'contacts' will be created if it does not exist
        String tobeputinsql;
        Identifier beaconid1; //beaconid1
        beaconid1 = beacon.getId1();
        String beaconidstr = beaconid1.toString();
        beaconidstr = "'" + beaconidstr + "'";


        tobeputinsql = "INSERT INTO contacts VALUES(" + beaconidstr + ", 1);"; //works like a charm: https://stackoverflow.com/a/12472295/10949995
        Log.d(TAG, "onCreate: sql is" + tobeputinsql);
        sqLiteDatabase.execSQL(tobeputinsql);

        //just for development purposes, to confirm it is written to the DB
        Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM contacts;", null);
        if (query.moveToFirst()) { //means equals to true, no need to specify
            do {
                String name = query.getString(0);
                int phone = query.getInt(1);
                //String email = query.getString(2);
                Toast.makeText(this, "SAVED TO DATABASE " +
                        "BluetoothID =" + name + " occurrence " + phone, Toast.LENGTH_LONG).show();

            } while (query.moveToNext());


        }
        query.close();
        sqLiteDatabase.close();
    }

    public void saveclosecontacts2(Beacon beacon, Double distance) {
        Identifier beaconid1; //beaconid1
        int occurrence = 0; // initalize
        SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);

        //for testing purposes only
        //
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS contacts;");
        //
        //

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(beaconid TEXT,occurrence INTEGER, closestavgdist DOUBLE)"); //a table in the database named 'contacts' will be created if it does not exist
        sqLiteDatabase.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_contacts_beaconid ON contacts(beaconid)");

        //declaring the variable 'tobeputinsql' as the variable to store the commands
        String tobeputinsql;

        //formating the beaconid1 value for the database, can be simplified later
        beaconid1 = beacon.getId1();
        String beaconidstr = beaconid1.toString();
        String bidnoquotes = beaconidstr;
        beaconidstr = "'" + beaconidstr + "'";

        Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM contacts;", null);

        if (query.moveToFirst()) { //means equals to true, no need to specify
            do {
                String beaconid = query.getString(0);

                if (beaconid.equals(bidnoquotes)) {
                    occurrence = query.getInt(1);
                    Double distanceprevious = query.getDouble(2);
                    if (distance > distanceprevious) {
                        distance = distanceprevious;
                    }
                    Toast.makeText(this, "BEFORE SAVED TO DATABASE " +
                            "BluetoothID =" + beaconid + " occurrence " + occurrence + "closest distance" + distance, Toast.LENGTH_LONG).show();
                }
            } while (query.moveToNext());
        }

        //saving values to DB
        //tobeputinsql = "INSERT OR IGNORE INTO contacts VALUES(" + beaconidstr + ", 0);"; //works like a charm: https://stackoverflow.com/a/12472295/10949995
        //sqLiteDatabase.execSQL(tobeputinsql);
        //tobeputinsql = "UPDATE contacts SET occurrence = occurrence + 1";
        //tobeputinsql = "UPDATE contacts SET occurrence = (occurrence + 50)";
        //" WHERE beaconid = '0ac59ca4-dfa6-442c-8c65-22247851344c'";

        int newoccurrence = occurrence + 1;
        tobeputinsql = "INSERT OR REPLACE INTO contacts VALUES(" + beaconidstr + "," + newoccurrence + "," + distance + ");";
        Log.d(TAG, "onCreate: sql is" + tobeputinsql);
        sqLiteDatabase.execSQL(tobeputinsql);

        //stopping query
        query.close();
        sqLiteDatabase.close();

        //loaddbview(this);

    }
    //

    public void loaddbview(Context context) {
        //Context context = this.context;
        StringBuilder dbinstr = new StringBuilder("Close Contacts Table \n : Beaconid, Seconds in contact(Occurrence) ");
        //SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        BeaconService beaconService = new BeaconService();
        SQLiteDatabase sqLiteDatabase = context.openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM contacts;", null);
        if (query.moveToFirst()) { //means equals to true, no need to specify
            do {
                String beaconid = query.getString(0);
                Integer occurrence = query.getInt(1);
                Double beacondist = query.getDouble(2);
                StringBuilder concatbeaconid = new StringBuilder("");
                //String concatbeaconid ="";
                for (int characterno = 0; characterno < 5; characterno++) {
                    concatbeaconid.append(beaconid.charAt(characterno));
                }
                dbinstr.append("\n" + concatbeaconid.toString() + ":   " + occurrence + ":   " + beacondist + "m");
            } while (query.moveToNext());
        }
        //final String BEACONIDTXT = "beaconid.txt";
        //setContentView(R.layout.activity_main);
        //EditText mEditText;
        //setContentView(R.layout.activity_main);
        System.out.println("actually");
        TextView dbtextview = (TextView) findViewById(R.id.dbtextview);
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
                datainstr.append("\n"+line.toString());
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
        TextView dbtextview = (TextView) findViewById(R.id.dbtextview);
        dbtextview.setText(datainstr);

    }
    public void transmitbeacon() {
        String BEACONUUID = autoload();
        System.out.println(BEACONUUID);
        //BEACONUUID = "10000000-0000-0000-0000-000000000000";
        System.out.println("^^^^^^^");
        Beacon beacon = new Beacon.Builder()
                .setId1(BEACONUUID)
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x004c)
                .setTxPower(-59)
                .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(beacon);
    }

    //public void setBeaconTransID(){

    public void savefile(View v) {
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

    public void load(View v) {
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

            mEditText.setText(sb.toString());
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

    public String autoload() {
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

            mEditText.setText(sb.toString());
            BEACONUUID = sb.toString(); //sb.toString gives you a string output of what is on the .txt document
            BEACONUUID = BEACONUUID.replaceAll("\\s+", "");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //if (fis!= null){
            try {
                System.out.println("here1223");
                //BEACONUUID = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa6";
                fis.close();
                System.out.println("here");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //}
        }
        return BEACONUUID;
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
            Log.d(TAG,"Arrived in do in background (Async Task)");
            //getting data in background from link
            InputStream input = null;
            try {
                Log.d(TAG,"do In Background: Starts download");
                input = new URL("http://206.189.39.40:5000/static/cases.csv").openStream();
                System.out.println("input"+input);
            } catch (IOException e) {
                Log.e(TAG, "download error");
                e.printStackTrace();
            }
            //reading the data into input stream reader
            Reader reader1 = null;
            try {
                reader1 = new InputStreamReader(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.wtf("MainActivity","Line41");
                e.printStackTrace();
            }

            BufferedReader reader = new BufferedReader(reader1);
            List<Patientdata> patientsdata = new ArrayList<>();
            String dataline;
            StringBuilder problematicpeole = new StringBuilder("");
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
                                Patientdata patientdata = new Patientdata(); //** Patient data is data for a single patient. Patients Data is data for all the close contact confirmed cases
                                patientdata.setUuid(tokens[0]);
                                patientdata.setSituation(tokens[1]);
                                patientdata.setDate(tokens[2]);
                                if (!Arrays.asList(patientsdata).contains(patientdata)){
                                    patientsdata.add(patientdata);
                                    System.out.println(patientsdata);
                                    Log.e(TAG,"Close Contact Patients Data" + patientsdata);
                                } else {
                                    System.out.println("Already Recorded");
                                };
                            }
                        } while (query.moveToNext());
                    }
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
    };


    



}
