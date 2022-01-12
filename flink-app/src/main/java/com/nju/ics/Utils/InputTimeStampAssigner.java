package com.nju.ics.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;

public class InputTimeStampAssigner implements SerializableTimestampAssigner<JSONObject> {
    SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public long extractTimestamp(JSONObject element, long recordTimestamp) {
        // TODO Auto-generated method stub
        long timestamp=DataSourceJudge.typeDetectAndTime(element,time);
        
        return timestamp;
       
       
    }

}
