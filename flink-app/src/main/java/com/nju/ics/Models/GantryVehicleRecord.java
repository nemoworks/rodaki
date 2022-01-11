package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;

public class GantryVehicleRecord extends AbstractModel {
    /** 车辆Id */
    @JSONField(name = "VEHICLEID")
    private String VEHICLEID;
    private int VLPC;
    private String VLP;


    @JSONField(name = "VEHICLESEAT")
    private int VEHICLESEAT;

    /** 门架：车辆长 */
    @JSONField(name = "VEHICLELENGTH")
    private int VEHICLELENGTH;
    /** 门架：车辆宽 */
    @JSONField(name = "VEHICLEWIDTH")
    private int VEHICLEWIDTH;
    /** 门架：车辆高 */
    @JSONField(name = "VEHICLEHIGHT")
    private int VEHICLEHIGHT;
    @JSONField(name = "AXLECOUNT")
    private int AXLECOUNT;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getVEHICLEID() {
        return VLP + "-" + VLPC;
    }

    @JSONField(deserialize = false)
    public void setVEHICLEID(String vEHICLEID) {
        VEHICLEID = vEHICLEID;
    }

    @JSONField(serialize = false)
    public int getVLPC() {
        return VLPC;
    }

    @JSONField(name = "VLPC")
    public void setVLPC(int vLPC) {
        VLPC = vLPC;
    }

    @JSONField(serialize = false)
    public String getVLP() {
        return VLP;
    }

    @JSONField(name = "VLP")
    public void setVLP(String vLP) {
        VLP = vLP;
    }

    public int getVEHICLESEAT() {
        return VEHICLESEAT;
    }

    public void setVEHICLESEAT(int vEHICLESEAT) {
        VEHICLESEAT = vEHICLESEAT;
    }

    public int getVEHICLELENGTH() {
        return VEHICLELENGTH;
    }

    public void setVEHICLELENGTH(int vEHICLELENGTH) {
        VEHICLELENGTH = vEHICLELENGTH;
    }

    public int getVEHICLEWIDTH() {
        return VEHICLEWIDTH;
    }

    public void setVEHICLEWIDTH(int vEHICLEWIDTH) {
        VEHICLEWIDTH = vEHICLEWIDTH;
    }

    public int getVEHICLEHIGHT() {
        return VEHICLEHIGHT;
    }

    public void setVEHICLEHIGHT(int vEHICLEHIGHT) {
        VEHICLEHIGHT = vEHICLEHIGHT;
    }

    public int getAXLECOUNT() {
        return AXLECOUNT;
    }

    public void setAXLECOUNT(int aXLECOUNT) {
        AXLECOUNT = aXLECOUNT;
    }

    
}
