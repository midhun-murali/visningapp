package com.dev.macx.visningsappen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ContactActivity extends AppCompatActivity {


    public ImageButton infobtn,manbtn,settingbtn,homebtn;
    private Button cancelbtn, okbtn;
    private EditText nameedt,emailedt,messageedt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        setupViews();
    }

    public void setupViews(){


        TextView tx = (TextView)findViewById(R.id.contact_titletext);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "FrederickatheGreat-Regular.ttf");
        tx.setTypeface(custom_font);


        MobileAds.initialize(getApplicationContext(), " ca-app-pub-6954747802734407~5311984177");
        AdView mAdView = (AdView) findViewById(R.id.contact_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        infobtn = (ImageButton)findViewById(R.id.contact_info);
        infobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContactActivity.this, InfoActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        manbtn = (ImageButton)findViewById(R.id.contact_man);
        manbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContactActivity.this, MapActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });
        settingbtn = (ImageButton)findViewById(R.id.contact_setting);
        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContactActivity.this, SettingActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        homebtn = (ImageButton)findViewById(R.id.contact_home);
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContactActivity.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

        nameedt = (EditText)findViewById(R.id.contact_name);
        emailedt = (EditText)findViewById(R.id.contact_email);
        messageedt = (EditText)findViewById(R.id.contact_message);
        cancelbtn = (Button)findViewById(R.id.contact_cancelbtn);
        okbtn = (Button)findViewById(R.id.contact_okbtn);

        okbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //sending message

                sendMessage();

            }
        });
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContactActivity.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(0,0);
                finish();
            }
        });

    }

    public void sendMessage()
    {

        final PostData post_message = new PostData(this);
        String send_data = "message_name=" + nameedt.getText().toString() + "&" + "message_email=" + emailedt.getText().toString() + "&" + "message_words=" + messageedt.getText().toString();
        String info_url = "http://visningsappen.se/communicationModel/message.php?" + send_data;
        post_message.execute(info_url,"message",send_data);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (post_message.getReturnCode() == 200) {

                    Toast.makeText(getApplicationContext(), "Messaeg sent successfully", Toast.LENGTH_LONG).show();
                    return;
                }else{
                    Toast.makeText(getApplicationContext(), "Err!", Toast.LENGTH_LONG).show();
                    return;
                }

            }
        }, 4000);

    }


}