package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.FastJsonUtils.IntDeserializer;

/**
 * 1. 2. 3. 4.
 */
public class Gantry extends AbstractModel {
    @JSONField(name = "GANTRYID")
    private String GANTRYID;
    @JSONField(name = "GANTRYNAME")
    private String GANTRYNAME;
    @JSONField(name = "GANTRYTYPE")
    private String GANTRYTYPE;
    @JSONField(name = "TIME")
    private long TIME;
    /** 经度 */
    @JSONField(name = "LONGTITUDE")
    private float LONGTITUDE;
    /** 纬度 */
    @JSONField(name = "LATITUDE")
    private float LATITUDE;
    @JSONField(name = "SPEED")
    private float SPEED;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return GANTRYID;
    }

}
