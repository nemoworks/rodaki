package com.nju.ics.models;

import java.util.List;

import com.nju.ics.configs.GantryPosition;

public class TollStation extends AbstractModel {
    /** 入口：入口站号 出口：出口站号 */
    public String number;
    /** 入口：入口站号(国标) 出口：出口站号(国标) */
    public String numberId;
    /** 入口：入口站HEX编码 出口：出口站HEX编码 */
    public String hex;

    /** 经度 */
    public float LONGITUDE;
    /** 纬度 */
    public float latitude;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return numberId;
    }

    public TollStation() {
    }

    public TollStation(String number, String numberId, String hex) {
        this.number =number==null?null: number.trim();
        this.numberId =numberId==null?null: numberId.trim();
        this.hex =hex==null?null: hex.trim();
        this.latitude = (GantryPosition.geoMap.containsKey(numberId) ? GantryPosition.geoMap.get(numberId).latitude
                : 0.0f);
        this.LONGITUDE = (GantryPosition.geoMap.containsKey(numberId) ? GantryPosition.geoMap.get(numberId).longitude
                : 0.0f);
    }

}
