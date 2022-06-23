package com.nju.ics.streamjobslocal;

import java.text.SimpleDateFormat;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.types.Row;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.models.HeartBeatAndRecord;
import com.nju.ics.models.OverlapPassidEvent;
import com.nju.ics.models.TimeoutEvent;

/**
 * 使用CEP来进行简单的超时验证
 */
public class TimestampAssigners {
    public static class TimestampAssigner implements SerializableTimestampAssigner<Row> {
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public long extractTimestamp(Row element, long recordTimestamp) {
            // TODO Auto-generated method stub
            long timestamp = 0;
            try {
                timestamp = time.parse(element.getFieldAs(9)).getTime();
            } catch (Exception e) {
                // System.out.println(gantryCharge);
            }
            return timestamp;
        }

    }

    public static class EventTimestampAssigner implements SerializableTimestampAssigner<TimeoutEvent> {
        @Override
        public long extractTimestamp(TimeoutEvent element, long recordTimestamp) {
            // TODO Auto-generated method stub
            return element.getTriggertime();
        }

    }

    public static class HeartBeatAndRecordTimestampAssigner implements SerializableTimestampAssigner<HeartBeatAndRecord> {

        @Override
        public long extractTimestamp(HeartBeatAndRecord element, long recordTimestamp) {
            // TODO Auto-generated method stub
            return element.getTimestamp();
        }

    }

    public static class JSONObjectTimestampAssigner implements SerializableTimestampAssigner<JSONObject> {
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public long extractTimestamp(JSONObject element, long recordTimestamp) {
            // TODO Auto-generated method stub
            long timestamp = 0;
            try {
                timestamp = time.parse(element.getString("TIMESTRING")).getTime();
            } catch (Exception e) {
                // System.out.println(gantryCharge);
            }
            return timestamp;
        }

    }

    public static class OverlapPassidEventTimeassigner implements SerializableTimestampAssigner<OverlapPassidEvent> {
    
        @Override
        public long extractTimestamp(OverlapPassidEvent element, long recordTimestamp) {
            // TODO Auto-generated method stub
            return element.getTimestamp();
        }
    
    }
}
