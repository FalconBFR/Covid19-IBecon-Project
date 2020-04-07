package com.example.beaconwithoracletrainingvideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(SensorRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        System.out.println(context + "SensorRestarterBroadcastReciever.java context");
        context.startService(new Intent(context, beaconservice.class));;
    }
}
