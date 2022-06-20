package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.fastjsonutils.IntDeserializer;

public class ExitVehicleInfo extends AbstractModel {
    /** 车辆Id */
    @JSONField(name = "VEHICLEID")
    private String VEHICLEID;
    private int VLPC;
    private String VLP;

    /** 车型 */
    @JSONField(name = "VEHICLETYPE")
    private int VEHICLETYPE;

    /** 轴组信息 */
    @JSONField(name = "AXISINFO")
    private String AXISINFO;
    /** 入口、出口：限载总重(kg) */
    @JSONField(name = "LIMITWEIGHT")
    private int LIMITWEIGHT;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return VEHICLEID;
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

    public int getVEHICLETYPE() {
        return VEHICLETYPE;
    }

    @JSONField(name = "EXVEHICLETYPE", deserializeUsing = IntDeserializer.class)
    public void setVEHICLETYPE(int vEHICLETYPE) {
        VEHICLETYPE = vEHICLETYPE;
    }

    public String getAXISINFO() {
        return AXISINFO;
    }

    @JSONField(name = "AXISINFO")
    public void setAXISINFO(String aXISINFO) {
        AXISINFO = aXISINFO;
    }

    public int getLIMITWEIGHT() {
        return LIMITWEIGHT;
    }

    @JSONField(name = "LIMITWEIGHT", deserializeUsing = IntDeserializer.class)
    public void setLIMITWEIGHT(int lIMITWEIGHT) {
        LIMITWEIGHT = lIMITWEIGHT;
    }
}
