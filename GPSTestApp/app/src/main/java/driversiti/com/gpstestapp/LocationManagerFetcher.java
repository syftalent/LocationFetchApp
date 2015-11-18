package driversiti.com.gpstestapp;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;


/**
 * Created by yifeishen on 9/23/15.
 */
public class LocationManagerFetcher {
    private static String LOG_TAG = LocationManagerFetcher.class.getSimpleName();
    private static int MIN_TIME = 300;
    private static int MIN_DISTANCE = 0;

    private Context mContext;
    private LocationChangeListener mCallback;
    private LocationListener mGPSLocationListener;
    private LocationListener mNetworkLocationListener;

    private LocationManager mLocationManager;
    private boolean mGPSAvailable = false;
    private boolean mNetworkAvailable = false;

    LocationManagerFetcher(Context context, LocationChangeListener callback) {
        mContext = context;
        mCallback = callback;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startFetchLocationByGps(){
        mGPSAvailable = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        mCallback.onGPSStausChanged(mGPSAvailable);

        Log.i(LOG_TAG, "GPS Listener Created");
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE,
                mGPSLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(LOG_TAG, "GPS Location changed");
                        mCallback.onLocationChanged(location, "GPS");
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                        Log.i(LOG_TAG, "GPS onStatusChanged");
                        switch (i) {
                            case LocationProvider.AVAILABLE:
                                Log.i(LOG_TAG, "GPS become available");
                                mGPSAvailable = true;
                                break;
                            case LocationProvider.OUT_OF_SERVICE:
                                Log.i(LOG_TAG, "GPS get out of service");
                                mGPSAvailable = false;
                                break;
                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                Log.i(LOG_TAG, "GPS get temporarily unavailable");
                                mGPSAvailable = false;
                                break;
                        }
                        mCallback.onGPSStausChanged(mGPSAvailable);
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                        Log.i(LOG_TAG, "GPS onProviderEnabled");
                        mGPSAvailable = true;
                        mCallback.onGPSStausChanged(mGPSAvailable);
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        Log.i(LOG_TAG, "GPS onProviderDisabled");
                        mGPSAvailable = false;
                        mCallback.onGPSStausChanged(mGPSAvailable);
                    }
                });
        Log.i(LOG_TAG, "GPS Enabled");
    }

    public void startFetchLocationByNetwork(){
        mNetworkAvailable = mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        mCallback.onNetworkStausChanged(mNetworkAvailable);

        Log.i(LOG_TAG, "Network Listener Created");
        mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE,
                mNetworkLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(LOG_TAG, "Network Location changed");
                        mCallback.onLocationChanged(location, "Network");
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                        Log.i(LOG_TAG, "Network onStatusChanged");
                        switch (i) {
                            case LocationProvider.AVAILABLE:
                                Log.i(LOG_TAG, "Network become available");
                                mNetworkAvailable = true;
                                break;
                            case LocationProvider.OUT_OF_SERVICE:
                                Log.i(LOG_TAG, "Network get out of service");
                                mNetworkAvailable = false;
                                break;
                            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                Log.i(LOG_TAG, "Network get temporarily unavailable");
                                mNetworkAvailable = false;
                                break;
                        }
                        mCallback.onNetworkStausChanged(mNetworkAvailable);
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                        Log.i(LOG_TAG, "Network onProviderEnabled");
                        mNetworkAvailable = true;
                        mCallback.onNetworkStausChanged(mNetworkAvailable);
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        Log.i(LOG_TAG, "Network onProviderDisabled");
                        mNetworkAvailable = false;
                        mCallback.onNetworkStausChanged(mNetworkAvailable);
                    }
                });
        Log.i(LOG_TAG, "Network Enabled");
    }

    public void stopFetchLocationByGps(){
        try {
            mLocationManager.removeUpdates(mGPSLocationListener);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "gps provider" + e.toString());
        }finally {
            Log.i(LOG_TAG, "remove gps provider");

            mGPSLocationListener = null;
            mGPSAvailable = false;
            mCallback.onGPSStausChanged(mGPSAvailable);
        }
        Log.i(LOG_TAG, "GPS Listener Removed");
    }

    public void stopFetchLocationByNetwork(){
        try {
            mLocationManager.removeUpdates(mNetworkLocationListener);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "network provider" + e.toString());
        } finally {
            Log.i(LOG_TAG, "remove network provider");

            mNetworkLocationListener = null;
            mNetworkAvailable = false;
            mCallback.onNetworkStausChanged(mNetworkAvailable);
        }
        Log.i(LOG_TAG, "Network Listener Removed");
    }

    public interface LocationChangeListener {
        public void onLocationChanged(Location location, String provider);
        public void onGPSStausChanged(boolean GPSAvailable);
        public void onNetworkStausChanged(boolean networkAvailable);
    }
}
