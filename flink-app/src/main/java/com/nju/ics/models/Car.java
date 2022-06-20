package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import com.nju.ics.fastjsonutils.IntDeserializer;

@UDT(keyspace = "test", name = "Car")
public class Car extends AbstractModel {

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return this.getVEHICLEID();
    }

    public Car() {

    }

    public Car(int vLPC, String vLP, String pASSID, String tRADEID, String sTATIONID, long tIME) {
        VLPC = vLPC;
        VLP = vLP;
        PASSID = pASSID;
        TRADEID = tRADEID;
        STATIONID = sTATIONID;
        TIME = tIME;

    }

    /** 车辆Id */
    @Field(name = "vehicleid")
    @JSONField(name = "VEHICLEID")
    private String VEHICLEID;
    @Field(name = "vlpc")
    private int VLPC;
    @Field(name = "vlp")
    private String VLP;
    /** 通行id */
    @Field(name = "passid")
    @JSONField(name = "PASSID")
    private String PASSID;
    /** 交易id */
    @Field(name = "tradeid")
    @JSONField(name = "TRADEID")
    private String TRADEID;
    @Field(name = "stationid")
    @JSONField(name = "STATIONID")
    private String STATIONID;
    /**
     * 1:入站
     * 2:门架
     * 3:出站
     */
    @Field(name = "stationtype")
    @JSONField(name = "STATIONTYPE")
    private int STATIONTYPE;
    /** 记录产生的时间 */
    @Field(name = "time")
    @JSONField(name = "TIME")
    private long TIME;
    /** 行驶时间 */
    @JSONField(name = "PASSTIME")
    @Field(name = "passtime")
    private long PASSTIME;
    /** 行驶里程 */
    @JSONField(name = "PASSMILES")
    @Field(name = "passmiles")
    private int PASSMILES;

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

    public String getPASSID() {
        return PASSID;
    }

    public void setPASSID(String pASSID) {
        PASSID = pASSID;
    }

    public String getTRADEID() {
        return TRADEID;
    }

    public void setTRADEID(String tRADEID) {
        TRADEID = tRADEID;
    }

    public String getSTATIONID() {
        return STATIONID;
    }

    @JSONField(deserialize = false)
    public void setSTATIONID(String sTATIONID) {
        STATIONID = sTATIONID;
    }

    public int getSTATIONTYPE() {
        return STATIONTYPE;
    }

    @JSONField(deserialize = false)
    public void setSTATIONTYPE(int sTATIONTYPE) {
        STATIONTYPE = sTATIONTYPE;
    }

    public long getTIME() {
        return TIME;
    }

    @JSONField(alternateNames = { "_time" })
    public void setTIME(long tIME) {
        TIME = tIME;
    }

    public long getPASSTIME() {
        return PASSTIME;
    }

    @JSONField(deserialize = false)
    public void setPASSTIME(long pASSTIME) {
        PASSTIME = pASSTIME;
    }

    public int getPASSMILES() {
        return PASSMILES;
    }

    @JSONField(deserialize = false)
    public void setPASSMILES(int pASSMILES) {
        PASSMILES = pASSMILES;
    }

}
