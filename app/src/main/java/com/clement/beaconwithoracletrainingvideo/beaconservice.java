package com.clement.beaconwithoracletrainingvideo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
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
import androidx.core.app.NotificationCompat;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class beaconservice extends Service implements BeaconConsumer {

    private static final String CHANNEL_ID = "Detecting Close Contacts" ;
    private NotificationManager mNM;
    private int NOTIFICATION = R.string.local_service_started;

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

        //showing a notif
        //mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.  We put an icon in the status bar.
        //showNotification(getString(R.string.servicerunningnote));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        //for the notification
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Detecting Close Contacts")
                .setContentText("Clement's Beacon App Running as usual. Nothing to worry about")
                .setSmallIcon(R.drawable.notifbar)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

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
        try {
            System.out.println("transmit-uno");
            String BEACONUUID = autoload();
            //if (BEACONUUID.length()!=36){
            //BEACONUUID="00000000-0000-0000-0000-000000000000";
            //}
            System.out.println(BEACONUUID);
            System.out.println("transmit-dos");
            //BEACONUUID = "10000000-0000-0000-0000-000000000000";
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
        } catch (NullPointerException e){
            Toast.makeText(this, "Bluetooth !!! Please Turn On Your Bluetooth!!!", Toast.LENGTH_LONG).show(); //often causes confusion. Made me debug for hours for twice already
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
            //Only here just in case the user did something to the file to avoid a crash
            //This really shouldn't happen :(
            System.out.println("FNF e");
            Log.e("beaconservice","CanotFindUUID.txt file. Running choose uuid and saving from server");
            NewUUID newUUID = new NewUUID();
            newUUID.execute("");
            autoload(); //run the function again since the UUID needs to be loaded into the system
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
            Log.d(TAG,"CRAP AUTOLOAD BACKUP UUID HAPPENED!!! No Failsafe funciton written for this");

        }
        return BEACONUUID;
    }

    public String datettimeprocessingselfwrite(long currenttimeinms){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:MM");
        Date date = new Date(currenttimeinms);
        String time = simpleDateFormat.format(date);
        Log.d("datetimeprocessing",time);
        return time;
    };

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

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(beaconid TEXT,occurrence INTEGER, closestavgdist DOUBLE, dateandtime TEXT)"); //a table in the database named 'contacts' will be created if it does not exist
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

        //formatDateTime(this, String.valueOf(System.currentTimeMillis()))
        int newoccurrence = occurrence + 1;
        tobeputinsql = "INSERT OR REPLACE INTO contacts VALUES(" + beaconidstr + "," + newoccurrence + "," + distance + "," + System.currentTimeMillis() + ");";
        Log.d(TAG, "onCreate: sql is" + tobeputinsql);
        sqLiteDatabase.execSQL(tobeputinsql);

        //stopping query
        query.close();
        sqLiteDatabase.close();
        //MainActivity mainActivity = new MainActivity();
        //mainActivity.loaddbview(this);

        //loaddbview();

    }

    public void savinguuidtotxt(String newuuid){
        FileOutputStream fos = null;
        try {
            Context context = this;
            String file_name = this.getFilesDir()
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
    };

    private class NewUUID extends AsyncTask<String,Void, List> {
        private static final String TAG = "DownloadedData";
        @Override
        protected void onPostExecute(List list) {
            Log.d(TAG,"onPostExecuted:Param is" + list);
            super.onPostExecute(list);
        }

        @Override
        protected List doInBackground(String... strings) {
            List<com.clement.beaconwithoracletrainingvideo.Patientdata> patientsdata = new ArrayList<>();
            Log.d(TAG,"Arrived in do in background (Async Task)");
            //getting data in background from link
            InputStream input = null;
            try {
                Log.d(TAG,"do In Background: NEWUUID!!!!!!!!!");
                input = new URL("http://206.189.39.40:5000/uuid/new").openStream();
                System.out.println("new uuid buffer input"+input);
            } catch (IOException e) {
                //todo: A better feedback to the user about the issue of lack of internet connection
                //Means that the server is either down or the user doesn't have internet connection
                Log.e(TAG, "download error");
                //e.printStackTrace();
                List<String> faileddownloaduserfeedback = new ArrayList<String>();
                return faileddownloaduserfeedback; //is empty
            }
            //reading the data into input stream reader
            Reader reader1 = null;
            try {
                reader1 = new InputStreamReader(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.wtf("NewUUID","Line547");
                e.printStackTrace();
            }

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
    }

    private void showNotification(String tobeshowntext){
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = tobeshowntext;

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notifbar)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.servicerunning))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();



        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}


