package com.dev.macx.visningsappen;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Macx on 12/19/16.
 */

public class Container {

    public String appinfo;
    public String nonObjmsg;
    public ArrayList distanceList;
    public ArrayList priceList;
    public ArrayList kvmList;
    public ArrayList sqmList;
    public ObjectModel[] objectList;

    public String selectedprice;
    public String selectedrum;
    public String selectedsqm;
    public String selectedradius;

    public String alerton;
    public String soundon;

    public String initSteop;

    public String currentlat;
    public String currentlng;

    //------------

    private static Container instance = null;
    private void Container(){

    }
    public static Container getInstance(){
        if(instance==null){
            instance = new Container();
        }
        return instance;
    }



}
