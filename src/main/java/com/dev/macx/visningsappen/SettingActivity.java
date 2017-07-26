package com.dev.macx.visningsappen;

import android.*;
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
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.macx.visningsappen.Utils.Constants;
import com.dev.macx.visningsappen.Utils.DialogFactory;
import com.dev.macx.visningsappen.Utils.SimpleGeofence;
import com.dev.macx.visningsappen.Utils.SimpleGeofenceStore;
import com.dev.macx.visningsappen.Utils.Utility;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class SettingActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final String TAG = "Settings";
    private static final String LOCATION_PERMISSION_REQUEST_MESSAGE = "Storage permission is required";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private LocationRequest locationRequest;
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 900;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private ObjectModel[] objectList;
    private LocationManager locationManager;


    private ProgressDialog dialog1;
    private boolean isListCompleted = false;

    public ImageButton infobtn,homebtn,manbtn,mailbtn;
    private Spinner radiusspinner,rumspinner,sqmspinner,prisspinner;
    private Switch swalert, swsound;
    private Button cancelbtn,okbtn;
    SimpleGeofenceStore simpleGeofenceStore;

    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        TextView tx = (TextView) findViewById(R.id.splash_title);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "FrederickatheGreat-Regular.ttf");
        tx.setTypeface(custom_font);
        checkLocationPermission();
    }

    private void buildGoogleApiClient() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            Log.i(SettingActivity.TAG, "Building GoogleApiClient");
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        } else {
            Toast.makeText(SettingActivity.this, "Google play services unavailable", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }


    private void askLocationPermission() {
        String locationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
        requestPermission(locationPermission, LOCATION_PERMISSION_REQUEST_MESSAGE, LOCATION_PERMISSION_REQUEST_CODE);
    }

    public void requestPermission(final String permission, final String permissionExplanationMessage, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                permission)) {

            DialogFactory.showDialog(this, R.string.permission_required, permissionExplanationMessage, R.string.okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(SettingActivity.this,
                            new String[]{permission}, requestCode);
                }
            }, R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            }, false);

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission}, requestCode);
        }
    }

    private void checkLocationPermission() {
        String locationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
        if (!(havePermission(locationPermission))) {
            askLocationPermission();
        } else {
            buildGoogleApiClient();
        }
    }

    public boolean havePermission(String permission) {
        return Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    buildGoogleApiClient();

                } else {
                    // Permission denied
                    System.exit(0);
                }
                break;
            }
        }
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
                                    if (!isNetworkAvailable(SettingActivity.this)) {
                                        showDataAlert();
                                    } else {

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
            if (!isNetworkAvailable(SettingActivity.this)) {
                showDataAlert();
            } else {
                startLocationUpdates();
                getLastKnownLocation();
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

    @Override
    protected void onResume() {
        super.onResume();
        if(!googleApiClient.isConnected()){
            buildGoogleApiClient();
        }
         else {
            showGpsAlert();
        }


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


    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");
        lastLocation = location;
        writeActualLocation(location);
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        showGpsAlert();
        //recoverGeofenceMarker();
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

    private void writeActualLocation(Location location) {
        //textLat.setText( "Lat: " + location.getLatitude() );
        //textLong.setText( "Long: " + location.getLongitude() );
        Container.getInstance().currentlat = String.valueOf(location.getLatitude());
        Container.getInstance().currentlng = String.valueOf(location.getLongitude());


        //markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            return;
        }
        if ( lastLocation != null ) {
            Log.i(TAG, "LasKnown location. " +
                    "Long: " + lastLocation.getLongitude() +
                    " | Lat: " + lastLocation.getLatitude());
            writeLastLocation();
            getNoneObjMsg();
        } else {
            Log.w(TAG, "No location retrieved yet");
            startLocationUpdates();
        }
    }


    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
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

                    setupViews();
                    //Toast.makeText(getApplicationContext(),"kvm list success!",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getKvmList();
                    //finish();
                }
            }
        }, 500);

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

    public void setupViews(){


        TextView tx = (TextView)findViewById(R.id.setting_titletext);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "FrederickatheGreat-Regular.ttf");
        tx.setTypeface(custom_font);


        MobileAds.initialize(getApplicationContext(), " ca-app-pub-6954747802734407~5311984177");
        AdView mAdView = (AdView) findViewById(R.id.setting_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mailbtn = (ImageButton)findViewById(R.id.setting_mail);
        mailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingActivity.this, ContactActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        homebtn = (ImageButton)findViewById(R.id.setting_home);
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });
        manbtn = (ImageButton)findViewById(R.id.setting_man);
        manbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingActivity.this, MapActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        infobtn = (ImageButton)findViewById(R.id.setting_info);
        infobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingActivity.this, InfoActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });


        radiusspinner = (Spinner)findViewById(R.id.setting_radiusspin);
            radiusspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.v("selected item:", (String)Container.getInstance().distanceList.get(position));
                    Container.getInstance().selectedradius = (String)((String) Container.getInstance().distanceList.get(position));

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        sqmspinner = (Spinner)findViewById(R.id.setting_sqmspin);
        sqmspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("selected item:", (String)Container.getInstance().sqmList.get(position));
                Container.getInstance().selectedsqm = (String)Container.getInstance().sqmList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rumspinner = (Spinner)findViewById(R.id.setting_rumspin);
        rumspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("selected item:", (String)Container.getInstance().kvmList.get(position));
                Container.getInstance().selectedrum = (String)Container.getInstance().kvmList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        prisspinner = (Spinner)findViewById(R.id.setting_prisspin);
        prisspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("selected item:", (String)Container.getInstance().priceList.get(position));
                Container.getInstance().selectedprice = (String)((String) Container.getInstance().priceList.get(position)).replaceAll("\\s+", "");;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayList<String> m_radiusArr =  new ArrayList<String>(Container.getInstance().distanceList);
            for (int i =0;i< Container.getInstance().distanceList.size();i++){
                m_radiusArr.set(i,Container.getInstance().distanceList.get(i) + " m");
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_radiusArr);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            radiusspinner.setAdapter(dataAdapter);

            rumspinner = (Spinner)findViewById(R.id.setting_rumspin);
            ArrayList<String> m_kvmArr =  new ArrayList<String>(Container.getInstance().kvmList);
            for (int i =0;i< Container.getInstance().kvmList.size();i++){
                m_kvmArr.set(i,Container.getInstance().kvmList.get(i) + " rum");
            }
            dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_kvmArr);
            rumspinner.setAdapter(dataAdapter);


             sqmspinner = (Spinner)findViewById(R.id.setting_sqmspin);
            ArrayList<String> m_sqmArr =  new ArrayList<String>(Container.getInstance().sqmList);
            for (int i =0;i< Container.getInstance().sqmList.size();i++){
                m_sqmArr.set(i,Container.getInstance().sqmList.get(i) + " m2");
            }
            dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_sqmArr);
            sqmspinner.setAdapter(dataAdapter);


            prisspinner = (Spinner)findViewById(R.id.setting_prisspin);
            ArrayList<String> m_prisArr =  new ArrayList<String>(Container.getInstance().priceList);
            for (int i =0;i< Container.getInstance().priceList.size();i++){
                m_prisArr.set(i,Container.getInstance().priceList.get(i) + " kr");
            }
            dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_prisArr);
             prisspinner.setAdapter(dataAdapter);

        swalert = (Switch)findViewById(R.id.swalert);
        swalert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.v("alert","on");
                    Container.getInstance().alerton = "1";
                }else{
                    Log.v("alert","off");
                    Container.getInstance().alerton = "0";
                }
            }
        });
        swsound = (Switch)findViewById(R.id.swsound);
        swsound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.v("sound","on");
                    Container.getInstance().soundon = "1";
                }else{
                    Log.v("sound","off");
                    Container.getInstance().soundon = "0";
                }
            }
        });

        okbtn = (Button)findViewById(R.id.setting_okbtn);
        okbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // save current setting

                SharedPreferences shared = getSharedPreferences("PREF_NAME", 0);
                SharedPreferences.Editor editor = shared.edit();
                editor.putString("MaxRadius", Container.getInstance().selectedradius);
                editor.putString("Minrooms", Container.getInstance().selectedrum);
                editor.putString("Minsqm", Container.getInstance().selectedsqm);
                editor.putString("Maxprice",Container.getInstance().selectedprice);
                editor.putString("alerton", Container.getInstance().alerton);
                editor.putString("soundon", Container.getInstance().soundon);

                String radius = Container.getInstance().selectedradius;



                editor.commit();// commit is important here.


                // setup new tracking object  target

                getObjectList();



            }
        });
        cancelbtn = (Button)findViewById(R.id.setting_cancelbtn);

        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });


        // at first loading, let's load previous setting values

        radiusspinner.setSelection(Container.getInstance().distanceList.indexOf(Container.getInstance().selectedradius));
        rumspinner.setSelection(Container.getInstance().kvmList.indexOf(Container.getInstance().selectedrum));
        sqmspinner.setSelection(Container.getInstance().sqmList.indexOf(Container.getInstance().selectedsqm));
        prisspinner.setSelection(Container.getInstance().priceList.indexOf(Container.getInstance().selectedprice));

        if(Container.getInstance().alerton != null && Container.getInstance().alerton.equals("1")){
            swalert.setChecked(true);
        }
        if(Container.getInstance().soundon != null && Container.getInstance().soundon.equals("1")){
            swsound.setChecked(true);
        }

        dialog.dismiss();

    }

    public void getObjectList()
    {

        // get saved default settings.

        /*SharedPreferences settings = getApplicationContext().getSharedPreferences("PREF_NAME", 0);
        String maxRadius = settings.getString("MaxRadius", "1250");
        String minRoom = settings.getString("Minrooms","1");
        String minSqm = settings.getString("Minsqm","25");
        String maxPrice = settings.getString("Maxprice","7000000");

        String latitude =  String.valueOf(lastLocation.getLatitude());
        String longitude = String.valueOf(lastLocation.getLongitude());*/
        dialog = ProgressDialog.show(this, "Searching..",
                "Wait a second...", true);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("PREF_NAME", 0);
        String maxRadius = "10000";
        Container.getInstance().selectedradius = maxRadius;

        String minRoom = settings.getString("Minrooms","1");
        Container.getInstance().selectedrum = minRoom;

        String minSqm = settings.getString("Minsqm","50");
        Container.getInstance().selectedsqm = minSqm;

        String maxPrice = settings.getString("Maxprice","10000000");
        Container.getInstance().selectedprice = maxPrice;

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



        final PostData post_ObjectList = new PostData(this);

        /*String send_data = "MaxRadius=" + maxRadius + "&" + "Minrooms=" + minRoom + "&" + "Minsqm=" + minSqm + "&" +"Maxprice=" +maxPrice +"&"
                + "Latitude=" + latitude + "&" +"Longitude=" + longitude;
        String info_url = "http://visningsappen.se/communicationModel/getObject.php?" + send_data;*/
        String send_data = "Latitude=" + latitude + "&Longitude=" + longitude + "&Minrooms=" + minRoom + "&Maxprice=" + maxPrice + "&MaxRadius=" + maxRadius + "&Minsqm=" + minSqm;
        String info_url = "http://visningsappen.se/communicationModel/getObject.php?" + send_data;


        post_ObjectList.execute(info_url,"getObjectList",send_data);


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                String tmp = post_ObjectList.getClient();
                if(tmp.equals("")) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }

                    tmp = post_ObjectList.getClient();

                }



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
                        dialog.dismiss();
                        Intent i = new Intent(SettingActivity.this, MapActivity.class);
                        startActivity(i);
                        finish();
                    }else {
                        //Toast.makeText(getApplicationContext(),"Objects available",Toast.LENGTH_SHORT).show();
                        objectList = onPasingJsonObjArraydata(tmp);
                        Container.getInstance().objectList = objectList;
                        addLocationGeofences(objectList);

                    }

                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getObjectList();
                    //finish();
                }

            }
        }, 2000);

    }

    public void addLocationGeofences(ObjectModel[] objectListGeo) {

        if (objectListGeo == null) {
            Toast.makeText(SettingActivity.this, "Object list empty", Toast.LENGTH_SHORT).show();
            dialog1.dismiss();
            Intent i = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            return;
        }
        if (objectListGeo.length != 0) {

            int length = objectListGeo.length;
            // Toast.makeText(Splash.this, "Object list not empty", Toast.LENGTH_SHORT).show();

            for (int i = 0; i < objectListGeo.length; i++) {

                if (objectListGeo[i] == null) {
                    // Toast.makeText(Splash.this, "Object list empty 2", Toast.LENGTH_SHORT).show();
                    break;
                }
                if(i == (objectListGeo.length - 1)){
                    isListCompleted = true;
                }
                Double lat = Double.parseDouble(objectListGeo[i].lat);
                Double lng = Double.parseDouble(objectListGeo[i].lng);
                SharedPreferences settings = getApplicationContext().getSharedPreferences("PREF_NAME", 0);
                String sRadius = settings.getString("MaxRadius", "200");
                Float radius = Float.valueOf(sRadius);
                //createGeofences(lat, lng, radius);
                Geofence geofence = createGeofence( lat, lng, radius);
                GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
                addGeofence( geofenceRequest );

            }


        } else {
            Toast.makeText(SettingActivity.this, "Object list empty", Toast.LENGTH_SHORT).show();
            dialog1.dismiss();
            Intent i = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            return;
        }
    }

    private Geofence createGeofence( Double latitude, Double longitude, float radius ) {
        Log.d(TAG, "createGeofence");
        String geofenceId = String.valueOf(Calendar.getInstance().getTimeInMillis());
        return new Geofence.Builder()
                .setRequestId(geofenceId)
                .setCircularRegion( latitude, longitude, radius)
                .setExpirationDuration( Geofence.NEVER_EXPIRE )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }
    private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }

    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        PendingIntent pendingIntent = getGeofenceTransitionPendingIntent();
        if (googleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                Toast.makeText(SettingActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            LocationServices.GeofencingApi.addGeofences(googleApiClient, request, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                @Override public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        if (isListCompleted){
                            dialog1.dismiss();
                            Intent i = new Intent(SettingActivity.this, MapActivity.class);
                            startActivity(i);
                        }


                    } else {
                        Toast.makeText(SettingActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        } else {
            googleApiClient.connect();
            addLocationGeofences(objectList);
            Toast.makeText(SettingActivity.this, "google api client not connected", Toast.LENGTH_SHORT).show();
        }
    }


    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(SettingActivity.this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(SettingActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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



    // Clear Geofence
    /*private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        );
    }*/


}
