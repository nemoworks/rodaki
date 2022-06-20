package com.nju.ics.funcs;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import com.nju.ics.models.HeartBeatAndRecord;
import com.nju.ics.models.TimerRecord;

public class GenerateHeartBeatProcess extends KeyedProcessFunction<String, TimerRecord, HeartBeatAndRecord> {
    private ValueState<Integer> count;
    private ValueState<Long> nextTimer;
    ValueStateDescriptor<Integer> countValueStateDescriptor = new ValueStateDescriptor<Integer>(
            "count", Integer.class);
    ValueStateDescriptor<Long> nextTimerValueStateDescriptor = new ValueStateDescriptor<Long>(
            "nextTimer", Long.class);
    private static long onehour = Duration.ofHours(1).toMillis();

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        count = getRuntimeContext().getState(countValueStateDescriptor);
        nextTimer = getRuntimeContext().getState(nextTimerValueStateDescriptor);
    }

    @Override
    public void processElement(TimerRecord value,
            KeyedProcessFunction<String, TimerRecord, HeartBeatAndRecord>.Context ctx,
            Collector<HeartBeatAndRecord> out) throws Exception {
        if (nextTimer.value() == null) {
            count.update(0);
            nextTimer.update(ctx.timestamp() + onehour);
            ctx.timerService().registerEventTimeTimer(nextTimer.value());

        } else {
            count.update(0);
            ctx.timerService().deleteEventTimeTimer(nextTimer.value());
            nextTimer.update(ctx.timestamp() + onehour);
            ctx.timerService().registerEventTimeTimer(nextTimer.value());
        }
        // 直接向后输出经过包装后的元素
        // if (ctx.getCurrentKey().contains("鲁PC6Q78")) {
        //     System.out.printf("1:%d %s\n", value.getTIME(), value.getVEHICLEID());
        // }
        out.collect(HeartBeatAndRecord.build(value));
    }

    @Override
    public void onTimer(long timestamp,
            KeyedProcessFunction<String, TimerRecord, HeartBeatAndRecord>.OnTimerContext ctx,
            Collector<HeartBeatAndRecord> out) throws Exception {
        // TODO Auto-generated method stub
        // if (ctx.getCurrentKey().contains("鲁PC6Q78")) {
        //     System.out.printf("2:%d %s\n", timestamp, ctx.getCurrentKey());
        // }
        out.collect(HeartBeatAndRecord.build(ctx.getCurrentKey(), timestamp));
        count.update(count.value() + 1);
        // 循环注册,避免batch模式下循环注册产生heatbeat，导致任务停不下来
        if (count.value() < 10) {
            nextTimer.update(timestamp + onehour);
            ctx.timerService().registerEventTimeTimer(timestamp + onehour);
        } else {
            count.clear();
            nextTimer.clear();
        }

    }
}
