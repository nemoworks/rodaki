package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.Configs.GantryPosition;

public class GantryInfo extends AbstractModel{
    @JSONField(name = "GANTRYID")
    private String GANTRYID;
    @JSONField(name = "GANTRYNAME")
    private String GANTRYNAME;
    @JSONField(name = "GANTRYTYPE")
    private String GANTRYTYPE;
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
    
}
