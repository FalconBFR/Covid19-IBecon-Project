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
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    private static final String TAG = "MainActivity";

    private Button startButton;
    private Button stopButton;

    private BeaconManager beaconManager = null;
    private Region beaconRegion = null;

    //self
    private Identifier beaconid1; //beaconid1

    private static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"; //Todo: switch to IBeacon Later

    /*private void showAlert(final String title, final String message) {
        runOnUiThread((){
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(message);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"OK");
                View.OnClickListener
        });
    } */
   /* private void ShowAlert(final String title, final String message){
        System.out.println("SHOW ALERT 47");
        runOnUiThread (new Thread(new Runnable() {
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(message);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        }));
    }*/
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
                        ShowAlert("didExitRangeRegion", "Ranging region" + region.getUniqueId() +
                                " Beacon detected UUID/major/minor:" + beacon.getId1() + "/" +
                                beacon.getId2() + "/" + beacon.getId3());
                        //pubbeacon = beacon;
                        System.out.println(beacon.getId1());
                        System.out.println("Here is your beacon");
                        System.out.println(beacon);
                        System.out.println("That was the beacon");
                        Log.d(TAG,"writing to database");
                        saveCloseContacts(beacon);
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


    public void saveCloseContacts(Beacon beacon){
        SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        //for testing purposes only
        //
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS contacts;");
        //
        //

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(beaconid TEXT,occurrence INTEGER)"); //a table in the database named 'contacts' will be created if it does not exist
        String tobeputinsql;
        beaconid1 = beacon.getId1();
        String beaconidstr = beaconid1.toString();
        beaconidstr = "'"+ beaconidstr + "'";

        System.out.println(beaconidstr);
        //System.out.println(beaconidstr.getClass());
        //beaconid1 = toString(beaconid1);

        String simpletest = "'abc'";

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
        SQLiteDatabase sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);
        //for testing purposes only
        //
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS contacts;");
        //
        //

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(beaconid TEXT,occurrence INTEGER)"); //a table in the database named 'contacts' will be created if it does not exist
        String tobeputinsql;
        beaconid1 = beacon.getId1();
        String beaconidstr = beaconid1.toString();
        System.out.println(beaconidstr); //for testing purposes

        tobeputinsql = "INSERT INTO contacts VALUES('\''+ beaconid1+'\'', 1);";
        Log.d(TAG, "onCreate: sql is" + tobeputinsql);
        sqLiteDatabase.execSQL(tobeputinsql);


    }

}
