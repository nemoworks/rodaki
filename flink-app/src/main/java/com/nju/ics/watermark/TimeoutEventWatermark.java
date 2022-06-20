package com.nju.ics.watermark;

import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import com.nju.ics.models.TimeoutEvent;

public class TimeoutEventWatermark implements AssignerWithPunctuatedWatermarks<TimeoutEvent> {
    private long max = 0;

    @Override
    public long extractTimestamp(TimeoutEvent element, long recordTimestamp) {
        // TODO Auto-generated method stub
        return element.getTriggertime();
    }

    @Override
    public Watermark checkAndGetNextWatermark(TimeoutEvent lastElement, long extractedTimestamp) {
        // TODO Auto-generated method stub
        if (extractedTimestamp > max) {
            max = extractedTimestamp;
            return new Watermark(extractedTimestamp-1);
        } else {
            return new Watermark(max-1);
        }
    }

}
