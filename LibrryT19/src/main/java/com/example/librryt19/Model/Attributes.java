package com.example.librryt19.Model;

public class Attributes {
    private  String dataString="";
    private  String battery="0 %";
    private  String date="NA";
    private  String temp_cen="NA";
    private  String temp_frn="NA";
    private  String alrt_wr_mac_id="";
    private  String cntct_dt="";

    public  String getAlrt_wr_mac_id() {
        return alrt_wr_mac_id;
    }

    public  void setAlrt_wr_mac_id(String alrt_wr_mac_id) {
        this.alrt_wr_mac_id = alrt_wr_mac_id;
    }

    public  String getCntct_dt() {
        return cntct_dt;
    }

    public  void setCntct_dt(String cntct_dt) {
        this.cntct_dt = cntct_dt;
    }
    public  String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    public  String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public  String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public  String getTemp_cen() {
        return temp_cen;
    }

    public void setTemp_cen(String temp_cen) {
        this.temp_cen = temp_cen;
    }

    public  String getTemp_frn() {
        return temp_frn;
    }

    public void setTemp_frn(String temp_frn) {
        this.temp_frn = temp_frn;
    }
}
