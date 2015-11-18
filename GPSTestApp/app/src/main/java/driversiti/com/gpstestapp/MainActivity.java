package driversiti.com.gpstestapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends Activity implements LocationManagerFetcher.LocationChangeListener,
        LocationServiceFetcher.ServiceChangeListener {

    private static String LOG_TAG = MainActivity.class.getSimpleName();

    private TextView mManagerLatTextView;
    private TextView mManagerLonTextView;
    private TextView mManagerSpdTextView;
    private TextView mManagerGPSTextView;
    private TextView mManagerNetworkTextView;
    private TextView mManagerAccTextView;
    private TextView mManagerProviderTextView;

    private TextView mServiceLonTextView;
    private TextView mServiceLatTextView;
    private TextView mServiceActTextView;
    private TextView mServiceAccTextView;
    private TextView mServiceSpdTextView;

    private ToggleButton mManagerSwitcherButton;
    private ToggleButton mServiceSwitcherButton;

    private boolean mGPSEnabledByUser = false;
    private boolean mNetworkEnabledByUser = false;
    private boolean mLocationManagerOn = false;
    private boolean mLocationServiceOn = false;


    private LocationManagerFetcher mManagerFetcher;
    private LocationServiceFetcher mServiceFetcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateUIElement();

        mManagerFetcher = new LocationManagerFetcher(this,this);
        mServiceFetcher = new LocationServiceFetcher(this,this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(LOG_TAG,"onResume");
    }

    //***************************Here starts Google Play Services Part****************************
    private final View.OnClickListener mServiceButtonClicked = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(!mLocationServiceOn) {
                mServiceFetcher.startServices();
            }else{
                mServiceFetcher.stopServices();
            }
            mLocationServiceOn = !mLocationServiceOn;
        }
    };



        //***************************Here starts LocationManager Part****************************

    private final View.OnClickListener mManagerButtonClicked = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            int id = v.getId();
            switch (id){
                //start or stop manager
                case R.id.btn_manager_switcher:
                    if(!mGPSEnabledByUser && !mNetworkEnabledByUser && !mLocationManagerOn){
                        mManagerSwitcherButton.setChecked(false);
                        showNoConnectivityDialogBox();
                        break;
                    }
                    mLocationManagerOn = !mLocationManagerOn;
                    if(mLocationManagerOn){
                        if(mGPSEnabledByUser)
                            mManagerFetcher.startFetchLocationByGps();
                        if(mNetworkEnabledByUser)
                            mManagerFetcher.startFetchLocationByNetwork();
                    }else{
                        if(mGPSEnabledByUser)
                            mManagerFetcher.stopFetchLocationByGps();
                        if(mNetworkEnabledByUser)
                            mManagerFetcher.stopFetchLocationByNetwork();
                        onLocationChanged(null,"Null");
                    }
                    break;
                //enable gps on manager
                case R.id.btn_gps_manager:
                    mGPSEnabledByUser = !mGPSEnabledByUser;
                    if(mLocationManagerOn) {
                        if (!mGPSEnabledByUser) {
                            mManagerFetcher.stopFetchLocationByGps();
                        } else {
                            mManagerFetcher.startFetchLocationByGps();
                        }
                    }
                    break;
                //enable network on manager
                case R.id.btn_network_manager:
                    mNetworkEnabledByUser = !mNetworkEnabledByUser;
                    if(mLocationManagerOn) {
                        if (!mNetworkEnabledByUser) {
                            mManagerFetcher.stopFetchLocationByNetwork();
                        } else {
                            mManagerFetcher.startFetchLocationByNetwork();
                        }
                    }
                    break;
                default:
                    Log.e(LOG_TAG,"Button Click Err");
            }
        }
    };

    private void showNoConnectivityDialogBox(){
        new  AlertDialog.Builder(this)
                .setTitle("No connectivity" )
                .setMessage("Please enable at least one provider." )
                .setPositiveButton("Ok", null)
                .show();
    }

    @Override
    public void onGPSStausChanged(boolean GPSAvailable) {
        if(GPSAvailable){
            mManagerGPSTextView.setText("True");
        }else{
            mManagerGPSTextView.setText("False");
        }
    }

    @Override
    public void onNetworkStausChanged(boolean networkAvailable) {
        if(networkAvailable){
            mManagerNetworkTextView.setText("True");
        }else{
            mManagerNetworkTextView.setText("False");
        }
    }

    @Override
    public void onLocationChanged(Location location, String provider){
        if(location == null){
            mManagerLatTextView.setText("Null");
            mManagerLonTextView.setText("Null");
            mManagerSpdTextView.setText("Null");
            mManagerAccTextView.setText("Null");
        }else {
            mManagerLatTextView.setText(String.format("%.6f", location.getLatitude()));
            mManagerLonTextView.setText(String.format("%.6f", location.getLongitude()));
            mManagerSpdTextView.setText(String.format("%.6f", location.getSpeed()));
            mManagerAccTextView.setText(String.format("%.6f", location.getAccuracy()) + " m");
        }
        mManagerProviderTextView.setText(provider);
    }

    @Override
    public void onActivityChanged(String activity) {
        mServiceActTextView.setText(activity);
    }

    @Override
    public void onLocationChanged(Location location){
        if(location == null){
            mServiceLatTextView.setText("Null");
            mServiceLonTextView.setText("Null");
            mServiceSpdTextView.setText("Null");
            mServiceAccTextView.setText("Null");
        }else {
            mServiceLatTextView.setText(String.format("%.6f", location.getLatitude()));
            mServiceLonTextView.setText(String.format("%.6f", location.getLongitude()));
            mServiceSpdTextView.setText(String.format("%.6f", location.getSpeed()));
            mServiceAccTextView.setText(String.format("%.6f", location.getAccuracy()) + " m");
        }
    }

    private void updateUIElement(){
        mManagerLatTextView = (TextView)findViewById(R.id.edittext_lat_location_manager);
        mManagerLonTextView = (TextView)findViewById(R.id.edittext_lon_location_manager);
        mManagerSpdTextView = (TextView)findViewById(R.id.edittext_speed_location_manager);
        mManagerGPSTextView = (TextView)findViewById(R.id.show_GPSstatus_location_manager);
        mManagerAccTextView = (TextView)findViewById(R.id.show_accuracy_location_manager);
        mManagerNetworkTextView = (TextView)findViewById(R.id.show_networkstatus_location_manager);
        mManagerProviderTextView = (TextView)findViewById(R.id.show_provider_manager);

        mServiceLonTextView = (TextView) findViewById(R.id.show_lon_play_services);
        mServiceLatTextView = (TextView) findViewById(R.id.show_lat_play_services);
        mServiceSpdTextView = (TextView) findViewById(R.id.show_spd_play_services);
        mServiceAccTextView = (TextView) findViewById(R.id.show_acc_play_services);
        mServiceActTextView = (TextView) findViewById(R.id.show_activity_play_services);


        mManagerSwitcherButton = (ToggleButton)findViewById(R.id.btn_manager_switcher);
        mServiceSwitcherButton = (ToggleButton)findViewById(R.id.btn_service_switcher);

        mManagerSwitcherButton.setOnClickListener(mManagerButtonClicked);
        mServiceSwitcherButton.setOnClickListener(mServiceButtonClicked);
        findViewById(R.id.btn_gps_manager).setOnClickListener(mManagerButtonClicked);
        findViewById(R.id.btn_network_manager).setOnClickListener(mManagerButtonClicked);
    }
}
