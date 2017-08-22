package com.dev.macx.visningsappen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public ImageButton infobtn,manbtn,settingbtn,mailbtn;
    private ProgressBar spinner;
    private ProgressDialog dialog1,dialog2,dialog3,dialog4,dialog5,dialog6,dialog7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView tx = (TextView)findViewById(R.id.titletext);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "FrederickatheGreat-Regular.ttf");
        tx.setTypeface(custom_font);


        MobileAds.initialize(getApplicationContext(), " ca-app-pub-6954747802734407~5311984177");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        setupViews();




    }

    public void setupViews(){
        infobtn = (ImageButton)findViewById(R.id.main_info);
        infobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        manbtn = (ImageButton)findViewById(R.id.main_man);
        manbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });
        settingbtn = (ImageButton)findViewById(R.id.main_setting);
        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        mailbtn = (ImageButton)findViewById(R.id.main_mail);
        mailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ContactActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });


    }



}