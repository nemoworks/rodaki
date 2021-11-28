package com.nju.ics.Operators;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Utils.DataSourceJudge;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;

public class DataSourceFilterFunc implements FilterFunction<JSONObject> {

    static int count = 0;
    /** 牌识数据全部去掉 */
    

    @Override
    public boolean filter(JSONObject value) throws Exception {
        // TODO Auto-generated method stub
        int type = value.getIntValue(DataSourceJudge.sourceKey);
        switch (type) {
            case DataSourceJudge.entryLane:
            case DataSourceJudge.exitLane:
            case DataSourceJudge.gantryCharge:
                // count += 1;
                // if (count > 300000) {
                //     System.exit(0);
                // }
                return true;
            case DataSourceJudge.laneDetect:
            case DataSourceJudge.gantryDetect:
                return false;
            default:
                return false;
        }
    }

}
