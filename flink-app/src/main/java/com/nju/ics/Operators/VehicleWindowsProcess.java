package com.nju.ics.Operators;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Configs.GantryPosition;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

public class VehicleWindowsProcess<W extends Window> extends ProcessWindowFunction<JSONObject, String, String, W>{

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        GantryPosition.initGantryPosition();
    }
    @Override
    public void process(String key, ProcessWindowFunction<JSONObject, String, String, W>.Context context,
            Iterable<JSONObject> elements, Collector<String> out) throws Exception {
        // TODO Auto-generated method stub
        
    }
    
}
