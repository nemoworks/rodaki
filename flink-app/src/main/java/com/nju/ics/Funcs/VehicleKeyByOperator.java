package com.nju.ics.Funcs;

import java.lang.String;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Utils.DataSourceJudge;

import org.apache.flink.api.java.functions.KeySelector;

public class VehicleKeyByOperator implements KeySelector<JSONObject, String> {
    /** 将车牌颜色与车牌号提取出来
     * <br>
     * <br>
     * 颜色与号码唯一地标识一个车子
     */
    @Override
    public String getKey(JSONObject value) throws Exception {
        // TODO Auto-generated method stub
        int type = value.getIntValue(DataSourceJudge.sourceKey);
        String vehicleNumber=null;
        String vehicleColor=null;
        switch (type) {
            case DataSourceJudge.entryLane:
                vehicleNumber = value.getString("VLP");
                vehicleColor = value.getString("VLPC");
                break;
            case DataSourceJudge.exitLane:
                vehicleNumber = value.getString("EXVLP");
                vehicleColor = value.getString("EXVLPC");
                break;

            case DataSourceJudge.gantryCharge:
                vehicleNumber = value.getString("VLP");
                vehicleColor = value.getString("VLPC");
                break;

        }
        return vehicleNumber+"-"+vehicleColor;
    }

}
