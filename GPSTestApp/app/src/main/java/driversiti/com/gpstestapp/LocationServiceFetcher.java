package driversiti.com.gpstestapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by yifeishen on 9/25/15.
 */
public class LocationServiceFetcher implements ConnectionCallbacks,  OnConnectionFailedListener, ResultCallback<Status> {
    private static String LOG_TAG = LocationServiceFetcher.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private LocationListener mLocationListener;
    private LocationRequest mLocationRequest;
    private ServiceChangeListener mCallback;
    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;

    private boolean mRequestingLocationUpdates = false;
    private boolean mResolvingError = false;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 1001;

    LocationServiceFetcher(Context context, ServiceChangeListener callback){
        mContext = context;
        mCallback = callback;
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    //***************************Main switcher to the services*******************
    public void startServices(){
        if (!mResolvingError) {
            Log.i(LOG_TAG,"Service connect api client");
            mGoogleApiClient.connect();
        }else{
            Toast.makeText(mContext,"Can not connect to google api client",Toast.LENGTH_SHORT);
            return;
        }

        //Elements Location Service needed.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(LOG_TAG,"Service Location have changed");
                mCallback.onLocationChanged(location);
            }
        };

        //Elements Activity Recognition needed
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver,
                new IntentFilter(ActivityDetectionConstants.BROADCAST_ACTION));

        //start services if google api is connected
        if(mRequestingLocationUpdates) {
            startLocationUpdates();
            startRequestActivityUpdates();
        }
    }

    //**********************Location service**********************
    public void stopServices(){
        if(mRequestingLocationUpdates) {
            stopLocationUpdates();
            stopRequestActivityUpdates();
        }else{
            Toast.makeText(mContext,"Google API Client disconnected",Toast.LENGTH_SHORT);
        }
        mGoogleApiClient.disconnect();
    }

    private void startLocationUpdates() {
        Log.i(LOG_TAG,"Service start Fetching location");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    public void stopLocationUpdates(){
        mRequestingLocationUpdates = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, mLocationListener);
        Log.i(LOG_TAG, "Location Service have been removed");
        mCallback.onLocationChanged(null);
    }

    //*********************Activity Detection*********************
    public void startRequestActivityUpdates() {
        Log.i(LOG_TAG, "start activity request");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                ActivityDetectionConstants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    public void stopRequestActivityUpdates(){
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        );
        mCallback.onActivityChanged("Null");
        Log.i(LOG_TAG, "Activity service have been removed");
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Log.i(LOG_TAG, "create pending activity");

        Intent intent = new Intent(mContext, ActivityDetectionIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Toast.makeText(mContext,"Activity Detected", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(LOG_TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG,"received broadcast");
            ArrayList<DetectedActivity> updatedActivities =
                    intent.getParcelableArrayListExtra(ActivityDetectionConstants.ACTIVITY_EXTRA);
            DetectedActivity mostConfidentActivity = getMostConfidentActivity(updatedActivities);
            if(mostConfidentActivity == null){
                mCallback.onActivityChanged(null);
            }else {
                mCallback.onActivityChanged(ActivityDetectionConstants.getActivityString(
                        mContext, mostConfidentActivity.getType())+ " " + mostConfidentActivity.getConfidence()+"%");
            }
        }
    }

    public DetectedActivity getMostConfidentActivity(ArrayList<DetectedActivity> updatedActivities){
        Log.i(LOG_TAG, "get most confident activity");
        if(updatedActivities.size() == 0){
            return null;
        }else {
            DetectedActivity maxDa = updatedActivities.get(0);
            int maxConfidence = maxDa.getConfidence();
            for(DetectedActivity da:updatedActivities){
                if(da.getConfidence() > maxConfidence){
                    maxDa = da;
                }
            }
            return maxDa;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(LOG_TAG, "GoogleApiClient connection succeed");
        mRequestingLocationUpdates = true;
        startLocationUpdates();
        startRequestActivityUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspend: reason: " + cause);
        mRequestingLocationUpdates = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "Google service onConnectionFailed()" + connectionResult.toString());
        Toast.makeText(mContext, "Can not connect to google api client", Toast.LENGTH_SHORT);

        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult != null) {
            Log.w(LOG_TAG, "Connection failed, unable to resolve: connectionResult: " + connectionResult.getErrorCode());
        }
        if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult((Activity) mContext, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "playServices connection failed");
            }
        } else {
            Log.e(LOG_TAG, "connectionResult does not have status code, ErrorCode: " + connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }




    public interface ServiceChangeListener {
        public void onLocationChanged(Location location);
        public void onActivityChanged(String activity);
    }
}
