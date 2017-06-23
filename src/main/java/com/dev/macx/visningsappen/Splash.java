package com.dev.macx.visningsappen;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.macx.visningsappen.Utils.Constants;
import com.dev.macx.visningsappen.Utils.DialogFactory;
import com.dev.macx.visningsappen.Utils.SimpleGeofence;
import com.dev.macx.visningsappen.Utils.SimpleGeofenceStore;
import com.dev.macx.visningsappen.Utils.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class Splash extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;
    public GoogleApiClient googleApiClient;
    private Location lastLocation;
    private static final String TAG = Splash.class.getSimpleName();
    private final int REQ_PERMISSION = 999;
    private LocationRequest locationRequest;
    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;
    SimpleGeofenceStore simpleGeofenceStore;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1340;


    private ProgressDialog dialog1;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tx = (TextView)findViewById(R.id.splash_title);

        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "FrederickatheGreat-Regular.ttf");

        tx.setTypeface(custom_font);
        simpleGeofenceStore = new SimpleGeofenceStore(this);
        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services unavailable.");
            Toast.makeText(Splash.this, "Google play services unavailable", Toast.LENGTH_SHORT).show();
            /*finish();
            return;*/
        }
        showGpsAlert();
    }

    private void showGpsAlert() {

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Please enable your GPS to continue")
                        .setCancelable(false)
                        .setPositiveButton("Enable GPS",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // set intent to open settings
                                        Intent callGPSSettingIntent = new Intent(
                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        if (!isNetworkAvailable(Splash.this)) {
                                            showDataAlert();
                                        } else {
                                            initGoogleAPI();
                                        }
                                        startActivity(callGPSSettingIntent);
                                    }
                                });
                alertDialogBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                System.exit(0);
                            }
                        });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            } else {
                if (!isNetworkAvailable(Splash.this)) {
                    showDataAlert();
                } else {
                    initGoogleAPI();
                }
            }


    }

    private void showDataAlert() {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Please enable your Mobile data to continue")
                    .setCancelable(false)
                    .setPositiveButton("Enable Mobile data",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // set intent to open settings
                                    Intent callDataSettingIntent = new Intent(
                                            Settings.ACTION_DATA_ROAMING_SETTINGS);
                                    startActivity(callDataSettingIntent);
                                    initGoogleAPI();

                                }
                            });
            alertDialogBuilder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.exit(0);
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();


    }

    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            boolean result = true;
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
                result = false;
            }
            return result;
        } else {
            return false;
        }
    }

    public GoogleApiClient getGoogleApiClient () {
        return googleApiClient;
    }


    /**
     * Checks if Google Play services is available.
     *
     * @return true if it is.
     */
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Google Play services is available.");
            }
            return true;
        } else {
            Log.e(TAG, "Google Play services is unavailable.");
            return false;
        }
    }



    // Create GoogleApiClient instance
    private void initGoogleAPI() {

        if (isNetworkAvailable(Splash.this)) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            googleApiClient.connect();

        } else {
            initGoogleAPI();
        }




    }

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }




    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case REQ_PERMISSION: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        // TODO close app and warn user
    }

    // Start location Updates
    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;

    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();

    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());

                // startLocationUpdates();

                // get and save location into Container.
                Container.getInstance().currentlat = String.valueOf(lastLocation.getLatitude());
                Container.getInstance().currentlng = String.valueOf(lastLocation.getLongitude());

                // This method will be executed once the timer is over
                // Start your app main activity


                dialog1 = ProgressDialog.show(this, "Loading..",
                        "Wait a second...", true);
                getObjectList();
                //getAppInfo();



            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
    }


    public void getAppInfo() {


        // download app info
        final PostData post_info = new PostData(this);
        JSONObject json_past_temp = new JSONObject();
        try {
            json_past_temp.put("name", "username");
            json_past_temp.put("pwd", "pw");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String send_data = json_past_temp.toString();
        String info_url = "http://visningsappen.se/communicationModel/getInfo.php?";
        post_info.execute(info_url, "getAppInfo", send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //        dialog1.dismiss();
                String tmp = post_info.getClient();
                if (post_info.getReturnCode() == 200) {

                    if(tmp.equals("{}")){
                        getAppInfo();

                    }
                    Log.v("info_string",tmp);
                    String appInfo = onPasingJsondata(tmp,"infotext");
                    Container.getInstance().appinfo = appInfo;
                    Log.v("saved appInfo:",tmp);


                    getNoneObjMsg();
                    //Toast.makeText(getApplicationContext(),"appinfo error success!",Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getAppInfo();
                    //finish();

                }
            }
        }, 2000);

    }

    public void getNoneObjMsg()
    {
        // dialog2 = ProgressDialog.show(this, "Loading..",
        //         "Wait a second...", true);
        // download app info
        final PostData post_NoneObj = new PostData(this);
        JSONObject json_past_temp=new JSONObject();
        try{
            json_past_temp.put("name","username");
            json_past_temp.put("pwd","pw");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        String send_data=json_past_temp.toString();
        String info_url = "http://visningsappen.se/communicationModel/getNoObjMsg.php?";
        post_NoneObj.execute(info_url,"getNoneObjMsg",send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //        dialog2.dismiss();
                String tmp = post_NoneObj.getClient();
                if (post_NoneObj.getReturnCode() == 200) {

                    if (tmp.equals("{}")){
                        getNoneObjMsg();

                    }

                    Log.v("nonobj_msg_string:",tmp);
                    String msg = onPasingJsondata(tmp,"nothing");
                    Container.getInstance().nonObjmsg = msg;

                    Log.v("saved nonobjmsg:",tmp);
                    getDistanceList();
                    //Toast.makeText(getApplicationContext(),"Noneobject success!",Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getNoneObjMsg();
                    //finish();
                }
            }
        }, 3000);


    }

    public void getDistanceList()
    {
        //   dialog3 = ProgressDialog.show(this, "Loading..",
        //           "Wait a second...", true);
        // download app info
        final PostData post_disList = new PostData(this);
        JSONObject json_past_temp=new JSONObject();
        try{
            json_past_temp.put("name","username");
            json_past_temp.put("pwd","pw");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        String send_data=json_past_temp.toString();
        String info_url = "http://visningsappen.se/communicationModel/getDistance.php?";
        post_disList.execute(info_url,"getDistanceList",send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //       dialog3.dismiss();
                String tmp = post_disList.getClient();
                if (post_disList.getReturnCode() == 200) {

                    if (tmp.equals("[]")){
                        getDistanceList();

                    }

                    Log.v("getdistancelist_string:",tmp);
                    ArrayList distanceList = onPasingJsonArraydata(tmp);
                    Container.getInstance().distanceList = distanceList;

                    Log.v("saved nonobjmsg:",tmp);
                    getPriceList();
                    //Toast.makeText(getApplicationContext(),"Distance list success!",Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getDistanceList();
                    //finish();
                }
            }
        }, 500);

    }
    public void getPriceList()
    {
        //  dialog4 = ProgressDialog.show(this, "Loading..",
        //         "Wait a second...", true);
        // download app info
        final PostData post_priceList = new PostData(this);
        JSONObject json_past_temp=new JSONObject();
        try{
            json_past_temp.put("name","username");
            json_past_temp.put("pwd","pw");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        String send_data=json_past_temp.toString();
        String info_url = "http://visningsappen.se/communicationModel/getPrice.php?";
        post_priceList.execute(info_url,"getPriceList",send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //         dialog4.dismiss();
                String tmp = post_priceList.getClient();
                if (post_priceList.getReturnCode() == 200) {

                    if (tmp.equals("[]")){
                        getPriceList();

                    }

                    Log.v("getpricelist_string:",tmp);
                    ArrayList priceList = onPasingJsonArraydata(tmp);
                    Container.getInstance().priceList = priceList;
                    Log.v("saved pricelist:",tmp);


                    getRumList();
                    //Toast.makeText(getApplicationContext(),"price list success!",Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getPriceList();
                    //finish();

                }
            }
        }, 500);

    }

    public void getRumList()
    {
        // dialog5 = ProgressDialog.show(this, "Loading..",
        //         "Wait a second...", true);
        // download app info
        final PostData post_rumList = new PostData(this);
        JSONObject json_past_temp=new JSONObject();
        try{
            json_past_temp.put("name","username");
            json_past_temp.put("pwd","pw");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        String send_data=json_past_temp.toString();
        String info_url = "http://visningsappen.se/communicationModel/getRum.php?";
        post_rumList.execute(info_url,"getRumList",send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //       dialog5.dismiss();
                String tmp = post_rumList.getClient();
                if (post_rumList.getReturnCode() == 200) {

                    if (tmp.equals("[]")){
                        getRumList();

                    }


                    Log.v("getrumlist_string:",tmp);
                    ArrayList kvmList = onPasingJsonArraydata(tmp);
                    Container.getInstance().kvmList = kvmList;

                    Log.v("saved rumList:",tmp);

                    getKvmList();
                    //Toast.makeText(getApplicationContext(),"Rum list success!",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getRumList();
                    //finish();
                }
            }
        }, 500);

    }
    public void getKvmList()
    {
        //  dialog6 = ProgressDialog.show(this, "Loading..",
        //          "Wait a second...", true);
        // download app info
        final PostData post_KvmList = new PostData(this);
        JSONObject json_past_temp=new JSONObject();
        try{
            json_past_temp.put("name","username");
            json_past_temp.put("pwd","pw");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        String send_data=json_past_temp.toString();
        String info_url = "http://visningsappen.se/communicationModel/getKvm.php?";
        post_KvmList.execute(info_url,"getKvmList",send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //  dialog6.dismiss();
                String tmp = post_KvmList.getClient();
                if (post_KvmList.getReturnCode() == 200) {
                    if (tmp.equals("[]")){
                        getKvmList();

                    }

                    Log.v("getsqmlist_string:",tmp);
                    ArrayList sqmList = onPasingJsonArraydata(tmp);
                    Container.getInstance().sqmList = sqmList;

                    Log.v("saved sqmList:",Container.getInstance().kvmList.toString());

                    getObjectList();
                    //Toast.makeText(getApplicationContext(),"kvm list success!",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getKvmList();
                    //finish();
                }
            }
        }, 500);

    }

    public void getObjectList()
    {

        //  dialog7 = ProgressDialog.show(this, "Loading..",
        //          "Wait a second...", true);
        // get saved default settings.

        SharedPreferences settings = getApplicationContext().getSharedPreferences("PREF_NAME", 0);
        /*String maxRadius = settings.getString("MaxRadius", "10000");
        Container.getInstance().selectedradius = maxRadius;

        String minRoom = settings.getString("Minrooms","1");
        Container.getInstance().selectedrum = minRoom;

        String minSqm = settings.getString("Minsqm","50");
        Container.getInstance().selectedsqm = minSqm;

        String maxPrice = settings.getString("Maxprice","10000000");
        Container.getInstance().selectedprice = maxPrice;

        String alerton = settings.getString("alerton","1");
        Container.getInstance().alerton = alerton;

        String soundon = settings.getString("soundon","1");
        Container.getInstance().soundon = soundon;*/

        String maxRadius = "10000";
        Container.getInstance().selectedradius = maxRadius;

        String minRoom = "1";
        Container.getInstance().selectedrum = minRoom;

        String minSqm = "50";
        Container.getInstance().selectedsqm = minSqm;

        String maxPrice = "10000000";
        Container.getInstance().selectedprice = maxPrice;

        String alerton = settings.getString("alerton","1");
        Container.getInstance().alerton = alerton;

        String soundon = settings.getString("soundon","1");
        Container.getInstance().soundon = soundon;

        String latitude, longitude;
        //if(Container.getInstance().currentlat == null){
        if(Container.getInstance().currentlat != null){
            latitude = Container.getInstance().currentlat;
        } else {
            latitude = "59.328720";
        }
        if(Container.getInstance().currentlng != null){
            longitude = Container.getInstance().currentlng;
        } else {
            longitude ="18.029720";
        }
        /*}else{
            String latitude = Container.getInstance().currentlat; // "59.345";
            String longitude =  Container.getInstance().currentlng; //"18.055";
        }

        String latitude = Container.getInstance().currentlat; // "59.345";
        String longitude =  Container.getInstance().currentlng; //"18.055";*/



        final PostData post_ObjectList = new PostData(this);

        /*String send_data = "MaxRadius=" + maxRadius + "&" + "Minrooms=" + minRoom + "&" + "Minsqm=" + minSqm + "&" +"Maxprice=" +maxPrice +"&"
                + "Latitude=" + latitude + "&" +"Longitude=" + longitude;*/
        String send_data = "Latitude=" + latitude + "&Longitude=" + longitude + "&Minrooms=" + minRoom + "&Maxprice=" + maxPrice + "&MaxRadius=" + maxRadius + "&Minsqm=" + minSqm;
        String info_url = "http://visningsappen.se/communicationModel/getObject.php?" + send_data;
        //String info_url = "http://visningsappen.se/communicationModel/getObject.php?Latitude=59.328720&Longitude=18.029720&Minrooms=1&Maxprice=10000000&MaxRadius=250&Minsqm=50";

        post_ObjectList.execute(info_url,"getObjectList",send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String tmp = post_ObjectList.getClient();

                if (post_ObjectList.getReturnCode() == 200) {

                    if(tmp.equals("")){
                        try {//sleep 2 seconds
                            Thread.sleep(2000);
                            System.out.println("Testing..." + new Date());
                            tmp = post_ObjectList.getClient();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (tmp.equals("[]")){
                        //   getObjectList();
                        //   return;
                        Toast.makeText(getApplicationContext(),"No objects available",Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Splash.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }else {
                        //Toast.makeText(getApplicationContext(),"Objects available",Toast.LENGTH_SHORT).show();
                        Container.getInstance().objectList = onPasingJsonObjArraydata(tmp);

                        ObjectModel[] database = Container.getInstance().objectList;
                        addLocationGeofences();

                    }

                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getObjectList();
                    //finish();
                }

            }
        }, 2000);

    }

    public void addLocationGeofences() {

        if (Container.getInstance().objectList == null) {
            Toast.makeText(Splash.this, "Object list empty", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(Splash.this, MainActivity.class);
            startActivity(i);
            finish();
            return;
        }
        if (Container.getInstance().objectList.length != 0) {

            int length = Container.getInstance().objectList.length;
           // Toast.makeText(Splash.this, "Object list not empty", Toast.LENGTH_SHORT).show();

            for (int i = 0; i < Container.getInstance().objectList.length; i++) {

                if (Container.getInstance().objectList[i] == null) {
                   // Toast.makeText(Splash.this, "Object list empty 2", Toast.LENGTH_SHORT).show();
                    break;
                }
                Double lat = Double.parseDouble(Container.getInstance().objectList[i].lat);
                Double lng = Double.parseDouble(Container.getInstance().objectList[i].lng);
                createGeofences(lat, lng);

            }


            // close this activity
            dialog1.dismiss();
            Intent i = new Intent(Splash.this, MainActivity.class);
            startActivity(i);
            finish();


        }
    }


    public void createGeofences(Double latitude, Double longitude) {
        Float radius = 200.00f;
        String geofenceId = String.valueOf(Calendar.getInstance().getTimeInMillis());
        SimpleGeofence simpleGeofence = new SimpleGeofence(
                geofenceId,                // geofenceId.
                latitude,
                longitude,
                radius,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER,
                Constants.NOT_CHECKED);

        // Store these flat versions in SharedPreferences and add them to the geofence list.
        simpleGeofenceStore.setGeofence(geofenceId, simpleGeofence);
        // Store id list
        Utility.setGeoFenceIdList(geofenceId, this);
        //Toast.makeText(Splash.this, "creating geofence", Toast.LENGTH_SHORT).show();

        //setupGeofence(simpleGeofence);

        addGeofence(simpleGeofence.toGeofence());
    }

    private void addGeofence(Geofence geofence) {
        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence).build();

        PendingIntent pendingIntent = getGeofenceTransitionPendingIntent();
        //Toast.makeText(Splash.this, "adding geofence", Toast.LENGTH_SHORT).show();

        if (googleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(Splash.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                @Override public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        //Toast.makeText(Splash.this, "Starting geofence transition service", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(Splash.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        } else {
            googleApiClient.connect();
            addLocationGeofences();
            Toast.makeText(Splash.this, "google api client not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(Splash.this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(Splash.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public String onPasingJsondata(String str,String key) {


        String result = "";
        try {

            JSONObject jsonRootObject = new JSONObject(str);
            result = jsonRootObject.getString(key).toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;

    }

    public ArrayList onPasingJsonArraydata(String str) {


        ArrayList result = new ArrayList();
        try {

            JSONArray jsonRootObject = new JSONArray(str);
            for (int i= 0;i<jsonRootObject.length();i++){
                result.add(jsonRootObject.get(i).toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;

    }

    public ObjectModel[] onPasingJsonObjArraydata(String str) {


        ObjectModel []result = new ObjectModel[1];

        try {

            JSONObject obj = new JSONObject(str);
            JSONArray jsonRootObject = obj.optJSONArray("objects");
            //JSONArray jsonRootObject = new JSONArray(str);

            //JSONArray jsonArray = anotherObj.getJSONArray("arrayKey");
            //JSONArray jsonRootObject = anotherObj.getJSONArray("arrayKey");;
            //JSONArray jsonRootObject = new JSONArray(str);
            result = Arrays.copyOf(result ,jsonRootObject.length());

            for (int i= 0;i<jsonRootObject.length();i++){
                JSONObject temp = (JSONObject) jsonRootObject.get(i);
                ObjectModel tempModel = new ObjectModel();


                tempModel.maklare = temp.getString("object_maklare");
                tempModel.logo = temp.getString("object_logo");
                tempModel.address = temp.getString("object_address");
                tempModel.price = temp.getString("object_price");
                tempModel.rooms = temp.getString("object_rooms");
                tempModel.sqm = temp.getString("object_sqm");
                tempModel.realstart = temp.getString("object_realstart");
                tempModel.start = temp.getString("object_start");
                tempModel.end = temp.getString("object_end");
                tempModel.photo = temp.getString("object_photo");
                tempModel.url = temp.getString("object_url");
                tempModel.lat = temp.getString("objekt_lat");
                tempModel.lng = temp.getString("objekt_lng");
                tempModel.clicked = temp.getString("object_clicked");
                tempModel.descr = temp.getString("object_descr");

                result[i] = tempModel;

                Log.v("fetching a element!","success");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;

    }


   /* public ObjectModel[] onPasingJsonObjArraydata(String str) {


        ObjectModel []result = new ObjectModel[1];

        try {

            JSONArray jsonRootObject = new JSONArray(str);
            result = Arrays.copyOf(result ,jsonRootObject.length());

            for (int i= 0;i<jsonRootObject.length();i++){
                JSONObject temp = (JSONObject) jsonRootObject.get(i);
                ObjectModel tempModel = new ObjectModel();


                tempModel.maklare = temp.getString("object_maklare");
                tempModel.logo = temp.getString("object_logo");
                tempModel.address = temp.getString("object_address");
                tempModel.price = temp.getString("object_price");
                tempModel.rooms = temp.getString("object_rooms");
                tempModel.sqm = temp.getString("object_sqm");
                tempModel.realstart = temp.getString("object_realstart");
                tempModel.start = temp.getString("object_start");
                tempModel.end = temp.getString("object_end");
                tempModel.photo = temp.getString("object_photo");
                tempModel.url = temp.getString("object_url");
                tempModel.lat = temp.getString("objekt_lat");
                tempModel.lng = temp.getString("objekt_lng");
                tempModel.clicked = temp.getString("object_clicked");
                tempModel.descr = temp.getString("object_descr");

                result[i] = tempModel;

                Log.v("fetching a element!","success");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;

    }*/


}
