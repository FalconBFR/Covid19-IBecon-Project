package com.example.beaconwithoracletrainingvideo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.altbeacon.beacon.Region;

/*public class beaconcontrolservice extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/

    public class beaconcontrolservice extends Service {
        public void setContext(Context context) {
            this.context = context;
        }

        private Looper serviceLooper;
        private ServiceHandler serviceHandler;

        // Handler that receives messages from the thread
        private final class ServiceHandler extends Handler {
            public ServiceHandler(Looper looper) {
                super(looper);
            }
            @Override
            public void handleMessage(Message msg) {
                // Normally we would do some work here, like download a file.
                // For our sample, we just sleep for 5 seconds.
                    //Thread.sleep(5000);
                while(true) {
                    beaconcontrol beaconcontrol = new beaconcontrol();
                    beaconcontrol.startBeaconMonitoring();
                }
                // Stop the service using the startId, so that we don't stop
                // the service in the middle of handling another job
                //stopSelf(msg.arg1);
            }
        }
        Context context;
        @Override
        public void onCreate() {
            // Start up the thread running the service. Note that we create a
            // separate thread because the service normally runs in the process's
            // main thread, which we don't want to block. We also make it
            // background priority so CPU-intensive work doesn't disrupt our UI.
            HandlerThread thread = new HandlerThread("ServiceStartArguments");
            //HandlerThread thread = new HandlerThread("ServiceStartArguments",
              //      Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            context = this;

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = thread.getLooper();
            serviceHandler = new ServiceHandler(serviceLooper);
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
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    //}

    /*private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }
    @Nullable
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MainActivity MainActivity = new MainActivity();
        //Region beaconRegion = new Region("MyBeaconStuff", null, null, null);
        //MainActivity.startBeaconMonitoring();
        beaconcontrol beaconcontrol = new beaconcontrol();

        //return super.onStartCommand(intent, flags, startId);
        //Region beaconRegion = new Region("MyBeaconStuff", null, null, null);
        beaconcontrol.startBeaconMonitoring();
        return START_STICKY;
    }

    @Nullable
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    /*@Override
    public boolean bindService(Intent intent, ServiceConnection conn, int mode) {
        return context.bindService(intent, conn, mode);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        context.unbindService(conn);
    }*/

}
