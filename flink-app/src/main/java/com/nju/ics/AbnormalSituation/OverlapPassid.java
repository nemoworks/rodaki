package com.nju.ics.AbnormalSituation;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.EventComparator;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

import com.nju.ics.Connectors.RabbitMQDataSink;
import com.nju.ics.Funcs.CEPOverlapPassidPatternProcess;
import com.nju.ics.Models.OverlapPassidEvent;
import com.nju.ics.Models.TimerRecord;
import com.nju.ics.Utils.UniversalDataStreamOps;

public class OverlapPassid {
    /**
     * 同时在图：使用cep监测事件
     * 
     * @param stationRecordFixed:经过修复的timerRecord
     */
    public static void generateStream(DataStream<TimerRecord> stationRecordFixed) {
        DataStream<OverlapPassidEvent> overlapevent = new UniversalDataStreamOps.ObserveFieldChangeBuilder<TimerRecord, OverlapPassidEvent>(
                stationRecordFixed)
                .outputType(OverlapPassidEvent.class)
                .changeProcess((pre, cur) -> {
                    return OverlapPassidEvent.buildPassidChangedEvent(pre.getPASSID(), cur.getPASSID(),
                            cur.getVEHICLEID(), cur.getTIME());
                })
                .keyby(x -> x.getVEHICLEID())
                .observeField("passid")
                .postProcess((pre, cur) -> {
                    // System.out.println("after");
                    switch (cur.getFLOWTYPE()) {
                        case 1:
                            // 入站记录

                        case 2:
                            // 门架记录

                            return OverlapPassidEvent.buildOnEvent(cur.getVEHICLEID(), cur.getTIME());
                        case 3:
                            // 出口记录
                            return OverlapPassidEvent.buildOffEvent(cur.getVEHICLEID(), cur.getTIME());
                    }
                    return null;
                })
                .build();
        // 重新设置元素的时间戳
        overlapevent = overlapevent.assignTimestampsAndWatermarks(WatermarkStrategy
                .<OverlapPassidEvent>forBoundedOutOfOrderness(
                        Duration.ofMinutes(10))
                .withTimestampAssigner(
                        new OverlapPassid.OverlapPassidEventTimeassigner()))
                .setParallelism(1);

        // 使用cep来监测
        DataStream<OverlapPassidEvent> overlapeventKeyby = overlapevent
                .keyBy(new KeySelector<OverlapPassidEvent, String>() {
                    @Override
                    public String getKey(OverlapPassidEvent value) throws Exception {
                        // System.out.println(value.getVehicleid());
                        return value.getVehicleid();
                    }
                });
        AfterMatchSkipStrategy skipStrategy = AfterMatchSkipStrategy.skipPastLastEvent();
        // 必须在检测到个事件，以on event开始，passidchanged event结束
        Pattern<OverlapPassidEvent, ?> pattern = Pattern.<OverlapPassidEvent>begin("onRecord", skipStrategy)
                .where(new SimpleCondition<OverlapPassidEvent>() {
                    @Override
                    public boolean filter(OverlapPassidEvent value) throws Exception {
                        // if (value.getType() == OverlapPassidEvent.on) {
                        // System.out.println("on");
                        // }
                        return value.getType() == OverlapPassidEvent.on;
                    }
                })
                .next("passidchanged")
                .where(new SimpleCondition<OverlapPassidEvent>() {
                    @Override
                    public boolean filter(OverlapPassidEvent value) throws Exception {
                        // if (value.getType() == OverlapPassidEvent.passidChanged) {
                        // System.out.println(2);
                        // }
                        return value.getType() == OverlapPassidEvent.passidChanged;

                    }
                });
        // OutputTag<OverlapPassidEvent> timeout = new
        // OutputTag<OverlapPassidEvent>("timeout") {
        // };
        PatternStream<OverlapPassidEvent> patternStream = CEP.pattern(overlapeventKeyby, pattern,
                new EventComparator<OverlapPassidEvent>() {
                    /**
                     * 对相同时间戳的两条记录进行再次排序，因为cep会先进行时间上的堆排序，导致乱序（不稳定），这里需要再次根据类型排序(降序)
                     * 
                     * @param o1
                     * @param o2
                     * @return
                     */
                    @Override
                    public int compare(OverlapPassidEvent o1, OverlapPassidEvent o2) {
                        // TODO Auto-generated method stub
                        // passidchanged放在on前面
                        // passidchanged放在off前面
                        // System.out.println((o2.getType() - o1.getType()));
                        return (int) (o2.getType() - o1.getType());
                    }

                });
        SingleOutputStreamOperator<OverlapPassidEvent> alerts = patternStream
                .process(new CEPOverlapPassidPatternProcess());
        // DataStream<OverlapPassidEvent> lateData = alerts.getSideOutput(timeout);
        // lateData.map(new MapFunction<OverlapPassidEvent, String>() {

        // @Override
        // public String map(OverlapPassidEvent value) throws Exception {
        // // TODO Auto-generated method stub
        // // if (value.getKey().contains("鲁PC6Q78")) {
        // System.out.printf("timeout:%d %d\n", value.getType(),value.getVehicleid());
        // // value.hashCode());
        // // }
        // return null;
        // }

        // });
        alerts.addSink(RabbitMQDataSink.generateRMQSink("CEPOverlapPassid"))
                .name(String.format("RMQ:%s", "CEPOverlapPassid"));
    }

    static class OverlapPassidEventTimeassigner implements SerializableTimestampAssigner<OverlapPassidEvent> {

        @Override
        public long extractTimestamp(OverlapPassidEvent element, long recordTimestamp) {
            // TODO Auto-generated method stub
            return element.getTimestamp();
        }

    }
}
