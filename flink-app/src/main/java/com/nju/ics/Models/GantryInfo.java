package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.Configs.GantryPosition;

public class GantryInfo extends AbstractModel {
    @JSONField(name = "GANTRYID")
    private String GANTRYID;
    @JSONField(name = "GANTRYNAME")
    private String GANTRYNAME;
    @JSONField(name = "GANTRYTYPE")
    private String GANTRYTYPE;
    /** 经度 */
    @JSONField(name = "LONGITUDE")
    private float LONGITUDE;
    /** 纬度 */
    @JSONField(name = "LATITUDE")
    private float LATITUDE;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return GANTRYID;
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
        }
    }

    public String getGANTRYNAME() {
        return GANTRYNAME;
    }

    @JSONField(deserialize = false)
    public void setGANTRYNAME(String gANTRYNAME) {
        GANTRYNAME = gANTRYNAME;
    }

    public String getGANTRYTYPE() {
        return GANTRYTYPE;
    }

    @JSONField(name = "GANTRYTYPE")
    public void setGANTRYTYPE(String gANTRYTYPE) {
        GANTRYTYPE = gANTRYTYPE;
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

}
