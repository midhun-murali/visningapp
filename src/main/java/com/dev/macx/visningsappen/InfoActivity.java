package com.dev.macx.visningsappen;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONException;
import org.json.JSONObject;

public class InfoActivity extends AppCompatActivity {


    public ImageButton mailbtn,manbtn,settingbtn,homebtn;
    public TextView infoTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        setupViews();
        getAppInfo();
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

                    infoTextView.setText(Container.getInstance().appinfo.toString());
                    //getNoneObjMsg();
                    //Toast.makeText(getApplicationContext(),"appinfo error success!",Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(getApplicationContext(),"Network Error!",Toast.LENGTH_SHORT).show();
                    getAppInfo();
                    //finish();

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
