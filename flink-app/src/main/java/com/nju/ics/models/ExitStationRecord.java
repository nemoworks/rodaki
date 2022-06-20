package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.configs.StationPosition;
import com.nju.ics.fastJsonutils.IntDeserializer;

public class ExitStationRecord extends AbstractModel {
    /** 入口、出口： 交易流水号 */
    @JSONField(name = "EXITID")
    private String EXITID;
    /** 时间 */
    @JSONField(name = "EXTIME")
    private long EXTIME;
    /** 通行交易id */
    @JSONField(name = "PASSID")
    private String PASSID;
    /** 车站id */
    @JSONField(name = "EXTOLLSTATIONID")
    private String EXTOLLSTATIONID;
    /** 车道id */
    @JSONField(name = "EXTOLLLANEID")
    private String EXTOLLLANEID;
    /** 班次id */
    @JSONField(name = "SHIFT")
    private String SHIFT;
    /** 操作员id */
    @JSONField(name = "OPERID")
    private String OPERID;
    /** 车辆Id */
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
    @JSONField(name = "PAYID")
    private String PAYID;
    /** 重量 */
    @JSONField(name = "EXWEIGHT")
    private int EXWEIGHT;
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
    /** 识别车牌Id */
    @JSONField(name = "EXIDENTIFY")
    private String EXIDENTIFY;
    private int IDENTIFYVLPC;
    private String IDENTIFYVLP;
    /** 收费站经度 */
    @JSONField(name = "LONGITUDE")
    private float LONGITUDE;
    /** 收费站纬度 */
    @JSONField(name = "LATITUDE")
    private float LATITUDE;
    /** 收费站名称 */
    @JSONField(name = "STATIONNAME")
    private String STATIONNAME;
    
    @Override
    public String id() {
        // TODO Auto-generated method stub
        return EXITID;
    }

    public ExitStationRecord() {
    }

    public String getEXITID() {
        return EXITID;
    }

    @JSONField(alternateNames = { "ID" })
    public void setEXITID(String eXITID) {
        EXITID = eXITID;
    }

    public long getEXTIME() {
        return EXTIME;
    }

    @JSONField(alternateNames = { "_time" })
    public void setEXTIME(long eXTIME) {
        EXTIME = eXTIME;
    }

    public String getPASSID() {
        return PASSID;
    }

    @JSONField(alternateNames = { "PASSID" })
    public void setPASSID(String pASSID) {
        PASSID = pASSID==null? "":pASSID;
    }

    public String getEXTOLLSTATIONID() {
        return EXTOLLSTATIONID;
    }

    @JSONField(name = "EXTOLLSTATIONID")
    public void setEXTOLLSTATIONID(String eXTOLLSTATIONID) {
        EXTOLLSTATIONID = eXTOLLSTATIONID;
        if (StationPosition.geoMap.containsKey(EXTOLLSTATIONID)) {
            this.setSTATIONNAME(StationPosition.geoMap.get(EXTOLLSTATIONID).stationName);
            this.setLONGITUDE(StationPosition.geoMap.get(EXTOLLSTATIONID).longitude);
            this.setLATITUDE(StationPosition.geoMap.get(EXTOLLSTATIONID).latitude);
        }
    }

    public String getEXTOLLLANEID() {
        return EXTOLLLANEID;
    }

    @JSONField(name = "EXTOLLLANEID")
    public void setEXTOLLLANEID(String eXTOLLLANEID) {
        EXTOLLLANEID = eXTOLLLANEID;

    }

    public String getSHIFT() {
        return SHIFT;
    }

    @JSONField(name = "SHIFT")
    public void setSHIFT(String sHIFT) {
        SHIFT = sHIFT;
    }

    public String getOPERID() {
        return OPERID;
    }

    @JSONField(name = "OPERID")
    public void setOPERID(String oPERID) {
        OPERID = oPERID;
    }

    public String getVEHICLEID() {
        return VLP + "-" + VLPC;
    }

    @JSONField(deserialize = false)
    public void setVEHICLEID(String vEHICLEID) {
        VEHICLEID = vEHICLEID;
    }

    public int getMEDIATYPE() {
        return MEDIATYPE;
    }

    @JSONField(name = "MEDIATYPE",deserializeUsing = IntDeserializer.class)
    public void setMEDIATYPE(int mEDIATYPE) {
        MEDIATYPE = mEDIATYPE;
    }

    public String getMEDIAID() {
        return MEDIAID;
    }

    @JSONField(name = "MEDIANO")
    public void setMEDIAID(String mEDIAID) {
        MEDIAID = mEDIAID;
    }

    public int getEXWEIGHT() {
        return EXWEIGHT;
    }

    @JSONField(name = "EXWEIGHT",deserializeUsing = IntDeserializer.class)
    public void setEXWEIGHT(int eXWEIGHT) {
        EXWEIGHT = eXWEIGHT;
    }

    public String getEXIDENTIFY() {
        return IDENTIFYVLP + "-" + IDENTIFYVLPC;
    }

    @JSONField(deserialize = false)
    public void setEXIDENTIFY(String eXIDENTIFY) {
        EXIDENTIFY = eXIDENTIFY;
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

    public String getSTATIONNAME() {
        return STATIONNAME;
    }

    @JSONField(deserialize = false)
    public void setSTATIONNAME(String sTATIONNAME) {
        STATIONNAME = sTATIONNAME;
    }

    @JSONField(serialize = false)
    public int getVLPC() {
        return VLPC;
    }

    @JSONField(alternateNames = { "EXVLPC" }, deserializeUsing = IntDeserializer.class)
    public void setVLPC(int vLPC) {
        VLPC = vLPC;
    }

    @JSONField(serialize = false)
    public String getVLP() {
        return VLP;
    }

    @JSONField(alternateNames = { "EXVLP" })
    public void setVLP(String vLP) {
        VLP = vLP;
    }

    @JSONField(serialize = false)
    public int getIDENTIFYVLPC() {
        return IDENTIFYVLPC;
    }

    @JSONField(name = "IDENTIFYVLPC", deserializeUsing = IntDeserializer.class)
    public void setIDENTIFYVLPC(int iDENTIFYVLPC) {
        IDENTIFYVLPC = iDENTIFYVLPC;
    }

    @JSONField(serialize = false)
    public String getIDENTIFYVLP() {
        return IDENTIFYVLP;
    }

    @JSONField(name = "IDENTIFYVLP")
    public void setIDENTIFYVLP(String iDENTIFYVLP) {
        IDENTIFYVLP = iDENTIFYVLP;
    }

    public int getFEE() {
        return FEE;
    }

    @JSONField(name = "FEE",deserializeUsing = IntDeserializer.class)
    public void setFEE(int fEE) {
        FEE = fEE;
    }

    public int getFEEMILEAGE() {
        return FEEMILEAGE;
    }

    @JSONField(name = "FEEMILEAGE",deserializeUsing = IntDeserializer.class)
    public void setFEEMILEAGE(int fEEMILEAGE) {
        FEEMILEAGE = fEEMILEAGE;
    }

    public String getPAYID() {
        return VLP+"-"+VLPC+"-"+PASSID;
    }

    @JSONField(deserialize = false)
    public void setPAYID(String pAYID) {
        PAYID = pAYID;
    }

}
