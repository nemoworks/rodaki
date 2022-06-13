package com.nju.ics.Funcs;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.nju.ics.Mappers.TimeoutEventMapper;
import com.nju.ics.Models.HeartBeatAndRecord;
import com.nju.ics.Models.TimeoutEvent;
import com.nju.ics.Models.TimerRecord;
import com.nju.ics.RawType.AbnormalVehicle;

import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.functions.TimedOutPartialMatchHandler;
import org.apache.flink.util.Collector;

public class CEPGantryTimerPatternProcess extends PatternProcessFunction<HeartBeatAndRecord, TimeoutEvent>
        implements TimedOutPartialMatchHandler<HeartBeatAndRecord> {

    @Override
    public void processMatch(Map<String, List<HeartBeatAndRecord>> match, Context ctx, Collector<TimeoutEvent> out)
            throws Exception {
        // TODO Auto-generated method stub
        out.collect(TimeoutEventMapper.INSTANCE.HeartBeatAndRecordToTimeoutEvent(match.get("startRecord").get(0)));
    }

    @Override
    public void processTimedOutMatch(Map<String, List<HeartBeatAndRecord>> match, Context ctx) throws Exception {
        // TODO Auto-generated method stub
        if (match.get("startRecord").get(0).getKey().contains("鲁PC6Q78")) {
            System.out.printf("timeout :%d \n", match.get("startRecord").get(0).getTimestamp());
        }
    }

}
