package com.cvicse.highway.domain;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.stereotype.Component;
import org.springframework.data.annotation.Id;

@Component
@Document(indexName = "test_01", createIndex = false)
public class TradeInfo {
    @Id
    private String id;
    @Field(type = FieldType.Keyword)
    private String gantryId;// 门架编号

    @Field(type = FieldType.Keyword)
    private String gantryHex;// 门架HEX值
    @Field(type = FieldType.Date)
    private String transTime;// 计费交易时间
    @Field(type = FieldType.Keyword)
    private String lastGantryHex;// 上个门架hex编码
    @Field(type = FieldType.Keyword)
    private String vehiclePlate;// 车牌号
    @Field(type = FieldType.Auto)
    private float[] geo = new float[2];

    public String getgantryId() {
        return gantryId;
    }

    public void setgantryId(String gantryId) {
        this.gantryId = gantryId;
    }

    public String getgantryHex() {
        return gantryHex;
    }

    public void setgantryHex(String gantryHex) {
        this.gantryHex = gantryHex;
    }

    public String gettransTime() {
        return transTime;
    }

    public void settransTime(String transTime) {
        if (transTime.charAt(10) == ' ') {
            char[] s = transTime.toCharArray();
            s[10] = 'T';
            transTime = String.valueOf(s);
        }
        this.transTime = transTime;
    }

    public String getvehiclePlate() {
        return vehiclePlate;
    }

    public void setvehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate.split("_")[0];
        // System.out.println(this.vehiclePlate);
    }

    public String getlastGantryHex() {
        return lastGantryHex;
    }

    public void setlastGantryHex(String lastGantryHex) {
        this.lastGantryHex = lastGantryHex;
    }

    public float[] getGeo() {
        return geo;
    }

    public void setGeo(float[] geo) {
        this.geo = geo;
    }
}
