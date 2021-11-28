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
    @JSONField(name = "LONGTITUDE")
    private float LONGTITUDE;
    /** 纬度 */
    @JSONField(name = "LATITUDE")
    private float LATITUDE;
    /** 名称 */
    @JSONField(name = "GANTRYNAME")
    private String GANTRYNAME;

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

    @JSONField(name = "_time")
    public void setTRANSTIME(long tRANSTIME) {
        TRANSTIME = tRANSTIME;
    }

    public String getPASSID() {
        return PASSID;
    }

    @JSONField(name = "PASSID", defaultValue = "")
    public void setPASSID(String pASSID) {
        PASSID = pASSID==null? "":pASSID;
    }

    public String getGANTRYID() {
        return GANTRYID;
    }

    @JSONField(name = "GANTRYID")
    public void setGANTRYID(String gANTRYID) {
        GANTRYID = gANTRYID;
        if (GantryPosition.geoMap.containsKey(GANTRYID)) {
            this.setGANTRYNAME(GantryPosition.geoMap.get(GANTRYID).name);
            this.setLONGTITUDE(GantryPosition.geoMap.get(GANTRYID).longtitude);
            this.setLATITUDE(GantryPosition.geoMap.get(GANTRYID).latitude);
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

    @JSONField(name = "OBUSN")
    public void setMEDIAID(String mEDIAID) {
        MEDIAID = mEDIAID;
    }

    public int getFEE() {
        return FEE;
    }

    @JSONField(name = "FEE")
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

    public float getLONGTITUDE() {
        return LONGTITUDE;
    }

    @JSONField(deserialize = false)
    public void setLONGTITUDE(float lONGTITUDE) {
        LONGTITUDE = lONGTITUDE;
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

}
