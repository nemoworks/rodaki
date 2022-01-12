package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.FastJsonUtils.IntDeserializer;

public class GantryRecord extends AbstractModel {
    @JSONField(name = "TRADEID")
    private String TRADEID;
    @JSONField(name = "TRANSTIME")
    private long TRANSTIME;
    @JSONField(name = "PASSID")
    private String PASSID;
    @JSONField(name = "GANTRYID")
    private String GANTRYID;
    @JSONField(name = "VEHICLEID")
    private String VEHICLEID;
    private int VLPC;
    private String VLP;
    /** 车型 */
    @JSONField(name = "VEHICLETYPE")
    private int VEHICLETYPE;
    /** 通行介质类型 */
    @JSONField(name = "MEDIATYPE")
    private int MEDIATYPE;
    /** 通行介质编号 */
    @JSONField(name = "MEDIAID")
    private String MEDIAID;

    /**
     * 通行费用 入口： 门架：交易金额 出口：总交易金额
     */
    @JSONField(name = "FEE")
    private int FEE;
    /**
     * 通行里程 入口： 门架：计费里程数 出口：计费总里程数
     */
    @JSONField(name = "FEEMILEAGE")
    private int FEEMILEAGE;
    /** 经度 */
    @JSONField(name = "LONGITUDE")
    private float LONGITUDE;
    /** 纬度 */
    @JSONField(name = "LATITUDE")
    private float LATITUDE;

    /** 省入口、省出口、普通 */
    @JSONField(name = "GANTRYPOSITIONFLAG")
    private String GANTRYPOSITIONFLAG;

    /** 名称 */
    @JSONField(name = "GANTRYNAME")
    private String GANTRYNAME;
    @JSONField(name = "SPECIALTYPE")
    private String SPECIALTYPE;
    @JSONField(name = "ORIGINALFLAG")
    private String ORIGINALFLAG;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTRADEID() {
        return TRADEID;
    }

    @JSONField(name = "TRADEID")
    public void setTRADEID(String tRADEID) {
        TRADEID = tRADEID;
    }

    public long getTRANSTIME() {
        return TRANSTIME;
    }

    @JSONField(alternateNames = { "_time" })
    public void setTRANSTIME(long tRANSTIME) {
        TRANSTIME = tRANSTIME;
    }

    public String getPASSID() {
        return PASSID;
    }

    @JSONField(name = "PASSID", defaultValue = "")
    public void setPASSID(String pASSID) {
        PASSID = pASSID == null ? "" : pASSID;
    }

    public String getGANTRYID() {
        return GANTRYID;
    }

    @JSONField(name = "GANTRYID")
    public void setGANTRYID(String gANTRYID) {
        GANTRYID = gANTRYID;
        if (GantryPosition.geoMap.containsKey(GANTRYID)) {
            this.setGANTRYNAME(GantryPosition.geoMap.get(GANTRYID).name);
            this.setLONGITUDE(GantryPosition.geoMap.get(GANTRYID).longitude);
            this.setLATITUDE(GantryPosition.geoMap.get(GANTRYID).latitude);
            this.setGANTRYPOSITIONFLAG(GantryPosition.geoMap.get(GANTRYID).gantryPositionFlag);
        }
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

    public int getMEDIATYPE() {
        return MEDIATYPE;
    }

    @JSONField(name = "MEDIATYPE")
    public void setMEDIATYPE(int mEDIATYPE) {
        MEDIATYPE = mEDIATYPE;
    }

    public String getMEDIAID() {
        return MEDIAID;
    }

    @JSONField(alternateNames = { "OBUSN" })
    public void setMEDIAID(String mEDIAID) {
        MEDIAID = mEDIAID;
    }

    public int getFEE() {
        return FEE;
    }

    @JSONField(name = "FEE", deserializeUsing = IntDeserializer.class)
    public void setFEE(int fEE) {
        FEE = fEE;
    }

    public int getFEEMILEAGE() {
        return FEEMILEAGE;
    }

    @JSONField(name = "FEEMILEAGE")
    public void setFEEMILEAGE(int fEEMILEAGE) {
        FEEMILEAGE = fEEMILEAGE;
    }

    public float getLONGITUDE() {
        return LONGITUDE;
    }

    @JSONField(deserialize = false)
    public void setLONGITUDE(float LONGITUDE) {
        this.LONGITUDE = LONGITUDE;
    }

    public float getLATITUDE() {
        return LATITUDE;
    }

    @JSONField(deserialize = false)
    public void setLATITUDE(float lATITUDE) {
        LATITUDE = lATITUDE;
    }

    public String getGANTRYNAME() {
        return GANTRYNAME;
    }

    @JSONField(deserialize = false)
    public void setGANTRYNAME(String gANTRYNAME) {
        GANTRYNAME = gANTRYNAME;
    }

    public String getSPECIALTYPE() {
        return SPECIALTYPE;
    }

    @JSONField(alternateNames = { "SPECIALTYPE" })
    public void setSPECIALTYPE(String sPECIALTYPE) {
        SPECIALTYPE = sPECIALTYPE;
    }

    public String getORIGINALFLAG() {
        return ORIGINALFLAG;
    }

    @JSONField(alternateNames = { "ORIGINALFLAG" })
    public void setORIGINALFLAG(String oRIGINALFLAG) {
        ORIGINALFLAG = oRIGINALFLAG;
    }

    public String getGANTRYPOSITIONFLAG() {
        return GANTRYPOSITIONFLAG;
    }

    @JSONField(deserialize = false)
    public void setGANTRYPOSITIONFLAG(String gANTRYPOSITIONFLAG) {
        GANTRYPOSITIONFLAG = gANTRYPOSITIONFLAG;
    }

    public int getVEHICLETYPE() {
        return VEHICLETYPE;
    }

    @JSONField(name = "VEHICLETYPE", deserializeUsing = IntDeserializer.class)
    public void setVEHICLETYPE(int vEHICLETYPE) {
        VEHICLETYPE = vEHICLETYPE;
    }
}
