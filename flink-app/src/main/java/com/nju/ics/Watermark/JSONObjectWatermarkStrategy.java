package com.nju.ics.Watermark;

import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;

public class JSONObjectWatermarkStrategy<JSONObject> implements WatermarkStrategy {

    @Override
    public WatermarkGenerator createWatermarkGenerator(
            org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier.Context context) {
        // TODO Auto-generated method stub
        return null;
    }

}
