package com.example.beaconwithoracletrainingvideo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import static android.content.Context.MODE_PRIVATE;

public class beaconcontrol extends AppCompatActivity implements BeaconConsumer {

    private Context context = this;
    public void setContext(Context context) {
        this.context = context;
    }

    //initialize
    private static final String TAG = "MainActivity";



    private Button startButton;
    private Button stopButton;


    private BeaconManager beaconManager = null;
    //public Region beaconRegion = null; //monitoring region

    //private static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"; //Todo: switch to IBeacon Later
    private static final String IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    //

    //UUID management
    final String BEACONIDTXT = "beaconid.txt";
    EditText mEditText;

    public Context mcontext;
    //public Context context;
    public void grabcontext(Context context){
        mcontext = context;
        //context = context;
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
                //ShowAlert("did Enter Region", "Entering Region" + region.getUniqueId() +
                        //"Beacon detected UUID/major/minor:" + region.getId1() + "/" + region.getId2() + "/" + region.getId3());
                //entryMessageRaised = true;
                //}
                //this is how you acess beacon  - System.out.println(beaconreal.getId1());
            }

            @Override
            public void didExitRegion(Region region) {
                System.out.println("DID EXIT REGION");
                //if (!exitMessageRaised) {
                //todo: change logic later
               // ShowAlert("did Exit Region", "Exiting Region" + region.getUniqueId() +
                   //     "Beacon detected UUID/major/minor:" + region.getId1() + "/" + region.getId2() + "/" + region.getId3());
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
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(mcontext, beaconParser);
        beaconTransmitter.startAdvertising(beacon);
    }

    public void saveclosecontacts2(Beacon beacon, Double distance) {
        Identifier beaconid1; //beaconid1
        int occurrence = 0; // initalize
        SQLiteDatabase sqLiteDatabase = mcontext.openOrCreateDatabase("sqlite-test-1.db", MODE_PRIVATE, null);

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
                    //Toast.makeText(this, "BEFORE SAVED TO DATABASE " +
                      //      "BluetoothID =" + beaconid + " occurrence " + occurrence + "closest distance" + distance, Toast.LENGTH_LONG).show();
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

        //loaddbview();

    }

    public String autoload() {
        //Context context;
        //context = getApplicationContext();
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
            //System.out.println("\nheyhey");
            //System.out.println(sb.toString());// sb.toString is correct
            //System.out.println("10000000-0000-0000-0000-000000000000");
            //System.out.println(BEACONUUID=="10000000-0000-0000-0000-000000000000");
            BEACONUUID = BEACONUUID.replaceAll("\\s+", "");
            //System.out.println(BEACONUUID=="10000000-0000-0000-0000-000000000000");
            System.out.println("+++++++++");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis!= null){
            try {
                System.out.println("here1223");
                BEACONUUID = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa6";
                fis.close();
                System.out.println("here");
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        }
        return BEACONUUID;
    }

    MainActivity MainActivity = new MainActivity();


}
