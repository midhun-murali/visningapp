package com.dev.macx.visningsappen;

import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class Splash extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private static final String TAG = Splash.class.getSimpleName();
    private final int REQ_PERMISSION = 999;
    private LocationRequest locationRequest;
    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;


    private ProgressDialog dialog1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tx = (TextView)findViewById(R.id.splash_title);

        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "FrederickatheGreat-Regular.ttf");

        tx.setTypeface(custom_font);

        createGoogleApi();
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
          googleApiClient.disconnect();
    }


    // Create GoogleApiClient instance
    private void createGoogleApi() {
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

                googleApiClient.disconnect();

                dialog1 = ProgressDialog.show(this, "Loading..",
                        "Wait a second...", true);
                getAppInfo();



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
                    return;
                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
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

                    return;
                }else {
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
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
                    return;

                }else {
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
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
                    return;
                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
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
                    return;
                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
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
                    return;
                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
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
        String maxRadius = settings.getString("MaxRadius", "250");
        Container.getInstance().selectedradius = maxRadius;

        String minRoom = settings.getString("Minrooms","1");
        Container.getInstance().selectedrum = minRoom;

        String minSqm = settings.getString("Minsqm","25");
        Container.getInstance().selectedsqm = minSqm;

        String maxPrice = settings.getString("Maxprice","6000000");
        Container.getInstance().selectedprice = maxPrice;

        String alerton = settings.getString("alerton","1");
        Container.getInstance().alerton = alerton;

        String soundon = settings.getString("soundon","1");
        Container.getInstance().soundon = soundon;

        if(Container.getInstance().currentlat == null){
            String latitude = "59.345";
            String longitude ="18.055";
        }else{
            String latitude = Container.getInstance().currentlat; // "59.345";
            String longitude =  Container.getInstance().currentlng; //"18.055";
        }

        String latitude = Container.getInstance().currentlat; // "59.345";
        String longitude =  Container.getInstance().currentlng; //"18.055";



        final PostData post_ObjectList = new PostData(this);

        String send_data = "MaxRadius=" + maxRadius + "&" + "Minrooms=" + minRoom + "&" + "Minsqm=" + minSqm + "&" +"Maxprice=" +maxPrice +"&"
                + "Latitude=" + latitude + "&" +"Longitude=" + longitude;
        String info_url = "http://visningsappen.se/communicationModel/getObject.php?" + send_data;
        post_ObjectList.execute(info_url,"getObjectList",send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String tmp = post_ObjectList.getClient();

                dialog1.dismiss();
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
                    }else {
                        Container.getInstance().objectList = onPasingJsonObjArraydata(tmp);

                        ObjectModel[] database = Container.getInstance().objectList;
                    }

                    Intent i = new Intent(Splash.this, MainActivity.class);
                    startActivity(i);

                    // close this activity
                    finish();

                    return;
                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

            }
        }, 2000);

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

    }


}
