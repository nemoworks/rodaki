package com.nju.ics.abnormalsituation;

import java.time.Duration;

import com.nju.ics.connectors.RabbitMQDataSink;
import com.nju.ics.funcs.CEPGantryTimerPatternProcess;
import com.nju.ics.funcs.GenerateHeartBeatProcess;
import com.nju.ics.models.HeartBeatAndRecord;
import com.nju.ics.models.TimeoutEvent;
import com.nju.ics.models.TimerRecord;
import com.nju.ics.streamjobslocal.TimestampAssigners;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

/**
 * 在经过修复的TimerRecord stream上进行超时监测
 */
public class TimeoutSituation {
    /**
     * 使用cep规则来监测超时问题
     * 
     * @param recordFixed
     */
    public static void generateStream(DataStream<TimerRecord> recordFixed) {
        DataStream<HeartBeatAndRecord> timeoutEvent = recordFixed.keyBy(x -> x.getVEHICLEID())
                .process(new GenerateHeartBeatProcess());
        // 重新设置元素的时间戳
        timeoutEvent = timeoutEvent.assignTimestampsAndWatermarks(WatermarkStrategy
                .<HeartBeatAndRecord>forBoundedOutOfOrderness(
                        Duration.ofMinutes(10))
                .withTimestampAssigner(
                        new TimestampAssigners.HeartBeatAndRecordTimestampAssigner()))
                .setParallelism(1);

        DataStream<HeartBeatAndRecord> timeoutEventKeyby = timeoutEvent
                .keyBy(new KeySelector<HeartBeatAndRecord, String>() {
                    @Override
                    public String getKey(HeartBeatAndRecord value) throws Exception {
                        // System.out.println(value.getKey());
                        return value.getKey();
                    }
                });
        AfterMatchSkipStrategy skipStrategy = AfterMatchSkipStrategy.skipPastLastEvent();
        // 必须在检测到个事件，以通行记录开头，5个heartbeat结束
        Pattern<HeartBeatAndRecord, ?> pattern = Pattern.<HeartBeatAndRecord>begin("startRecord", skipStrategy)
                .where(new SimpleCondition<HeartBeatAndRecord>() {
                    @Override
                    public boolean filter(HeartBeatAndRecord value) throws Exception {

                        if (value.getType() != HeartBeatAndRecord.RECORD) {
                            return false;
                        }
                        if (value.getRecord().getFLOWTYPE() == 3 || value.getRecord()
                                .getFLOWTYPE() == 2
                                && (value.getRecord().getORIGINALFLAG() == 2 || value
                                        .getRecord().getPROVINCEBOUND() == 2)) {
                            return false;
                        }
                        return true;
                    }
                })
                .next("fivetimeoutevents")
                .where(new SimpleCondition<HeartBeatAndRecord>() {
                    @Override
                    public boolean filter(HeartBeatAndRecord value) throws Exception {

                        if (value.getType() == HeartBeatAndRecord.HEARTBEAT) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .timesOrMore(5).consecutive();//连续出现5次
        PatternStream<HeartBeatAndRecord> patternStream = CEP.pattern(timeoutEventKeyby, pattern);
        SingleOutputStreamOperator<TimeoutEvent> alerts = patternStream
                .process(new CEPGantryTimerPatternProcess());
        alerts.addSink(RabbitMQDataSink.generateRMQSink("CEPAbnormalVehiclehb"))
                .name(String.format("RMQ:%s", "CEPAbnormalVehicle"));
    }
    
}
