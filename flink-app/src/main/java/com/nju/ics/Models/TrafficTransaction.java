package com.nju.ics.Models;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 通行交易，入口就开始产生，不断更新信息 1. 2. 3. 4. 5. 6. 7. 8. 9. 10
 */
public class TrafficTransaction extends AbstractModel {
    /** 通行交易id */
    @JSONField(name = "PASSID")
    private String PASSID;
    /** 车辆Id */
    @JSONField(name = "VEHICLEID")
    private String VEHICLEID;
    /** 通行介质类型 */
    @JSONField(name = "MEDIATYPE")
    private int MEDIATYPE;
    /** 通行介质编号 */
    @JSONField(name = "MEDIAID")
    private String MEDIAID;
    @JSONField(name = "PAYID")
    private String PAYID;
    @JSONField(name = "ENIDENTIFY")
    private String ENIDENTIFY;
    @JSONField(name = "ENWEIGHT")
    private int ENWEIGHT;
    @JSONField(name = "EXIDENTIFY")
    private String EXIDENTIFY;
    @JSONField(name = "EXWEIGHT")
    private int EXWEIGHT;
    
    @JSONField(name = "PASSEDSITES")
    private List<Map> PASSEDSITES;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return PASSID;
    }

    public TrafficTransaction() {

    }

    public String getPASSID() {
        return PASSID;
    }

    // "通行标识ID", "通行标识 ID"
    @JSONField(alternateNames = { "PASSID" })
    public void setPASSID(String id) {
        this.PASSID = id == null ? null : id.trim();
    }

    public String getVEHICLEID() {
        return VEHICLEID;
    }

    public void setVEHICLEID(String vEHICLEID) {
        VEHICLEID = vEHICLEID;
    }

    public int getMEDIATYPE() {
        return MEDIATYPE;
    }

    public void setMEDIATYPE(int mEDIATYPE) {
        MEDIATYPE = mEDIATYPE;
    }

    public String getMEDIAID() {
        return MEDIAID;
    }

    public void setMEDIAID(String mEDIAID) {
        MEDIAID = mEDIAID;
    }

    public String getPAYID() {
        return PAYID;
    }

    public void setPAYID(String pAYID) {
        PAYID = pAYID;
    }

    public String getENIDENTIFY() {
        return ENIDENTIFY;
    }

    public void setENIDENTIFY(String eNIDENTIFY) {
        ENIDENTIFY = eNIDENTIFY;
    }

    public int getENWEIGHT() {
        return ENWEIGHT;
    }

    public void setENWEIGHT(int eNWEIGHT) {
        ENWEIGHT = eNWEIGHT;
    }

    public String getEXIDENTIFY() {
        return EXIDENTIFY;
    }

    public void setEXIDENTIFY(String eXIDENTIFY) {
        EXIDENTIFY = eXIDENTIFY;
    }

    public int getEXWEIGHT() {
        return EXWEIGHT;
    }

    public void setEXWEIGHT(int eXWEIGHT) {
        EXWEIGHT = eXWEIGHT;
    }

    public List<Map> getPASSEDSITES() {
        return PASSEDSITES;
    }

    public void setPASSEDSITES(List<Map> pASSEDSITES) {
        PASSEDSITES = pASSEDSITES;
    }
    
}
