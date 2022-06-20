package com.nju.ics.operators;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.api.common.state.StateDescriptor;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.runtime.operators.windowing.EvictingWindowOperator;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.apache.flink.streaming.runtime.operators.windowing.functions.InternalWindowFunction;
import org.apache.flink.util.OutputTag;

import com.alibaba.fastjson.JSONObject;
public class VehicleWindowsEvictor<W extends Window> implements Evictor<JSONObject, W>{
    
    @Override
    public void evictBefore(Iterable<TimestampedValue<JSONObject>> elements, int size, W window,
            EvictorContext evictorContext) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void evictAfter(Iterable<TimestampedValue<JSONObject>> elements, int size, W window,
            EvictorContext evictorContext) {
        // TODO Auto-generated method stub
        //process函数处理完一个passid后，需要将这个passid对应的所有记录全部删除
    }

    

  
    
}
