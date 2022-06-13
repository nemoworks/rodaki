package com.nju.ics.Funcs;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.nju.ics.Mappers.TimeoutEventMapper;
import com.nju.ics.Models.OverlapPassidEvent;
import com.nju.ics.Models.TimeoutEvent;
import com.nju.ics.Models.TimerRecord;
import com.nju.ics.RawType.AbnormalVehicle;

import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.functions.TimedOutPartialMatchHandler;
import org.apache.flink.util.Collector;

public class CEPOverlapPassidPatternProcess extends PatternProcessFunction<OverlapPassidEvent, OverlapPassidEvent>
        implements TimedOutPartialMatchHandler<OverlapPassidEvent> {

    @Override
    public void processMatch(Map<String, List<OverlapPassidEvent>> match, Context ctx, Collector<OverlapPassidEvent> out)
            throws Exception {
        // TODO Auto-generated method stub
        out.collect(match.get("passidchanged").get(0));
    }

    @Override
    public void processTimedOutMatch(Map<String, List<OverlapPassidEvent>> match, Context ctx) throws Exception {
        // TODO Auto-generated method stub
    }

}
