package com.nju.ics.operators;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.Window;

import java.time.Duration;
import java.util.Map;
public class TrafficTransactionStreamGantryTrigger<W extends Window> extends Trigger<Map, W> {
    ValueStateDescriptor<Long> triggerTimeDescriptor = new ValueStateDescriptor<>("nextTriggerTime", Long.class);
    @Override
    public TriggerResult onElement(Map element, long timestamp, W window, TriggerContext ctx) throws Exception {
        // TODO Auto-generated method stub
         // 设置定时器，防止没有新的通行记录过来
         ValueState<Long> triggerTime = ctx.getPartitionedState(triggerTimeDescriptor);
         if (triggerTime.value() != null) {
             ctx.deleteEventTimeTimer(triggerTime.value());
 
         }
         triggerTime.update(timestamp + Duration.ofHours(1).toMillis());
         ctx.registerEventTimeTimer(triggerTime.value());
        return TriggerResult.FIRE;
    }

    @Override
    public TriggerResult onProcessingTime(long time, W window, TriggerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TriggerResult onEventTime(long time, W window, TriggerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        return TriggerResult.PURGE;
    }

    @Override
    public void clear(W window, TriggerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        ValueState<Long> triggerTime = ctx.getPartitionedState(triggerTimeDescriptor);
        if (triggerTime.value() != null) {
            ctx.deleteEventTimeTimer(triggerTime.value());

        }
    }
    
}
