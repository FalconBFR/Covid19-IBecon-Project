package com.clement.beaconwithoracletrainingvideo;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

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
import java.util.concurrent.TimeUnit;

public class beaconservice extends Service implements BeaconConsumer {

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    //public Context context = getApplicationContext(); //not needed.

    private static final String TAG = "MainActivity";



    private Button startButton;
    private Button stopButton;


    public BeaconManager beaconManager = null;
    //public Region beaconRegion = null; //monitoring region

    //private static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"; //Todo: switch to IBeacon Later
    private static final String IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    //

    //UUID management
    final String BEACONIDTXT = "beaconid.txt";
    EditText mEditText;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

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
                  //      "Beacon detected UUID/major/minor:" + region.getId1() + "/" + region.getId2() + "/" + region.getId3());
                //entryMessageRaised = true;
                //}
                //this is how you acess beacon  - System.out.println(beaconreal.getId1());
            }

            @Override
            public void didExitRegion(Region region) {
                System.out.println("DID EXIT REGION");
                //if (!exitMessageRaised) {
                //todo: change logic later
                //ShowAlert("did Exit Region", "Exiting Region" + region.getUniqueId() +
                  //      "Beacon detected UUID/major/minor:" + region.getId1() + "/" + region.getId2() + "/" + region.getId3());
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

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            Region beaconRegion = new Region("MyBeaconStuff", null, null, null);
            System.out.println(beaconRegion);
            Log.d(TAG, "startBeaconMonitoring called");
            System.out.println("START BEACON MONITORING FUNCTION");
            try {
                System.out.println("uno");
                System.out.println(beaconRegion);
                System.out.println("dos");
                beaconManager.startMonitoringBeaconsInRegion(beaconRegion);
                System.out.println("tres");
                beaconManager.startRangingBeaconsInRegion(beaconRegion);
                System.out.println("quatro");
                transmitbeacon();
                System.out.println("cinco");
            } catch (RemoteException e) {
                System.out.println("mal");
                e.printStackTrace();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
                //TimeUnit.MINUTES.sleep(1);
            System.out.println("reaches");
            if (Thread.interrupted()) {
                System.out.println("oopsopsohno");
                return;
            }
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments");
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout((IBEACON_LAYOUT))); //SWITCH TO IBEACON LATER //Todo: switch to IBeacon
        beaconManager.bind(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);

        sendBroadcast(broadcastIntent);
    }

    public void transmitbeacon() {
        System.out.println("transmit-uno");
        String BEACONUUID = autoload();
        System.out.println(BEACONUUID);
        System.out.println("transmit-dos");
        //BEACONUUID = "10000000-0000-0000-0000-000000000000";
        System.out.println("^^^^^^^");
        Beacon beacon = new Beacon.Builder()
                .setId1(BEACONUUID)
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x004c)
                .setTxPower(-59)
                .build();
        System.out.println("transmit-tres");
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        System.out.println("transmit-quatro");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        System.out.println("transmit-cinco");
        beaconTransmitter.startAdvertising(beacon);
        System.out.println("transmit-seis");
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

    public void saveclosecontacts2(Beacon beacon, Double distance) {
        System.out.println("savingtodb!In service! Yeah");
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
        //MainActivity mainActivity = new MainActivity();
        //mainActivity.loaddbview(this);

        //loaddbview();

    }
}


