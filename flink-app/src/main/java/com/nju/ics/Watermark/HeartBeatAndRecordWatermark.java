package com.nju.ics.Watermark;

import com.nju.ics.Models.HeartBeatAndRecord;

import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

public class HeartBeatAndRecordWatermark implements AssignerWithPunctuatedWatermarks<HeartBeatAndRecord> {
    private long max = 0;

    @Override
    public long extractTimestamp(HeartBeatAndRecord element, long recordTimestamp) {
        // TODO Auto-generated method stub

        return element.getTimestamp();

    }

    @Override
    public Watermark checkAndGetNextWatermark(HeartBeatAndRecord lastElement, long extractedTimestamp) {
        // TODO Auto-generated method stub
        if (extractedTimestamp > max) {
            max = extractedTimestamp;
            return new Watermark(extractedTimestamp - 1);
        } else {
            return new Watermark(max - 1);
        }
    }

}
