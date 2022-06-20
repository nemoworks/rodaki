package com.nju.ics.funcs;

import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.Window;

import java.time.Duration;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.utils.DataSourceJudge;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;

public class VehicleWindowsTrigger<W extends Window> extends Trigger<JSONObject, W> {
    /** 之前的通行标识ID 用来判断是否是新的通行 */

    ValueStateDescriptor<String> passIdDescriptor = new ValueStateDescriptor<>("passId", String.class);
    ValueStateDescriptor<Long> triggerTimeDescriptor = new ValueStateDescriptor<>("nextTriggerTime", Long.class);

    @Override
    public TriggerResult onElement(JSONObject element, long timestamp, W window, TriggerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        ValueState<String> previousPassId = ctx.getPartitionedState(passIdDescriptor);
        String currentPassId = element.getString("PASSID");
        if (previousPassId.value() != null && previousPassId.value() != currentPassId) {
            // 说明之前没有通行记录或者变成新的通行了
            // 这时候需要根据去触发一次process
            return TriggerResult.FIRE;
        }
        previousPassId.update(currentPassId);
        // 设置定时器，防止没有新的通行记录过来
        ValueState<Long> triggerTime = ctx.getPartitionedState(triggerTimeDescriptor);
        if (triggerTime.value() != null) {
            ctx.deleteEventTimeTimer(triggerTime.value());

        }
        triggerTime.update(timestamp + Duration.ofHours(1).toMillis());
        ctx.registerEventTimeTimer(triggerTime.value());

        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onProcessingTime(long time, W window, TriggerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TriggerResult onEventTime(long time, W window, TriggerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        // 说明长时间没有新的数据到来，可以认为行程已经结束
        return TriggerResult.FIRE;
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
