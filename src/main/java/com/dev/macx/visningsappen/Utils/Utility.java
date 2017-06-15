package com.dev.macx.visningsappen.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * This class is used for..
 *
 * @author Midhun.
 */

public class Utility {

    private static final String GEO_LIST = "LIST";

    public static void setGeoFenceIdList(String id, Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String idListString = mPrefs.getString(GEO_LIST, "");
        if (!TextUtils.isEmpty(idListString)) {
            idListString += ","+ id ;
        } else {
            idListString = id ;
        }
        mPrefs.edit().putString(GEO_LIST, idListString).commit();
    }

    public static String[] getGeoFenceIdList(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String idListString = mPrefs.getString(GEO_LIST, "");
        ArrayList<String> idList = new ArrayList<>();
        if (!TextUtils.isEmpty(idListString)) {

            return idListString.split(",");
        } else
            return null;
    }
}