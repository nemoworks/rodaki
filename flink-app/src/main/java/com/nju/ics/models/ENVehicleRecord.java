package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.fastJsonutils.IntDeserializer;

public class ENVehicleRecord extends AbstractModel {
    /** 车辆Id */
    @JSONField(name="VEHICLEID")
    private String VEHICLEID;
    private int VLPC;
    private String VLP;
    /** 车型 */
    @JSONField(name="VEHICLETYPE")
    private int VEHICLETYPE;
    /** 轴组信息 */
    @JSONField(name="AXISINFO")
    private String AXISINFO;
    /** 入口、出口：限载总重(kg) */
    @JSONField(name="LIMITWEIGHT")
    private int LIMITWEIGHT;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return null;
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

    @JSONField(name = "VLPC",deserializeUsing =IntDeserializer.class )
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
    @JSONField(name = "VEHICLETYPE",deserializeUsing  = IntDeserializer.class)
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
    @JSONField(name = "LIMITWEIGHT",deserializeUsing  = IntDeserializer.class)
    public void setLIMITWEIGHT(int lIMITWEIGHT) {
        LIMITWEIGHT = lIMITWEIGHT;
    }

    
}
