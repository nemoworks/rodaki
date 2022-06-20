package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.configs.StationPosition;
import com.nju.ics.fastJsonutils.IntDeserializer;
import com.nju.ics.utils.DataSourceJudge;

public class ENStationRecord extends AbstractModel {
    /** 入口、出口： 交易流水号 */
    @JSONField(name = "ENTRYID")
    private String ENTRYID;
    /** 时间 */
    @JSONField(name = "ENTIME")
    private long ENTIME;
    /** 通行交易id */
    @JSONField(name = "PASSID")
    private String PASSID;
    /** 车站id */
    @JSONField(name = "ENTOLLSTATIONID")
    private String ENTOLLSTATIONID;
    /** 车道id */
    @JSONField(name = "ENTOLLLANEID")
    private String ENTOLLLANEID;
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
    private String OBUID;
    private String CARDID;
    /** 重量 */
    @JSONField(name = "ENWEIGHT")
    private int ENWEIGHT;

    /** 识别车牌Id */
    @JSONField(name = "ENIDENTIFY")
    private String ENIDENTIFY;
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
        return ENTRYID;
    }

    public ENStationRecord() {
    }

    public String getENTRYID() {
        return ENTRYID;
    }

    @JSONField(alternateNames = { "ID" })
    public void setENTRYID(String eNTRYID) {
        ENTRYID = eNTRYID;
    }

    public long getENTIME() {
        return ENTIME;
    }

    @JSONField(alternateNames = { "_time" })
    public void setENTIME(long eNTIME) {
        ENTIME = eNTIME;
    }

    public String getPASSID() {
        return PASSID;
    }

    @JSONField(alternateNames = { "PASSID" }, defaultValue = "")
    public void setPASSID(String pASSID) {
        PASSID = pASSID == null ? "" : pASSID;
    }

    public String getENTOLLSTATIONID() {
        return ENTOLLSTATIONID;
    }

    @JSONField(alternateNames = "ENTOLLSTATIONID")
    public void setENTOLLSTATIONID(String eNTOLLSTATIONID) {
        ENTOLLSTATIONID = eNTOLLSTATIONID;
        if (StationPosition.geoMap.containsKey(ENTOLLSTATIONID)) {
            this.setSTATIONNAME(StationPosition.geoMap.get(ENTOLLSTATIONID).stationName);
            this.setLONGITUDE(StationPosition.geoMap.get(ENTOLLSTATIONID).longitude);
            this.setLATITUDE(StationPosition.geoMap.get(ENTOLLSTATIONID).latitude);
        }
    }

    public String getENTOLLLANEID() {
        return ENTOLLLANEID;
    }

    @JSONField(alternateNames = { "ENTOLLLANEID" })
    public void setENTOLLLANEID(String eNTOLLLANEID) {
        ENTOLLLANEID = eNTOLLLANEID;

    }

    public String getSHIFT() {
        return SHIFT;
    }

    @JSONField(alternateNames = { "SHIFT" })
    public void setSHIFT(String sHIFT) {
        SHIFT = sHIFT;
    }

    public String getOPERID() {
        return OPERID;
    }

    @JSONField(alternateNames = { "OPERID" })
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

    @JSONField(alternateNames = { "MEDIATYPE" }, deserializeUsing = IntDeserializer.class)
    public void setMEDIATYPE(int mEDIATYPE) {
        MEDIATYPE = mEDIATYPE;
    }

    public String getMEDIAID() {
        if (MEDIATYPE == 1) {
            return OBUID;
        } else {
            return CARDID;
        }
    }

    @JSONField(deserialize = false)
    public void setMEDIAID(String mEDIAID) {
        MEDIAID = mEDIAID;
    }

    @JSONField(serialize = false)
    public String getOBUID() {
        return OBUID;
    }

    @JSONField(alternateNames = { "OBUID" })
    public void setOBUID(String oBUID) {
        OBUID = oBUID;
    }

    @JSONField(serialize = false)
    public String getCARDID() {
        return CARDID;
    }

    @JSONField(alternateNames = { "CARDID" })
    public void setCARDID(String cARDID) {
        CARDID = cARDID;
    }

    public int getENWEIGHT() {
        return ENWEIGHT;
    }

    @JSONField(alternateNames = { "ENWEIGHT" }, deserializeUsing = IntDeserializer.class)
    public void setENWEIGHT(int eNWEIGHT) {
        ENWEIGHT = eNWEIGHT;
    }

    public String getENIDENTIFY() {
        return IDENTIFYVLP + "-" + IDENTIFYVLPC;
    }

    @JSONField(deserialize = false)
    public void setENIDENTIFY(String eNIDENTIFY) {
        ENIDENTIFY = eNIDENTIFY;
    }

    public float getLONGITUDE() {
        return LONGITUDE;
    }

    @JSONField(deserialize = false)
    public void setLONGITUDE(float lONGITUDE) {
        LONGITUDE = lONGITUDE;
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

}
