package com.example.beaconwithoracletrainingvideo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    private static final String TAG = "MainActivity";

    private Button startButton; //no button created in gradle //todo: create button in gradle
    private Button stopButton; //no button created in gradle //todo: create button in gradle

    private BeaconManager beaconManager = null;
    private Region beaconRegion = null;

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
    private void ShowAlert(final String title, final String message){
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
        startButton.setOnClickListener((v)->{startBeaconMonitoring();});//todo: green text
        stopButton.setOnClickListener((v)->{stopBeaconMonitoring();});//todo: green text

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout((ALTBEACON_LAYOUT))); //SWITCH TO IBEACON LATER //Todo: switch to IBeacon
        beaconManager.bind(this);
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
                if (!entryMessageRaised) {
                    //todo: change logic later
                    ShowAlert("did Enter Region", "Entering Region" + region.getUniqueId() +
                            "Beacon detected UUID/major/minor:" + region.getId1()+"/"+region.getId2()+"/"+region.getId3());
                    entryMessageRaised = true;
                }
            }

            @Override
            public void didExitRegion(Region region) {
                System.out.println("DID EXIT REGION");
                if (!exitMessageRaised) {
                    //todo: change logic later
                    ShowAlert("did Exit Region", "Exiting Region" + region.getUniqueId() +
                            "Beacon detected UUID/major/minor:" + region.getId1()+"/"+region.getId2()+"/"+region.getId3());
                    exitMessageRaised = true;
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(!rangingMessageRaised && beacons != null && !beacons.isEmpty()) {
                    for (Beacon beacon: beacons) {
                        ShowAlert("didExitRegion", "Ranging region" + region.getUniqueId() +
                                " Beacon detected UUID/major/minor:" + beacon.getId1() + "/" +
                                beacon.getId2() + "/" + beacon.getId3());
                    }
                    rangingMessageRaised = true;
                }
            }
        });
    }

    private void startBeaconMonitoring(){
        Log.d(TAG, "startBeaconMonitoring called");
        System.out.println("START BEACON MONITORING FUNCTION");
        try{
            beaconRegion = new Region("MyBeacons", Identifier.parse("0AC59CA4-DFA6-442C-8C65-22247851344C"),
                    Identifier.parse("4"),Identifier.parse("200"));
            beaconManager.startMonitoringBeaconsInRegion(beaconRegion);
            beaconManager.startRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void stopBeaconMonitoring(){
        System.out.println("Stop Beacon Monitoring");
        Log.d(TAG,"stopBeaconMonitoring called");
        try{
            beaconManager.stopMonitoringBeaconsInRegion(beaconRegion);
            beaconManager.stopRangingBeaconsInRegion(beaconRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
