package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.configs.GantryPosition;
import com.nju.ics.fastJsonutils.IntDeserializer;

/**
 * 1. 2. 3. 4.
 */
public class Gantry extends AbstractModel {
    @JSONField(name = "GANTRYID")
    private String GANTRYID;
    @JSONField(name = "GANTRYNAME")
    private String GANTRYNAME;
    @JSONField(name = "GANTRYTYPE")
    private String GANTRYTYPE;
    @JSONField(name = "TIME")
    private long TIME;
    /** 经度 */
    @JSONField(name = "LONGITUDE")
    private float LONGITUDE;
    /** 纬度 */
    @JSONField(name = "LATITUDE")
    private float LATITUDE;
    @JSONField(name = "SPEED")
    private float SPEED;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return GANTRYID;
    }

    public String getGANTRYID() {
        return GANTRYID;
    }

    public void setGANTRYID(String gANTRYID) {
        GANTRYID = gANTRYID;
    }

    public String getGANTRYNAME() {
        return GANTRYNAME;
    }

    public void setGANTRYNAME(String gANTRYNAME) {
        GANTRYNAME = gANTRYNAME;
    }

    public String getGANTRYTYPE() {
        return GANTRYTYPE;
    }

    public void setGANTRYTYPE(String gANTRYTYPE) {
        GANTRYTYPE = gANTRYTYPE;
    }

    public long getTIME() {
        return TIME;
    }

    public void setTIME(long tIME) {
        TIME = tIME;
    }

    public float getLONGITUDE() {
        return LONGITUDE;
    }

    public void setLONGITUDE(float LONGITUDE) {
        this.LONGITUDE = LONGITUDE;
    }

    public float getLATITUDE() {
        return LATITUDE;
    }

    public void setLATITUDE(float lATITUDE) {
        LATITUDE = lATITUDE;
    }

    public float getSPEED() {
        return SPEED;
    }

    public void setSPEED(float sPEED) {
        SPEED = sPEED;
    }
    
    
}
