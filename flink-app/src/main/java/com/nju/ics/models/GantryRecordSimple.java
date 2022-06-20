package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.configs.GantryPosition;
import com.nju.ics.fastjsonutils.IntDeserializer;

public class GantryRecordSimple extends AbstractModel {
    @JSONField(name = "TRANSTIME")
    private long TRANSTIME;
    @JSONField(name = "GANTRYID")
    private String GANTRYID;
    @JSONField(name = "VEHICLEID")
    private String VEHICLEID;
    private int VLPC;
    private String VLP;
    /** 车型 */
    @JSONField(name = "VEHICLETYPE")
    private int VEHICLETYPE;
    /** 名称 */
    @JSONField(name = "GANTRYNAME")
    private String GANTRYNAME;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getTRANSTIME() {
        return TRANSTIME;
    }

    @JSONField(alternateNames = { "_time" })
    public void setTRANSTIME(long tRANSTIME) {
        TRANSTIME = tRANSTIME;
    }

    
    public String getGANTRYID() {
        return GANTRYID;
    }

    @JSONField(name = "GANTRYID")
    public void setGANTRYID(String gANTRYID) {
        GANTRYID = gANTRYID;
        if (GantryPosition.geoMap.containsKey(GANTRYID)) {
            this.setGANTRYNAME(GantryPosition.geoMap.get(GANTRYID).name);
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

    

    public String getGANTRYNAME() {
        return GANTRYNAME;
    }

    @JSONField(deserialize = false)
    public void setGANTRYNAME(String gANTRYNAME) {
        GANTRYNAME = gANTRYNAME;
    }

    

    public int getVEHICLETYPE() {
        return VEHICLETYPE;
    }

    @JSONField(name = "VEHICLETYPE", deserializeUsing = IntDeserializer.class)
    public void setVEHICLETYPE(int vEHICLETYPE) {
        VEHICLETYPE = vEHICLETYPE;
    }
}
