package com.dev.macx.visningsappen;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.dev.macx.visningsappen.Utils.SimpleGeofence;
import com.dev.macx.visningsappen.Utils.SimpleGeofenceStore;
import com.dev.macx.visningsappen.Utils.Utility;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
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

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final int REQ_PERMISSION = 999;
    private LocationRequest locationRequest;
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;


    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

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
        //createGoogleApi();
        initGoogleAPI();
        simpleGeofenceStore = new SimpleGeofenceStore(this);
    }

    private void initGoogleAPI() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();


    }

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


                //dialog1 = ProgressDialog.show(this, "Loading..",
                // "Wait a second...", true);
                dialog = ProgressDialog.show(this, "Settings",
                        "Fetching Settings", true);
                setupViews();
                //getNoneObjMsg();
                //getAppInfo();



            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
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
                Container.getInstance().selectedradius = (String) Container.getInstance().distanceList.get(position);

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
                Container.getInstance().selectedprice = (String) Container.getInstance().priceList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(Container.getInstance() != null) {
            ArrayAdapter<String> dataAdapter;
            if(Container.getInstance().distanceList != null) {
                radiusspinner = (Spinner)findViewById(R.id.setting_radiusspin);
                ArrayList<String> m_radiusArr = new ArrayList<String>(Container.getInstance().distanceList);
                for (int i = 0; i < Container.getInstance().distanceList.size(); i++) {
                    m_radiusArr.set(i, Container.getInstance().distanceList.get(i) + " m");
                }
                dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_radiusArr);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                radiusspinner.setAdapter(dataAdapter);
                radiusspinner.setSelection(Container.getInstance().distanceList.indexOf(Container.getInstance().selectedradius));
            }

            if(Container.getInstance().kvmList != null) {
                rumspinner = (Spinner) findViewById(R.id.setting_rumspin);
                ArrayList<String> m_kvmArr = new ArrayList<String>(Container.getInstance().kvmList);
                for (int i = 0; i < Container.getInstance().kvmList.size(); i++) {
                    m_kvmArr.set(i, Container.getInstance().kvmList.get(i) + " rum");
                }
                dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_kvmArr);
                rumspinner.setAdapter(dataAdapter);
                rumspinner.setSelection(Container.getInstance().kvmList.indexOf(Container.getInstance().selectedrum));
            }

            if(Container.getInstance().sqmList != null) {
                sqmspinner = (Spinner) findViewById(R.id.setting_sqmspin);
                ArrayList<String> m_sqmArr = new ArrayList<String>(Container.getInstance().sqmList);
                for (int i = 0; i < Container.getInstance().sqmList.size(); i++) {
                    m_sqmArr.set(i, Container.getInstance().sqmList.get(i) + " m2");
                }
                dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_sqmArr);
                sqmspinner.setAdapter(dataAdapter);
                sqmspinner.setSelection(Container.getInstance().sqmList.indexOf(Container.getInstance().selectedsqm));
            }


            if(Container.getInstance().priceList != null) {
                prisspinner = (Spinner) findViewById(R.id.setting_prisspin);
                ArrayList<String> m_prisArr = new ArrayList<String>(Container.getInstance().priceList);
                for (int i = 0; i < Container.getInstance().priceList.size(); i++) {
                    m_prisArr.set(i, Container.getInstance().priceList.get(i) + " kr");
                }
                dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_prisArr);
                prisspinner.setAdapter(dataAdapter);
                prisspinner.setSelection(Container.getInstance().priceList.indexOf(Container.getInstance().selectedprice));
            }

            swalert = (Switch) findViewById(R.id.swalert);
            swalert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Log.v("alert", "on");
                        Container.getInstance().alerton = "1";
                    } else {
                        Log.v("alert", "off");
                        Container.getInstance().alerton = "0";
                    }
                }
            });
            swsound = (Switch) findViewById(R.id.swsound);
            swsound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Log.v("sound", "on");
                        Container.getInstance().soundon = "1";
                    } else {
                        Log.v("sound", "off");
                        Container.getInstance().soundon = "0";
                    }
                }
            });

        }

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

        if(Container.getInstance() != null) {
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

        String minRoom = settings.getString("Minrooms","1");
        Container.getInstance().selectedrum = minRoom;

        String minSqm = settings.getString("Minsqm","50");
        Container.getInstance().selectedsqm = minSqm;

        String maxPrice = settings.getString("Maxprice","10000000");
        Container.getInstance().selectedprice = maxPrice;
        maxPrice = maxPrice.replaceAll("\\s+", "");

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
        String send_data = "Latitude=" + latitude + "&Longitude=" + longitude + "&Minrooms=" + minRoom + "&Maxprice=" + maxPrice + "&MaxRadius=" + "10000" + "&Minsqm=" + minSqm;
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
            Toast.makeText(SettingActivity.this, "Object list empty", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            Intent i = new Intent(SettingActivity.this, MapActivity.class);
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
                String geofenceAddress = Container.getInstance().objectList[i].address;
                createGeofences(lat, lng, geofenceAddress);

            }


            // close this activity
            dialog.dismiss();
            Intent i = new Intent(SettingActivity.this, MapActivity.class);
            startActivity(i);
            finish();


        }
        else {
            Toast.makeText(SettingActivity.this, "Object list empty", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(SettingActivity.this, MapActivity.class);
            startActivity(i);
            finish();
            return;
        }
    }


    public void createGeofences(Double latitude, Double longitude, String geofenceAddress) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("PREF_NAME", 0);
        String maxRadius = settings.getString("MaxRadius","200");
        Container.getInstance().selectedradius = maxRadius;
        Float radius = Float.valueOf(maxRadius);
        String geofenceId = String.valueOf(Calendar.getInstance().getTimeInMillis());
        SimpleGeofence simpleGeofence = new SimpleGeofence(
                geofenceId,                // geofenceId.
                latitude,
                longitude,
                radius,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER,
                Constants.NOT_CHECKED,
                geofenceAddress);

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
                Toast.makeText(SettingActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                @Override public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        //Toast.makeText(Splash.this, "Starting geofence transition service", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(SettingActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        } else {
            googleApiClient.connect();
            addLocationGeofences();
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
    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        //  googleApiClient.disconnect();
    }

  /*  public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MapActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }*/
    // Create GoogleApiClient instance
   /* private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
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
    } */

    // Start location Updates
   /* private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        //if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
    }
    // GoogleApiClient.ConnectionCallbacks connected
   *//* @Override
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
    } *//*
    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        //if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
       // }
        //else askPermission();
    }
*/


    // Start Geofence creation process
   /* private void startGeofence(int index) {
        Log.i(TAG, "startGeofence()");
        float lat = Float.valueOf(Container.getInstance().objectList[index].lat);
        float lng = Float.valueOf(Container.getInstance().objectList[index].lng);
       // Geofence geofence = createGeofence( new LatLng(19.9,59.34608), GEOFENCE_RADIUS );
        Geofence geofence = createGeofence( new LatLng(lat,lng), GEOFENCE_RADIUS,index);
        GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
        addGeofence( geofenceRequest );
    }
    // Create a Geofence
    private Geofence createGeofence( LatLng latLng, float radius , int index) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(String.valueOf(index))
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( GEO_DURATION )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }
    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;
        Intent intent = new Intent( this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }
    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }
    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
        } else {
            // inform about fail
        }
    }
    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;
    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
    }
    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";
    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        );
    }*/


}