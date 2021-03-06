package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.FastJsonUtils.IntDeserializer;

public class TimerRecord {
    @JSONField(name = "FLOWTYPE")
    private int FLOWTYPE;
    @JSONField(name = "ORIGINALFLAG")
    private int ORIGINALFLAG;
    @JSONField(name = "PASSID")
    private String PASSID;
    @JSONField(name = "PROVINCEBOUND")
    private int PROVINCEBOUND;
    @JSONField(name = "STATIONID")
    private String STATIONID;
    @JSONField(name = "TIME")
    private long TIME;
    /** 车型 */
    @JSONField(name = "VEHICLETYPE")
    private int VEHICLETYPE;
    @JSONField(name = "VEHICLEID")
    private String VEHICLEID;
    private int VLPC;
    private String VLP;
    // 通行介质
    @JSONField(name = "MEDIATYPE")
    private int MEDIATYPE;
    //
    @JSONField(name = "SPECIALTYPE")
    private String SPECIALTYPE;
    @JSONField(name = "TRANSCODE")
    private String TRANSCODE;

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

    @JSONField(name = "VLPC", deserializeUsing = IntDeserializer.class)
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

    public int getVEHICLETYPE() {
        return VEHICLETYPE;
    }

    @JSONField(name = "VEHICLETYPE", deserializeUsing = IntDeserializer.class)
    public void setVEHICLETYPE(int vEHICLETYPE) {
        VEHICLETYPE = vEHICLETYPE;
    }

    public int getFLOWTYPE() {
        return FLOWTYPE;
    }

    @JSONField(name = "FLOWTYPE", deserializeUsing = IntDeserializer.class)
    public void setFLOWTYPE(int fLOWTYPE) {
        FLOWTYPE = fLOWTYPE;
    }

    public int getORIGINALFLAG() {
        return ORIGINALFLAG;
    }

    @JSONField(name = "ORIGINALFLAG", deserializeUsing = IntDeserializer.class)
    public void setORIGINALFLAG(int oRIGINALFLAG) {
        ORIGINALFLAG = oRIGINALFLAG;
    }

    public String getPASSID() {
        return PASSID;
    }

    public void setPASSID(String pASSID) {
        PASSID = pASSID;
    }

    public int getPROVINCEBOUND() {
        return PROVINCEBOUND;
    }

    @JSONField(deserializeUsing = IntDeserializer.class)
    public void setPROVINCEBOUND(int pROVINCEBOUND) {
        PROVINCEBOUND = pROVINCEBOUND;
    }

    public String getSTATIONID() {
        return STATIONID;
    }

    public void setSTATIONID(String sTATIONID) {
        STATIONID = sTATIONID;
    }

    public long getTIME() {
        return TIME;
    }

    @JSONField(alternateNames = { "_time" })
    public void setTIME(long tIME) {
        TIME = tIME;
    }

    public int getMEDIATYPE() {
        return MEDIATYPE;
    }
    @JSONField(deserializeUsing = IntDeserializer.class)
    public void setMEDIATYPE(int mEDIATYPE) {
        MEDIATYPE = mEDIATYPE;
    }

    public String getSPECIALTYPE() {
        return SPECIALTYPE;
    }

    public void setSPECIALTYPE(String sPECIALTYPE) {
        SPECIALTYPE = sPECIALTYPE;
    }

    public String getTRANSCODE() {
        return TRANSCODE;
    }

    public void setTRANSCODE(String tRANSCODE) {
        TRANSCODE = tRANSCODE;
    }
    
}
