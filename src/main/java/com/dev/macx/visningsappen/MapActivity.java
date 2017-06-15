package com.dev.macx.visningsappen;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.renderscript.Double2;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.location.LocationListener;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.macx.visningsappen.Utils.PermissionUtils;
import com.dev.macx.visningsappen.Utils.SimpleGeofence;
import com.dev.macx.visningsappen.Utils.SimpleGeofenceStore;
import com.dev.macx.visningsappen.Utils.Utility;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener,
        OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    public ImageButton infobtn, homebtn, settingbtn, mailbtn, zoominbtn, zoomoutbtn;
    private GoogleMap mMap;
    private View infowindow;

    private TextView addresstxt, descriptiontxt, pricetxt, rumtxt, kvmtxt, timetxt, moretxt;
    private ImageView logoimg, photoimg;
    private  float currentzoom;
    private BroadcastReceiver mReceiver;

    LatLng latLng;
    Marker currLocationMarker;
    private SimpleGeofenceStore mGeofenceStorage;
    List<Geofence> mGeofenceList;
    List<SimpleGeofence> mSimpleGeofenceList;
    // Persistent storage for geofences.

    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private static final String TAG = Splash.class.getSimpleName();
    private final int REQ_PERMISSION = 999;
    private LocationRequest locationRequest;
    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        createGoogleApi();
        mGeofenceStorage = new SimpleGeofenceStore(this);
        mGeofenceList = new ArrayList<Geofence>();
        mSimpleGeofenceList = new ArrayList<SimpleGeofence>();
        initMap();
        setupViews();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    public void setupViews() {


        TextView tx = (TextView) findViewById(R.id.map_titletext);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "FrederickatheGreat-Regular.ttf");
        tx.setTypeface(custom_font);


        MobileAds.initialize(getApplicationContext(), " ca-app-pub-6954747802734407~5311984177");
        AdView mAdView = (AdView) findViewById(R.id.map_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mailbtn = (ImageButton) findViewById(R.id.map_mail);
        mailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapActivity.this, ContactActivity.class);
                startActivity(i);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        homebtn = (ImageButton) findViewById(R.id.map_home);
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapActivity.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(0, 0);
                finish();
            }
        });
        settingbtn = (ImageButton) findViewById(R.id.map_setting);
        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapActivity.this, SettingActivity.class);
                startActivity(i);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        infobtn = (ImageButton) findViewById(R.id.map_info);
        infobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapActivity.this, InfoActivity.class);
                startActivity(i);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        infowindow = findViewById(R.id.map_objectinfolayout);
        infowindow.setVisibility(View.INVISIBLE);


        addresstxt = (TextView) findViewById(R.id.objinfo_addresstxt);
        descriptiontxt = (TextView) findViewById(R.id.objinfo_descriptiontxt);
        pricetxt = (TextView) findViewById(R.id.objinfo_pricetxt);
        rumtxt = (TextView) findViewById(R.id.objinfo_rumtxt);
        kvmtxt = (TextView) findViewById(R.id.objinfo_kvmtxt);
        timetxt = (TextView) findViewById(R.id.objinfo_timetxt);
        moretxt = (TextView) findViewById(R.id.objinfo_objinfo_moretxt);
        photoimg = (ImageView) findViewById(R.id.objinfo_photoimg);
        logoimg = (ImageView) findViewById(R.id.objinfo_logoimg);

        zoominbtn = (ImageButton) findViewById(R.id.zoomin);
        zoomoutbtn = (ImageButton) findViewById(R.id.zoomout);

        zoominbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentzoom < 17){
                    currentzoom += 0.5;
                    float  currentlat = Float.valueOf(Container.getInstance().currentlat);
                    float  currentlng = Float.valueOf(Container.getInstance().currentlng);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentlat,currentlng), currentzoom));
                }

            }
        });
        zoomoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentzoom > 15){
                    currentzoom -= 0.5;
                    float  currentlat = Float.valueOf(Container.getInstance().currentlat);
                    float  currentlng = Float.valueOf(Container.getInstance().currentlng);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentlat,currentlng), currentzoom));
                }

            }
        });




    }

    /*@Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        currentzoom = 16.0f;

        // add markers in objectlist
        if(Container.getInstance().objectList == null){ return;}
        if (Container.getInstance().objectList.length != 0){

            int length = Container.getInstance().objectList.length;

            for (int i = 0;i< Container.getInstance().objectList.length;i++){

                if(Container.getInstance().objectList[i] == null){ break;}
                Double lat = Double.parseDouble(Container.getInstance().objectList[i].lat);
                Double lng = Double.parseDouble(Container.getInstance().objectList[i].lng);
                LatLng objectloc = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(objectloc).icon(BitmapDescriptorFactory.fromResource(R.drawable.object1)));


            }

        }


        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                infowindow.setVisibility(View.VISIBLE);


//set data from marker
                Double markerlat = marker.getPosition().latitude;
                Double markerlng = marker.getPosition().longitude;

                for (int i = 0;i< Container.getInstance().objectList.length;i++){

                    Double lat = Double.parseDouble(Container.getInstance().objectList[i].lat);
                    Double lng = Double.parseDouble(Container.getInstance().objectList[i].lng);

                    final int  index = i;
                    if(lat.equals(markerlat) && lng.equals(markerlng)){
                        // this is marker's data
                        addresstxt.setText(Container.getInstance().objectList[i].address);
                        descriptiontxt.setText(Container.getInstance().objectList[i].descr);
                        pricetxt.setText("Pris: " + Container.getInstance().objectList[i].price + " kr");
                        rumtxt.setText("Rum: " + Container.getInstance().objectList[i].rooms + " rok");
                        kvmtxt.setText("Stirek: " + Container.getInstance().objectList[i].sqm + " kvm");


                        // processing time
                        String startsub = Container.getInstance().objectList[i].start.substring(11,16);
                        String endsub = Container.getInstance().objectList[i].end.substring(11,16);
                        timetxt.setText("Visningstid: " + startsub+"-"+endsub);


                        //setting url
                        final String object_url = Container.getInstance().objectList[i].url;
                        String htmlstr = "<a href='"+object_url+"'>Kllcka för mer information</a>";
                        Spanned Text = Html.fromHtml(htmlstr);
                        moretxt.setText(Text);

                        moretxt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                increaseNum(index);
                            }
                        });

                        // load image photo and logo
                        byte[] decodedString = Base64.decode(Container.getInstance().objectList[i].photo, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        photoimg.setImageBitmap(decodedByte);

                        decodedString = Base64.decode(Container.getInstance().objectList[i].logo, Base64.DEFAULT);
                        decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        logoimg.setImageBitmap(decodedByte);

                    }

                }

            return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                //Do what you want on obtained latLng
                Log.v("Clicked","now");
                infowindow.setVisibility(View.INVISIBLE);

            }
        });


    }*/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        currentzoom = 16.0f;
        enableMyLocation();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
        }
        if(Container.getInstance().objectList == null){ return;}
        if (Container.getInstance().objectList.length != 0){

            int length = Container.getInstance().objectList.length;

            for (int i = 0;i< Container.getInstance().objectList.length;i++){

                if(Container.getInstance().objectList[i] == null){ break;}
                Double lat = Double.parseDouble(Container.getInstance().objectList[i].lat);
                Double lng = Double.parseDouble(Container.getInstance().objectList[i].lng);
                LatLng objectloc = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(objectloc).icon(BitmapDescriptorFactory.fromResource(R.drawable.object1)));
                    CircleOptions circleOptions = new CircleOptions()
                            .center(objectloc)
                            .radius(300)
                            .fillColor(0x40ff0000)
                            .strokeColor(Color.TRANSPARENT)
                            .strokeWidth(2);
                    mMap.addCircle(circleOptions);


            }

            mMap.setOnInfoWindowClickListener(this);
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    infowindow.setVisibility(View.VISIBLE);


//set data from marker
                    Double markerlat = marker.getPosition().latitude;
                    Double markerlng = marker.getPosition().longitude;

                    for (int i = 0;i< Container.getInstance().objectList.length;i++){

                        Double lat = Double.parseDouble(Container.getInstance().objectList[i].lat);
                        Double lng = Double.parseDouble(Container.getInstance().objectList[i].lng);

                        final int  index = i;
                        if(lat.equals(markerlat) && lng.equals(markerlng)){
                            // this is marker's data
                            addresstxt.setText(Container.getInstance().objectList[i].address);
                            descriptiontxt.setText(Container.getInstance().objectList[i].descr);
                            pricetxt.setText("Pris: " + Container.getInstance().objectList[i].price + " kr");
                            rumtxt.setText("Rum: " + Container.getInstance().objectList[i].rooms + " rok");
                            kvmtxt.setText("Stirek: " + Container.getInstance().objectList[i].sqm + " kvm");


                            // processing time
                            String startsub = Container.getInstance().objectList[i].start.substring(11,16);
                            String endsub = Container.getInstance().objectList[i].end.substring(11,16);
                            timetxt.setText("Visningstid: " + startsub+"-"+endsub);


                            //setting url
                            final String object_url = Container.getInstance().objectList[i].url;
                            String htmlstr = "<a href='"+object_url+"'>Kllcka för mer information</a>";
                            Spanned Text = Html.fromHtml(htmlstr);
                            moretxt.setText(Text);

                            moretxt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    increaseNum(index);
                                }
                            });

                            // load image photo and logo
                            /*byte[] decodedString = Base64.decode(Container.getInstance().objectList[i].photo, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            photoimg.setImageBitmap(decodedByte);

                            decodedString = Base64.decode(Container.getInstance().objectList[i].logo, Base64.DEFAULT);
                            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            logoimg.setImageBitmap(decodedByte);*/

                        }

                    }

                    return false;
                }
            });
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                    //Do what you want on obtained latLng
                    Log.v("Clicked","now");
                    infowindow.setVisibility(View.INVISIBLE);

                }
            });

        }


        //setRecyclerViewAdapter();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


    public void increaseNum(int indexi)
    {
        final int index = indexi;
        // download app info
        final PostData post_inc = new PostData(this);

        String send_data = "object_id=" + String.valueOf(index) +"&object_clicked=" + Container.getInstance().objectList[index].clicked;

        //http://visningsappen.se/communicationModel/incNumofVisiter.php?object_id=2&object_clicked=2


        String info_url = "http://visningsappen.se/communicationModel/incNumofVisiter.php?" + send_data;

        post_inc.execute(info_url,"increaseNum",send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String tmp = post_inc.getClient();
                if (post_inc.getReturnCode() == 200) {
                    if (tmp.equals("")){
                        increaseNum(index);
                        return;
                    }
                    Log.d("increase step:",tmp);
                    return;
                }else{
                    Toast.makeText(getApplicationContext(), "Err!", Toast.LENGTH_LONG).show();
                }
            }
        }, 2000);

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT).show();
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

        /*if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);*/
    }


    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();

    }
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {


        // get and save location into Container.
        Container.getInstance().currentlat = String.valueOf(location.getLatitude());
        Container.getInstance().currentlng = String.valueOf(location.getLongitude());

        //place marker at current position
        //mGoogleMap.clear();
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currLocationMarker = mMap.addMarker(markerOptions);

        // move to new location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), currentzoom));

        //save location to db.
        Container.getInstance().currentlat = String.valueOf(location.getLatitude());
        Container.getInstance().currentlng = String.valueOf(location.getLongitude());
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override public void onProviderEnabled(String provider) {

    }

    @Override public void onProviderDisabled(String provider) {

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

                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
    }

    @Override protected void onResume() {
        super.onResume();
        initBroadcastReceiver();
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //unregister our receiver
        this.unregisterReceiver(this.mReceiver);
    }

    private void fetchGeofencesFromStore() {

        String[] geofenceIdList = Utility.getGeoFenceIdList(this);
        if (geofenceIdList != null && geofenceIdList.length > 0) {
            for (int i = 0; i < geofenceIdList.length; i++) {
                SimpleGeofence simpleGeofence = mGeofenceStorage.getGeofence(geofenceIdList[i]);
                mSimpleGeofenceList.add(simpleGeofence);
                mGeofenceList.add(simpleGeofence.toGeofence());
            }
        } else {
            showMessage("No Geofences Added. Please add using the plus button.");
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                    mGeofenceList.clear();
                    fetchGeofencesFromStore();
            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);
    }



}
