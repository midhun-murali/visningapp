package com.dev.macx.visningsappen;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class InfoActivity extends AppCompatActivity {


    public ImageButton mailbtn,manbtn,settingbtn,homebtn;
    public TextView infoTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        setupViews();
        infoTextView.setText(Container.getInstance().appinfo.toString());
    }


    public void setupViews(){

        TextView tx = (TextView)findViewById(R.id.info_titletext);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "FrederickatheGreat-Regular.ttf");
        tx.setTypeface(custom_font);


        MobileAds.initialize(getApplicationContext(), " ca-app-pub-6954747802734407~5311984177");
        AdView mAdView = (AdView) findViewById(R.id.info_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mailbtn = (ImageButton)findViewById(R.id.info_mail);
        mailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(InfoActivity.this, ContactActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        manbtn = (ImageButton)findViewById(R.id.info_man);
        manbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(InfoActivity.this, MapActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });
        settingbtn = (ImageButton)findViewById(R.id.info_setting);
        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(InfoActivity.this, SettingActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        homebtn = (ImageButton)findViewById(R.id.info_home);
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(InfoActivity.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        infoTextView =  (TextView)findViewById(R.id.infoTextview);

    }
}
