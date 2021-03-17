package com.example.librryt19;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.librryt19.Commons.Commons;
import com.example.librryt19.Commons.CustomProgress;
import com.example.librryt19.Model.Attributes;
import com.example.librryt19.Utils.WristbandUtils;
import com.example.librryt19.ble.Bluetooth;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Conn  {
    Activity activity;
    TreeMap<Integer, String> mapData = new TreeMap<>();
    private Bluetooth bluetooth=null;
    private String bat_stat="0";
    private String temp_cen="NA";
    private String temp_frhn="NA";
    private Attributes attributes;
    public Conn(Activity activity, String data, Bluetooth bluetooth,Attributes attributes) {
        this.activity = activity;
        this.bluetooth = bluetooth;
        this.attributes = attributes;
        //if(!Commons.hasCon)
            authConnetion(data);
    }
    public void authConnetion(final String data) {
        @SuppressLint("StaticFieldLeak")
        class ConnectionAsync extends AsyncTask<String, Void, Void> {
            private final CustomProgress cp = CustomProgress.getInstance();
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                cp.showProgress(activity,"Please wait while connecting!");
                if(data.length()==0||data==null){
                    Commons.hasCon=false;
                    
                }
                else
                {
                    Commons.dvc_macId=data;
                }
            }

            @Override
            protected Void doInBackground(String... params) {
                if(data.length()>0)
                {
                    bluetooth.initialize();
                    bluetooth.connect(Commons.dvc_macId);
                }

                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                cp.hideProgress();
                if(data.length()>0)
                {
                    try {
                        System.out.println("AAAAAAAAAAAAAAAAAAAAA");
                        Commons.mDeviceAddress = Commons.dvc_macId;
                        Commons.unq_id= WristbandUtils.getAndroidId(activity);
                        bluetooth.writeDataToCharacteristic(bluetooth.getGatt().getServices().get(5).getCharacteristics().get(0),
                                WristbandUtils.hexStringToByteArray(WristbandUtils.createDateHex()));
                        Commons.hasCon=true;
                        
                    } catch (Exception e) {
                        cp.hideProgress();
                        Commons.hasCon=false;
                        
                    }
                }
                else
                {
                    Commons.hasCon=false;
                    
                }
            }
        }
        ConnectionAsync la = new ConnectionAsync();
        la.execute();
    }
    //***********************************    Retrive Data    ***********************************************
    public void display(String data) {
        if (data != null) {
            if (!data.equalsIgnoreCase("")) {
                try {
                    String[] value = data.split("\r\n");
                    for (int i = 0; i < value.length; i++) {
                        try {
                            String[] finalValue = value[i].split(",");
                            if (finalValue[0].equalsIgnoreCase("$2020-07-21")) {
                                continue;
                            }
                            String anno=String.valueOf(Float.parseFloat(finalValue[2].replace("#", ""))
                                    * 1.0 / 100);
                            System.out.println("Value is--->> "+anno);
                            String timeKey = finalValue[0].replace("$", "")
                                    .replace("-", "")
                                    + "" + finalValue[1].replace(":", "");
                            timeKey = timeKey.substring(2, timeKey.length() - 2);
                            int key = Integer.parseInt(timeKey);
                            mapData.put(key, String.valueOf(Float.parseFloat(finalValue[2].replace("#", ""))
                                    * 1.0 / 100) + "~" + finalValue[0].replace("$", " ")
                                    + "\n "
                                    + finalValue[1].replace(" ", " "));
                        } catch (Exception e) {
                            try {
                                String[] finalValue = data.split("\n");
                                bat_stat=Integer.parseInt(finalValue[1].replace("\b\n", "").trim(), 16) + "%";
                            } catch (Exception ex) {
                                // bat_stat="0 %";
                            }
                        }
                    }

                    // Get Set of entries
                    Set set = mapData.entrySet();
                    // Get iterator
                    Iterator it = set.iterator();

                    // Show TreeMap elements
                    System.out.println("TreeMap contains: ");
                    Map.Entry<Integer, String> lastEntry = mapData.lastEntry();
                    String[] finalValue1 = (lastEntry.getValue() + "").split("~");
                    DecimalFormat df1 = new DecimalFormat("#.00");
                    df1.format(0.912385);
                    float fahrenheit1, celsius1;
                    celsius1 = Float.parseFloat(finalValue1[0]);
                    fahrenheit1 = ((celsius1 * 9) / 5) + 32;
                    temp_cen=df1.format(celsius1) + " \u2103";
                    temp_frhn=df1.format(fahrenheit1) + " \u2109";
                    String dt_time=finalValue1[1].replace("\n", " at ");
                    attributes.setBattery(bat_stat);
                    attributes.setTemp_cen(temp_cen);
                    attributes.setTemp_frn(temp_frhn);
                    attributes.setDate(dt_time);
                    attributes.setDataString(data);
                } catch (Exception e) {
                    System.out.println("eRROR: "+e);
                }

            }
        }
    }
    //***********************************    Retrive Contact Sync    ***********************************************
    public void display_cntct(String data) {
        if (data != null) {
            if (!data.equalsIgnoreCase("")) {
                try {
                    String[] value = data.split("\r\n");
                    for (int i = 0; i < value.length; i++) {
                        try {
                            String[] finalValue = value[i].split(",");
                            if (finalValue[0].equalsIgnoreCase("$2020-07-21")) {
                                continue;
                            }
                           // mDataField.setText(finalValue[2].replace("#", ""));
                            String timeKey = finalValue[0].replace("$", "")
                                    .replace("-", "")
                                    + "" + finalValue[1].replace(":", "");
                            timeKey = timeKey.substring(2, timeKey.length() - 2);
                            int key = Integer.parseInt(timeKey);
                            mapData.put(key,
                                    finalValue[2].replace("#", "")
                                            + "~" + finalValue[0].replace("$", " ")
                                            + "\n "
                                            + finalValue[1].replace(" ", " "));
                        } catch (Exception e) {
                        }
                    }
                    // Get Set of entries
                    Set set = mapData.entrySet();
                    // Get iterator
                    Iterator it = set.iterator();
                    // Show TreeMap elements
                    System.out.println("TreeMap contains: ");
                    int j=0;
                    while (it.hasNext()) {
                        if(j>9){
                            break;
                        }
                        j++;
                        Map.Entry pair = (Map.Entry) it.next();
                        System.out.print("Key is: " + pair.getKey() + " and ");
                        System.out.println("Value is: " + pair.getValue());
                        String[] finalValue = (pair.getValue() + "").split("~");
                        attributes.setAlrt_wr_mac_id(finalValue[0]);
                        attributes.setCntct_dt(finalValue[1]);
                    }
                } catch (Exception e) {
                    Log.v("error", e.toString());
                }

            }
        }
    }
}
