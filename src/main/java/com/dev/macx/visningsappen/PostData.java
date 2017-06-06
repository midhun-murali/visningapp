package com.dev.macx.visningsappen;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;


public class PostData extends AsyncTask<String,Void,String> {
    Context context;
    String strClient = "";

    String        strScret;
    int     nRet = 0;
    AlertDialog alertDialog;



    public PostData(Context ctx) {
        context=ctx;
        strClient = "";
        strScret = "";


    }

    @Override
    protected String doInBackground(String... params) {
        String request_url=params[0];
        String choose=params[1];
        String type1=params[2];

       if(choose.equals("getAppInfo")|| choose.equals("getNoneObjMsg") ||choose.equals("getDistanceList") ||choose.equals("getPriceList") ||choose.equals("getRumList") ||choose.equals("getKvmList")  ){


              try {
                    URL url = null;

                    url = new URL(request_url);

                    HttpURLConnection httpurlcon = (HttpURLConnection) url.openConnection();

                    int code = httpurlcon.getResponseCode();
                    nRet = code;
                    if (code == 200) {
                        InputStream inputStream = httpurlcon.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                        String result = "";
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null) {
                            result += line;
                        }
                        String output = "";
                        try {
                        /* From ISO-8859-1 to UTF-8 */
                            output = new String(result.getBytes("ISO-8859-1"), "UTF-8");

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        strClient = output;
                        bufferedReader.close();
                        inputStream.close();
                        httpurlcon.disconnect();
                    } else {
                        String result = "";
                        nRet = code;
                        httpurlcon.disconnect();
                    }
                }catch (Exception e){

                }




        }else{


           try {
               URL url = null;

               url = new URL(request_url);

               HttpURLConnection httpurlcon = (HttpURLConnection) url.openConnection();

               int code = httpurlcon.getResponseCode();
               nRet = code;
               if (code == 200) {
                   InputStream inputStream = httpurlcon.getInputStream();
                   BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                   String result = "";
                   String line = "";
                   while ((line = bufferedReader.readLine()) != null) {
                       result += line;
                   }
                   String output = "";
                   try {
                       output = new String(result.getBytes("ISO-8859-1"), "UTF-8");

                   } catch (UnsupportedEncodingException e) {
                       e.printStackTrace();
                   }

                   strClient = output;
                   bufferedReader.close();
                   inputStream.close();
                   httpurlcon.disconnect();
               } else {
                   String result = "";
                   nRet = code;
                   httpurlcon.disconnect();
               }
           }catch (Exception e){
               Log.v("Exception:",e.getLocalizedMessage());
           }




    }

            return null;
    }

    protected void onPreExecute() {

        alertDialog=new AlertDialog.Builder(context).create();
        ////alertDialog.setTitle("Login Status");
    }

    @Override
    protected void onPostExecute(String result) {



    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }



    public String getClient()
    {
        return this.strClient;
    }



    public int getReturnCode()
    {
        return this.nRet;
    }

}
