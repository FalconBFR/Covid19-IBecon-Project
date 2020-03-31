package com.example.beaconwithoracletrainingvideo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.w3c.dom.Text;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    //initialize
    private static final String TAG = "MainActivity";

    private Button startButton;
    private Button stopButton;


    private BeaconManager beaconManager = null;
    private Region beaconRegion = null; //monitoring region

    private static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"; //Todo: switch to IBeacon Later
    //

    //main functions
    private void ShowAlert(final String title, final String message) {
       runOnUiThread(() -> {
           AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
           alertDialog.setTitle(title);
           alertDialog.setMessage(message);
           alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"OK", (DialogInterface.OnClickListener) (dialog, which)->{
               dialog.dismiss();
           });
           alertDialog.show();
       });
   }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("APP STARTED");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //error due to api version issues. Debugger explains. Ignore for now. (Api 23 , API 21)
        requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1234); //check min api error

        startButton = (Button)findViewById(R.id.startButton);
        stopButton = (Button)findViewById(R.id.stopButton);
        startButton.setOnClickListener((v)->{startBeaconMonitoring();});
        stopButton.setOnClickListener((v)->{stopBeaconMonitoring();});

        TextView dbtextview = (TextView) findViewById(R.id.dbtextview);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout((ALTBEACON_LAYOUT))); //SWITCH TO IBEACON LATER //Todo: switch to IBeacon
        beaconManager.bind(this);

        //SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        //sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(beaconid TEXT, occurrence INTEGER)"); //a table in the database named 'contacts' will be created if it does not exist
        //sqLiteDatabase.execSQL
    }

    private Boolean entryMessageRaised = false;
    private Boolean exitMessageRaised = false;
    private Boolean rangingMessageRaised = false;

    @Override
    public void onBeaconServiceConnect(){
        Log.d(TAG, "onBeaconServiceConnect called");

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                System.out.println("DID ENTER REGION");
                //if (!entryMessageRaised) {
                    //todo: change logic later
                    ShowAlert("did Enter Region", "Entering Region" + region.getUniqueId() +
                            "Beacon detected UUID/major/minor:" + region.getId1()+"/"+region.getId2()+"/"+region.getId3());
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
                            "Beacon detected UUID/major/minor:" + region.getId1()+"/"+region.getId2()+"/"+region.getId3());
                //System.out.println(beaconRegion.getId1());
                //System.out.println(beacon.get);
                //System.out.println(beacon.getid1);
                    exitMessageRaised = true;
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
                    for (Beacon beacon: beacons) {
                        //ShowAlert("didExitRangeRegion", "Ranging region" + region.getUniqueId() +
                          //      " Beacon detected UUID/major/minor:" + beacon.getId1() + "/" +
                            //    beacon.getId2() + "/" + beacon.getId3());
                        //pubbeacon = beacon;
                        System.out.println(beacon.getId1());
                        System.out.println("Here is your beacon");
                        System.out.println(beacon);
                        System.out.println("That was the beacon");
                        Log.d(TAG,"writing to database");
                        saveclosecontacts2(beacon);
                        Log.d(TAG,"writing to databse complete");
                        //public pubbeacon = beacon.getId1();
                        //getClass(beacon);
                        //Region beaconRegionactual = new Region("MyBeaconStuff", beacon.getId1(), beacon.getId2(), beacon.getId3());
                    //}
                        System.out.println(beacons.isEmpty());
                        rangingMessageRaised = true;
                        //beaconreal = beacon;
                //}

                }

            }
        });
    }

    private void startBeaconMonitoring(){
        Log.d(TAG, "startBeaconMonitoring called");
        System.out.println("START BEACON MONITORING FUNCTION");
        try{
            //beaconRegion = new Region("MyBeacons", Identifier.parse("0AC59CA4-DFA6-442C-8C65-22247851344C"),
                   // Identifier.parse("4"),Identifier.parse("200"));
            beaconRegion = new Region("MyBeaconStuff",null,null,null);
            beaconManager.startMonitoringBeaconsInRegion(beaconRegion);
            beaconManager.startRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void stopBeaconMonitoring(){
        System.out.println("Stop Beacon Monitoring");
        Log.d(TAG,"stopBeaconMonitoring called");
        /*try{
            beaconManager.stopMonitoringBeaconsInRegion(beaconRegion);
            beaconManager.stopRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        } */
    }

    //don't use, use saveCloseContacts2 instead
    public void saveCloseContacts(Beacon beacon){
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
        beaconidstr = "'"+ beaconidstr + "'";


        tobeputinsql = "INSERT INTO contacts VALUES(" + beaconidstr + ", 1);"; //works like a charm: https://stackoverflow.com/a/12472295/10949995
        Log.d(TAG, "onCreate: sql is" + tobeputinsql);
        sqLiteDatabase.execSQL(tobeputinsql);

        //just for development purposes, to confirm it is written to the DB
        Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM contacts;", null);
        if(query.moveToFirst()) { //means equals to true, no need to specify
            do {
                String name = query.getString(0);
                int phone = query.getInt(1);
                //String email = query.getString(2);
                Toast.makeText(this, "SAVED TO DATABASE " +
                        "BluetoothID =" + name + " occurrence " + phone, Toast.LENGTH_LONG).show();

            } while(query.moveToNext());


        }
        query.close();
        sqLiteDatabase.close();
    }

    public void saveclosecontacts2(Beacon beacon){
        Identifier beaconid1; //beaconid1
        int occurrence = 0; // initalize
        SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);

        //for testing purposes only
        //
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS contacts;");
        //
        //

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(beaconid TEXT,occurrence INTEGER)"); //a table in the database named 'contacts' will be created if it does not exist

        //declaring the variable 'tobeputinsql' as the variable to store the commands
        String tobeputinsql;

        //formating the beaconid1 value for the database, can be simplified later
        beaconid1 = beacon.getId1();
        String beaconidstr = beaconid1.toString();
        String bidnoquotes = beaconidstr;
        beaconidstr = "'"+ beaconidstr + "'";

        Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM contacts;", null);

        //todo:need to add an array and counter/ dictionary type thing to bind occurence number with beaconid or things will be messed up.
        //todo:issues when two beacons are together
        //Map<String,Integer>occurrencedict = new HashMap<String,Integer>();
        if(query.moveToFirst()) { //means equals to true, no need to specify
            do {
                String beaconid = query.getString(0);
                System.out.println(beaconid);
                System.out.println(bidnoquotes);
                //System.out.println(beaconid == bidnoquotes);
                //System.out.println( "0ac59ca4-dfa6-442c-8c65-22247851344c" == "0ac59ca4-dfa6-442c-8c65-22247851344c" );
                //System.out.println(beaconid.equals(bidnoquotes));
                //System.out.println(beaconid.equals(beaconidstr));
                System.out.println("the above");
                if(beaconid.equals(bidnoquotes)) {
                    System.out.println("hereherehere");
                    occurrence = query.getInt(1);
                    //occurrencedict.put(beaconid, occurrence);
                    Toast.makeText(this, "BEFORE SAVED TO DATABASE " +
                            "BluetoothID =" + beaconid + " occurrence " + occurrence, Toast.LENGTH_LONG).show();
                    TextView dbtextview = (TextView) findViewById(R.id.dbtextview);
                    dbtextview.setText("BEFORE SAVED TO DATABASE " + "BluetoothID =" + beaconid + " occurrence " + occurrence);
                }
            } while(query.moveToNext());
        }

        //saving values to DB
        //tobeputinsql = "INSERT OR IGNORE INTO contacts VALUES(" + beaconidstr + ", 0);"; //works like a charm: https://stackoverflow.com/a/12472295/10949995
        //sqLiteDatabase.execSQL(tobeputinsql);
        //tobeputinsql = "UPDATE contacts SET occurrence = occurrence + 1";
        //tobeputinsql = "UPDATE contacts SET occurrence = (occurrence + 50)";
                //" WHERE beaconid = '0ac59ca4-dfa6-442c-8c65-22247851344c'";

        int newoccurrence = occurrence + 1;
        tobeputinsql = "INSERT OR REPLACE INTO contacts VALUES(" + beaconidstr + "," + newoccurrence + ");";
        Log.d(TAG, "onCreate: sql is" + tobeputinsql);
        sqLiteDatabase.execSQL(tobeputinsql);

        //stopping query
        query.close();
        sqLiteDatabase.close();

    }
    //
}
