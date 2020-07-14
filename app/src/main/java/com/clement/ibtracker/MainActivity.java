package com.clement.ibtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.altbeacon.beacon.BeaconManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //initialize
    private static final String TAG = "MainActivity";

    public Context context;

    private Button startButton;
    private Button stopButton;
    private Button dbupdate_button;
    private Button risk_status_button;
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

        //making sure that a UUID is present, else: grabbing one from server
        autoloadtocheckuuid();

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        //loadtheill = findViewById(R.id.loadtheill);
        dbupdate_button = findViewById(R.id.dbupdate);
        //startButton.setOnClickListener((v) -> { startBeaconMonitoring(); });
        startButton.setOnClickListener((v) -> {
            //Turning On Bluetooth to avoid errors:
            enableBT();
            startService(new Intent(this, beaconservice.class));
            //which view to load at start (ill patients or full uuid view
            //loaddbview(this);
            //loadillclosecontactsview(getBaseContext());
            //loaddbview(this);
            //loadillclosecontactsview(this);
            loaduuid(); //for the view
        });
        System.out.println("OMGOMGOMG");
        //stopButton.setOnClickListener((v) -> { stopBeaconMonitoring(); });
        stopButton.setOnClickListener((v) -> {
            stopService(new Intent(this, com.clement.ibtracker.beaconservice.class));
        });

        dbupdate_button.setOnClickListener((v) -> {
            //loaddbview(this);
            //loaduuid(); //for the view
            startActivity(new Intent(this,all_contacts.class));
        });

        //new big colorful button to display risk level
        risk_status_button = findViewById(R.id.circular_risk_status_button);

        risk_status_button.setOnClickListener((v) -> {
            //loadillclosecontactsview(this);
            //loaduuid(); //for the view
            startActivity(new Intent(this,at_risk.class));
        });




                //TextView dbtextview = findViewById(R.id.dbtextview);

        yourassigneduuid = findViewById(R.id.yourassigneduuid);

        System.out.println("getBaseContextOnCreate" + getBaseContext());
        basecontext = getBaseContext();

        //loaddbview(this);
        //loadillclosecontactsview(this);

        DownoladData downloadData = new DownoladData();
        downloadData.execute("");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loaduuid(); //for the view


    }

    public void enableBT() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
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

    public String datettimeprocessingselfwrite(long currenttimeinms) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:MM");
        Date date = new Date(currenttimeinms);
        String time = simpleDateFormat.format(date);
        Log.d("datetimeprocessing", time);
        return time;
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
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    private class DownoladData extends AsyncTask<String, Void, List> {
        private static final String TAG = "DownloadedData";

        @Override
        protected void onPostExecute(List list) {
            Log.d(TAG, "onPostExecuted:Param is" + list);
            super.onPostExecute(list);
        }

        @Override
        protected List doInBackground(String... strings) {
            List<com.clement.ibtracker.Patientdata> patientsdata = new ArrayList<>();
            List<String> patientsdatauuid = new ArrayList<>();
            Log.d(TAG, "Arrived in do in background (Async Task)");
            //getting data in background from link
            InputStream input = null;
            try {
                Log.d(TAG, "do In Background: Starts download");
                input = new URL("http://206.189.39.40/static/cases.csv").openStream();
                System.out.println("input" + input);
            } catch (IOException e) {
                //Means that the server is either down or the user doesn't have internet connection
                Log.e(TAG, "download error");
                //e.printStackTrace();
                List<String> faileddownloaduserfeedback = new ArrayList<String>();
                faileddownloaduserfeedback.add("!!!!!! ***** Check Your Internet Connection." +
                        " Or else, the server is currently down. Sorry for the inconvinience caused ****** !!!");
                ShowAlert("Nothing to worry about but No Internet Connection/Server is Down "
                        , "Please continue to turn on this app. Again, let me repeat that we don't upload your data. However, " +
                                "I don't know who is sick yet. Please let me repeat that this app will not detect every single close contact. " +
                                "Take necessary precautions please. This is not a replacement for Social Distancing or any other measures you are " +
                                "currently taking/asked by the goverment to take.");
                savingcctotxt(faileddownloaduserfeedback);
                //better user feedback - big circular button
                risk_status_button.setBackgroundColor(Color.YELLOW);
                risk_status_button.setText("No Internet Connection. Please check your internet connection. If this error persists, please email the dev team to check if the server is down. 無網絡連結，無法確認您有沒有感染的風險。請確保您有打開Wifi/流動數據。");
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
                        Log.d(TAG, "Querey-ing");
                        do {
                            String beaconid = query.getString(0);
                            Log.d(TAG,"query String line (line 337)" + beaconid);
                            Integer occurrence = query.getInt(1);
                            Double beacondist = query.getDouble(2);
                            if (beaconid.equals(tokens[0])) { // never use string == string. Use string.equals(string) beaconid == tokens[0] is wrong
                                Log.e(TAG, "Problem here. Confimed case" + patientsdata);

                                //setting risk button
                                risk_status_button.setBackgroundColor(Color.RED);
                                risk_status_button.setText("You May Be At Risk of An Infection. Click HERE to learn more 你可能被感染，詳情按此");
                                //todo: limitation: Running Sequence issue. The color of the button may not be displayed correctly if the user jumped between activities. To solve this, save current color in a .txt file
                                //


                                //problematicpeole.append("\n" + beaconid + ":   " + occurrence + ":   " + beacondist + "m");
                                com.clement.ibtracker.Patientdata patientdata = new com.clement.ibtracker.Patientdata(); //** Patient data is data for a single patient. Patients Data is data for all the close contact confirmed cases
                                patientdata.setUuid(tokens[0]);
                                patientdata.setSituation(tokens[1]);
                                patientdata.setDate(tokens[2]);
                                System.out.println("patientdata" + patientdata.getUuid());
                                // todo: limitation : If multiple records of the Same UUID exists on the server,
                                //  it cannot be handeled. It will print out all the uuids. However, if there are multiple detections of the Same UUID locally, it is not a problem
                                System.out.println("Contains patient???" + Arrays.asList(patientsdata).contains(patientdata));
                                if (!Arrays.asList(patientsdata).contains(patientdata)) {
                                    patientsdata.add(patientdata);
                                    patientsdatauuid.add(patientdata.getUuid());
                                    System.out.println("patientsdatalist" + patientsdata);
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
                } catch (SQLiteException e) {
                    break;
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                } catch (IOException e) {
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

    public void savingcctotxt(List patientsdata) {
        FileOutputStream fos = null;
        try {
            Context context = this;
            String file_name = MainActivity.this.getFilesDir()
                    .getAbsolutePath() + "/closecontacts.txt";
            //fos = openFileOutput("closecontacts.txt", MODE_PRIVATE);

            fos = new FileOutputStream(file_name, false);
            //fos.write(text.getBytes());
            String tobewrittendata;
            tobewrittendata = "\n";
            fos.write(tobewrittendata.getBytes());
            tobewrittendata = patientsdata.toString();
            tobewrittendata = tobewrittendata.replace("}", "\n");
            fos.write(tobewrittendata.getBytes());
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

    public class NewUUID extends AsyncTask<String, Void, List> {
        private static final String TAG = "DownloadedData";

        @Override
        protected void onPostExecute(List list) {
            Log.d(TAG, "onPostExecuted:Param is" + list);
            super.onPostExecute(list);
        }

        @Override
        protected List doInBackground(String... strings) {
            List<com.clement.ibtracker.Patientdata> patientsdata = new ArrayList<>();
            Log.d(TAG, "Arrived in do in background (Async Task)");
            //getting data in background from link
            InputStream input = null;
            try {
                Log.d(TAG, "do In Background: NEWUUID!!!!!!!!!");
                input = new URL("http://206.189.39.40/uuid/officialnew").openStream();
                System.out.println("new uuid buffer input" + input);
                Log.d(TAG, "new uuid buffer input" + input);
            } catch (IOException e) {
                //todo: A better feedback to the user about the issue of lack of internet connection
                //Means that the server is either down or the user doesn't have internet connection
                Log.e(TAG, "download error");
                //e.printStackTrace();
                List<String> faileddownloaduserfeedback = new ArrayList<String>();
                faileddownloaduserfeedback.add("!!!!!! ***** Check Your Internet Connection." +
                        " Or else, the server is currently down. Sorry for the inconvinience caused ****** !!!");
                ShowAlert("Nothing to worry about but No Internet Connection/Server is Down "
                        , "Please try to ensure that internet connection can be guranteed and " +
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

        void savinguuidtotxt(String newuuid) {
            FileOutputStream fos = null;
            try {
                Context context = MainActivity.this;
                String file_name = MainActivity.this.getFilesDir()
                        .getAbsolutePath() + "/beaconid.txt";
                //fos = openFileOutput("closecontacts.txt", MODE_PRIVATE);

                fos = new FileOutputStream(file_name, false);
                fos.write(newuuid.getBytes());
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
            BEACONUUID = BEACONUUID.replaceAll("\\s+", "");
            //System.out.println(BEACONUUID=="10000000-0000-0000-0000-000000000000");
            System.out.println("+++++++++");

        } catch (FileNotFoundException e) {
            //Implement cannot find UUID function:
            System.out.println("FNF e");
            Log.d("beaconservice", "CanotFindUUID.txt file. Running choose uuid and saving from server");
            Log.d(TAG, "notifying user to turn on their wifi or mobile netowrk");
            Toast.makeText(this, R.string.Cannotuuidnew, Toast.LENGTH_LONG);
            NewUUID newUUID = new NewUUID();
            newUUID.execute("");
            Log.d("beaconservice", "NewUUID Ran sucessfully");
            //Re-Running Function - delay to avoid it calling for a new uuid multiple times since it takes time to save the new uuid to save to the .txt file
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 0.05s = 50ms
                    autoloadtocheckuuid(); //run the function again since the UUID needs to be loaded into the system
                }
            }, 50);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (NullPointerException e) {
                //another spot to implement UUID not found function but I am choosing another place to run the newuuid func
                Log.d("beaconservice", "This should not happen! in autoload function Null Pointer Exception e. Should be rulled out by FNe");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return BEACONUUID;
    }
}
