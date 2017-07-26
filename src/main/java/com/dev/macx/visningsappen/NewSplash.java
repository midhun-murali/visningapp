package com.dev.macx.visningsappen;

/**
 * This class is used for..
 *
 * @author Midhun.
 */

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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class NewSplash extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "New Splash";
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView tx = (TextView) findViewById(R.id.splash_title);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "FrederickatheGreat-Regular.ttf");
        tx.setTypeface(custom_font);
        checkLocationPermission();
    }

    private void buildGoogleApiClient() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            Log.i(NewSplash.TAG, "Building GoogleApiClient");
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        } else {
            Toast.makeText(NewSplash.this, "Google play services unavailable", Toast.LENGTH_SHORT).show();
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
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        requestPermission(locationPermission, LOCATION_PERMISSION_REQUEST_MESSAGE, LOCATION_PERMISSION_REQUEST_CODE);
    }

    public void requestPermission(final String permission, final String permissionExplanationMessage, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                permission)) {

            DialogFactory.showDialog(this, R.string.permission_required, permissionExplanationMessage, R.string.okay, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(NewSplash.this,
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
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
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
                                    if (!isNetworkAvailable(NewSplash.this)) {
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
            if (!isNetworkAvailable(NewSplash.this)) {
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                dialog1 = ProgressDialog.show(this, "Loading..",
                        "Wait a second...", true);
                getObjectList();
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


    public void getObjectList()
    {

        //  dialog7 = ProgressDialog.show(this, "Loading..",
        //          "Wait a second...", true);
        // get saved default settings.

        SharedPreferences settings = getApplicationContext().getSharedPreferences("PREF_NAME", 0);
        String maxRadius = "100000";
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
                        Intent i = new Intent(NewSplash.this, MainActivity.class);
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
            Toast.makeText(NewSplash.this, "Object list empty", Toast.LENGTH_SHORT).show();
            dialog1.dismiss();
            Intent i = new Intent(NewSplash.this, MainActivity.class);
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
            Toast.makeText(NewSplash.this, "Object list empty", Toast.LENGTH_SHORT).show();
            dialog1.dismiss();
            Intent i = new Intent(NewSplash.this, MainActivity.class);
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
                Toast.makeText(NewSplash.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            LocationServices.GeofencingApi.addGeofences(googleApiClient, request, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                @Override public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        if (isListCompleted){
                            dialog1.dismiss();
                            Intent i = new Intent(NewSplash.this, MapActivity.class);
                            startActivity(i);
                        }


                    } else {
                        Toast.makeText(NewSplash.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        } else {
            googleApiClient.connect();
            addLocationGeofences(objectList);
            Toast.makeText(NewSplash.this, "google api client not connected", Toast.LENGTH_SHORT).show();
        }
    }


    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(NewSplash.this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(NewSplash.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public ObjectModel[] onPasingJsonObjArraydata(String str) {


        ObjectModel []result = new ObjectModel[1];

        try {

            JSONObject obj = new JSONObject(str);
            JSONArray jsonRootObject = obj.optJSONArray("objects");
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

