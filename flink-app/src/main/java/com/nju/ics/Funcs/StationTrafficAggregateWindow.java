package com.nju.ics.Funcs;

import java.time.Duration;

import com.nju.ics.Models.StationTraffic;
import com.nju.ics.Models.TimerRecord;

import org.apache.curator.framework.api.TempGetDataBuilder;
import org.apache.flink.api.common.functions.AggregateFunction;

public class StationTrafficAggregateWindow implements AggregateFunction<TimerRecord, StationTraffic, StationTraffic> {
    static long onehourseconds = Duration.ofHours(1).toMillis();

    @Override
    public StationTraffic createAccumulator() {
        // TODO Auto-generated method stub
        return new StationTraffic();
    }

    @Override
    public StationTraffic add(TimerRecord value, StationTraffic accumulator) {
        // TODO Auto-generated method stub
        accumulator.addCount();
        accumulator.setId(value.getSTATIONID());
        accumulator.setTimestamp(value.getTIME() - value.getTIME() % onehourseconds);
        return accumulator;
    }

    @Override
    public StationTraffic getResult(StationTraffic accumulator) {
        // TODO Auto-generated method stub
        return accumulator;
    }

    @Override
    public StationTraffic merge(StationTraffic a, StationTraffic b) {
        // TODO Auto-generated method stub
        StationTraffic tmp = new StationTraffic();
        tmp.setId(a.getId());
        tmp.setCount(a.getCount() + b.getCount());
        tmp.setTimestamp(a.getTimestamp());
        return tmp;
    }

}
