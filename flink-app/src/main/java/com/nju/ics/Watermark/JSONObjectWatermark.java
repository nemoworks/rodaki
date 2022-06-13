package com.nju.ics.Watermark;

import java.text.SimpleDateFormat;

import com.alibaba.fastjson.JSONObject;

import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

public class JSONObjectWatermark implements AssignerWithPunctuatedWatermarks<JSONObject> {
    SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private long max = 0;

    @Override
    public long extractTimestamp(JSONObject element, long recordTimestamp) {
        // TODO Auto-generated method stub
        long timestamp = 0;

        try {

            timestamp = time.parse(element.getString("TIMESTRING")).getTime();

        } catch (Exception e) {
            // System.out.println(gantryCharge);
        }
        // System.out.printf("extract:%d",timestamp);
        return timestamp;
    }

    @Override
    public Watermark checkAndGetNextWatermark(JSONObject lastElement, long extractedTimestamp) {
        // TODO Auto-generated method stub
        // System.out.printf("watermark %d\n",extractedTimestamp);
        if (extractedTimestamp > max) {
            max = extractedTimestamp;
            return new Watermark(extractedTimestamp-1);
        } else {
            return new Watermark(max-1);
        }

    }

}
