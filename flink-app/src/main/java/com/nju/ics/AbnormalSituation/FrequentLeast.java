package com.nju.ics.AbnormalSituation;

import com.nju.ics.Connectors.RabbitMQDataSink;
import com.nju.ics.Models.FrequentLeastFeeEvent;
import com.nju.ics.Models.TimerRecord;
import com.nju.ics.Utils.UniversalDataStreamOps;

import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.datastream.DataStream;

/**
 * 频繁兜底
 */
public class FrequentLeast {
    public static void generateStream(DataStream<TimerRecord> stationRecordFixed) {
        //使用
        DataStream<FrequentLeastFeeEvent> alerts = new UniversalDataStreamOps.ObserveFieldFrequentBuilder<TimerRecord, FrequentLeastFeeEvent>(
                stationRecordFixed)
                .filter((record) -> {
                    return record.getACTUALFEECLASS() == 6;
                })
                .outputType(FrequentLeastFeeEvent.class)
                .frequent(1)
                .maxInterval(Time.days(1).toMilliseconds())
                .keyby(x -> x.getVEHICLEID())
                .matchProcess((match) -> {
                    return FrequentLeastFeeEvent.build(match.get(0).getVEHICLEID(), match.get(0).getTIME(),
                            match.get(0).getTIME());
                })
                .build();
        alerts.addSink(RabbitMQDataSink.generateRMQSink("CEPFrequentLeast"))
                .name(String.format("RMQ:%s", "CEPFrequentLeast"));
    }
}
