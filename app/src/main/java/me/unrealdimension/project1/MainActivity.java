package me.unrealdimension.project1;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.os.AsyncTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.DataSource;

import java.util.concurrent.TimeUnit;

import static android.os.AsyncTask.Status;
/*
import com.google.api.client;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.*;
 */


public class MainActivity extends ActionBarActivity {
    private Chronometer mChronometer;
    long stopTime = 0;
    long baseTime = SystemClock.elapsedRealtime()-stopTime; // The initial base time for when the timer is first started.
    long elapsedTime;
    private static final int REQUEST_OAUTH = 1;

    /**
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    GoogleApiClient mClient = null;
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit?")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

      /*  mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
       */
        buildFitnessClient();


        setContentView(R.layout.activity_main);
        //mChronometer = (Chronometer)findViewById(R.id.chronometer);

    }
/*    @Override
    public void onConnected(Bundle bundle){
        final String TAG = MainActivity.class.getSimpleName();
        Log.i(TAG, "CONNECTED");
        invokeFitnessAPIs();
    }
    public void invokeFitnessAPIs(){
        DataSourceListener listener = new DataSourceListener(){
         @Override
            public void onEvent(DataPoint dataPoint){
             for (DataPoint.Field field : dataPoint.getDataType().getFields()){
                 Value val = dataPoint.getValue(field);
             }
         }
        };


        SensorRequest req = new SensorRequest.Builder()
        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .setSamplingRate(1, TimeUnit.SECONDS)
        .build();

        PendingResult <Status> regResult = Fitness.SensorsApi.register(client, req, listener) ;
        regResult.setResultCallback(resultCallback);
    }
*/
    /**
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or having
     *  multiple accounts on the device and needing to specify which account to use, etc.

*/
    

    private void buildFitnessClient() {
        // Create the Google API Client
        final String TAG = MainActivity.class.getSimpleName();
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.


                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            MainActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(MainActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    protected void onStart() {
        final String TAG = MainActivity.class.getSimpleName();
        super.onStart();
        // Connect to the Fitness API
        Log.i(TAG, "Connecting...");
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_info:
                // info item was selected
                Intent intent = new Intent(this, help.class);
                startActivity(intent);
                this.finish();
                return true;
            case R.id.menu_exit:
                // exit item was selected
                this.finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startTimer(View v) {
        // TODO use setBase method of the chronometer the restart it from the last time that it was started from
        //baseTime = SystemClock.elapsedRealtime() - stopTime;
        mChronometer.setBase(baseTime);
        mChronometer.start();
    }

    public void resetTimer(View v) {
        mChronometer.stop();
        baseTime = 0;
        stopTime = 0;
        mChronometer.setBase(0);
    }

    public void stopTimer(View v) {
        mChronometer.stop();
        //baseTime = SystemClock.elapsedRealtime() - stopTime;
        //stopTime = Long.parseLong(mChronometer.getText().toString());
    }


}
