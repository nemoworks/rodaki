package com.nju.ics.watermark;

import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import com.nju.ics.models.HeartBeatAndRecord;

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
