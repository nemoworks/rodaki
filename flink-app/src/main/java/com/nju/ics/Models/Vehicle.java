package com.nju.ics.Models;

import com.nju.ics.Annotation.IotDBAnnotion.sinkIotDB;
import com.nju.ics.FastJsonUtils.IntDeserializer;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;

import java.util.List;
import java.util.Map;

/**
 * 收集模型里的
 * 
 * 1. 2. 4. 5. 6. 10. 11. 15. 16. 17. 18. 19. 20. 21. 22.
 */
public class Vehicle extends AbstractModel {

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return VEHICLEID;
    }

    /** 车辆Id */
    @JSONField(name = "VEHICLEID")
    private String VEHICLEID;
    /** 通行交易id */
    @JSONField(name = "PASSID")
    private String PASSID;
    /** 通行介质类型 */
    @JSONField(name = "MEDIATYPE")
    private int MEDIATYPE;
    /** 通行介质编号 */
    @JSONField(name = "MEDIAID")
    private String MEDIAID;
    /** 通行站点 */
    @JSONField(name = "PASSEDSITES")
    private List<Map> PASSEDSITES;

    /** 车型 */
    @JSONField(name = "VEHICLETYPE")
    private int VEHICLETYPE;
    /** 轴组信息 */
    @JSONField(name = "AXISINFO")
    private String AXISINFO;
    /** 限载总重(kg) */
    @JSONField(name = "LIMITWEIGHT")
    private int LIMITWEIGHT;

    /** 车辆座位数 */
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
    /** 车轴数 */
    @JSONField(name = "AXLECOUNT")
    private int AXLECOUNT;
    public String getVEHICLEID() {
        return VEHICLEID;
    }
    public void setVEHICLEID(String vEHICLEID) {
        VEHICLEID = vEHICLEID;
    }
    public String getPASSID() {
        return PASSID;
    }
    public void setPASSID(String pASSID) {
        PASSID = pASSID;
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
    public List<Map> getPASSEDSITES() {
        return PASSEDSITES;
    }
    public void setPASSEDSITES(List<Map> pASSEDSITES) {
        PASSEDSITES = pASSEDSITES;
    }
    public int getVEHICLETYPE() {
        return VEHICLETYPE;
    }
    public void setVEHICLETYPE(int vEHICLETYPE) {
        VEHICLETYPE = vEHICLETYPE;
    }
    public String getAXISINFO() {
        return AXISINFO;
    }
    public void setAXISINFO(String aXISINFO) {
        AXISINFO = aXISINFO;
    }
    public int getLIMITWEIGHT() {
        return LIMITWEIGHT;
    }
    public void setLIMITWEIGHT(int lIMITWEIGHT) {
        LIMITWEIGHT = lIMITWEIGHT;
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
