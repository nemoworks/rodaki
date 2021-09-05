package com.nju.ics.Operators;

import java.lang.String;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Utils.DataSourceJudge;
import com.typesafe.config.ConfigException.Null;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;

public class VehicleKeyByOperator implements KeySelector<JSONObject, String> {
    /** 将车牌颜色与车牌号提取出来
     * <br>
     * <br>
     * 颜色与号码唯一地标识一个车子
     */
    @Override
    public String getKey(JSONObject value) throws Exception {
        // TODO Auto-generated method stub
        int type = DataSourceJudge.typeDetect(value);
        String vehicleNumber=null;
        String vehicleColor=null;
        switch (type) {
            case DataSourceJudge.entryLane:
                vehicleNumber = value.getString("车牌号");
                vehicleColor = value.getString("车牌颜色");
                break;
            case DataSourceJudge.exitLane:
                vehicleNumber = value.getString("出口实际车牌号");
                vehicleColor = value.getString("出口实际车牌颜色");
                break;

            case DataSourceJudge.gantryCharge:
                vehicleNumber = value.getString("计费车牌号");
                vehicleColor = value.getString("计费车牌颜色");
                break;

        }
        return vehicleNumber+"-"+vehicleColor;
    }

}
