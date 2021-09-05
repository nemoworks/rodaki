package com.nju.ics.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;

public class InputTimeStampAssigner implements SerializableTimestampAssigner<JSONObject> {
    SimpleDateFormat time = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    @Override
    public long extractTimestamp(JSONObject element, long recordTimestamp) {
        // TODO Auto-generated method stub
        if (element.containsKey("识别时间")) {
            // 车道牌识数据
            try {
                //System.out.println(time.parse(element.get("识别时间").asText()).getTime());
                return time.parse(element.getString("识别时间")).getTime();
            } catch (Exception e) {
                System.out.println(e);
            }
        } else if (element.containsKey("压线圈时间")) {
            // 说明是出口车道数据或者是入口车道数据
            try {
                return time.parse(element.getString("压线圈时间")).getTime();
            } catch (Exception e) {
                System.out.println(e);
            }
        } else if (element.containsKey("计费交易时间")) {
            // 说明是门架计费扣费数据
            try {
                return time.parse(element.getString("计费交易时间")).getTime();
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            // 说明是门架牌识数据，因为
            try {
                return time.parse(element.getString("门架后台匹配时间")).getTime();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return 0;
    }

}
