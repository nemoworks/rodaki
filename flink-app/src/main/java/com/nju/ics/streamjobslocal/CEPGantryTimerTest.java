package com.nju.ics.streamjobslocal;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.datastax.driver.mapping.Mapper;
import com.nju.ics.connectors.HadoopFS;
import com.nju.ics.connectors.RabbitMQDataSink;
import com.nju.ics.funcs.CEPGantryTimerPatternProcess;
import com.nju.ics.funcs.CEPVehiclePreProcess;
import com.nju.ics.funcs.CarSnapshotProcess;
import com.nju.ics.funcs.FixTimerRecord;
import com.nju.ics.funcs.GenerateHeartBeatProcess;
import com.nju.ics.funcs.Row2JSONObject;
import com.nju.ics.models.HeartBeatAndRecord;
import com.nju.ics.models.TimeoutEvent;
import com.nju.ics.models.TimerRecord;
import com.nju.ics.rawtype.AbnormalVehicle;
import com.nju.ics.snapshots.CarSnapshot;
import com.nju.ics.utils.ConfigureENV;
import com.nju.ics.utils.DataSourceJudge;
import com.nju.ics.watermark.HeartBeatAndRecordWatermark;
import com.nju.ics.watermark.JSONObjectWatermark;
import com.nju.ics.watermark.TimeoutEventWatermark;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.RowCsvInputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.hadoop.io.Text;

/**
 * 使用CEP来进行简单的超时验证
 */
public class CEPGantryTimerTest {
        public static void main(String[] args) throws Exception {
                // set up the streaming execution environment
                final ParameterTool params = ParameterTool.fromArgs(args);

                Configuration conf = new Configuration();
                ConfigureENV.initConfiguration("/applicationdebug.properties");
                conf.setInteger("rest.port", 9000);
                StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
                env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
                ConfigureENV.configureEnvironment(params, env);
                // 输入文件路径
                String gantrycsv = "/hdd/data/1101/1101_sort.csv";
                // 使用 RowCsvInputFormat 把每一行记录解析为一个 Row
                RowCsvInputFormat csvGantryInput = new RowCsvInputFormat(
                                new Path(gantrycsv), // 文件路径
                                new TypeInformation[] { Types.STRING, Types.STRING, Types.STRING, Types.STRING,
                                                Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING,
                                                Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING }, // 字段类型
                                "\n", // 行分隔符
                                ",",
                                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 },
                                false); // 字段分隔符
                csvGantryInput.setSkipFirstLineAsHeader(true);
                csvGantryInput.setLenient(true);
                DataStream<JSONObject> rawGantry = env.readFile(csvGantryInput, gantrycsv)
                                .map(new Row2JSONObject(
                                                new String[] { "FLOWTYPE", "LANESPINFO", "MEDIATYPE", "ORIGINALFLAG",
                                                                "PASSID", "PROVINCEBOUND", "SPECIALTYPE", "STATIONID",
                                                                "TIME", "TIMESTRING",
                                                                "TRANSCODE", "VEHICLETYPE", "VLP", "VLPC"
                                                }))
                                .assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(
                                                Duration.ofSeconds(10)).withIdleness(Duration.ofMinutes(1))
                                                .withTimestampAssigner(
                                                                new CEPGantryTimerTest.JSONObjectTimestampAssigner()));

                // DataStream<JSONObject> rawGantry = env.fromElements(
                // JSON.parseObject(
                // "{\"TRANSCODE\":\"\",\"VLPC\":\"1\",\"FLOWTYPE\":\"2\",\"MEDIATYPE\":\"1\",\"STATIONID\":\"G003537001001610150\",\"TIME\":\"1635696000000\",\"ORIGINALFLAG\":\"1\",\"PROVINCEBOUND\":\"0\",\"SPECIALTYPE\":\"\",\"PASSID\":\"014101202423180300098320211031204024\",\"TIMESTRING\":\"2021-11-01
                // 00:00:00\",\"LANESPINFO\":\"\",\"VLP\":\"豫RVR363\",\"VEHICLETYPE\":\"13\"}"),
                // JSON.parseObject(
                // "{\"TRANSCODE\":\"\",\"VLPC\":\"1\",\"FLOWTYPE\":\"2\",\"MEDIATYPE\":\"1\",\"STATIONID\":\"G003537001001610150\",\"TIME\":\"1635706800000\",\"ORIGINALFLAG\":\"1\",\"PROVINCEBOUND\":\"0\",\"SPECIALTYPE\":\"\",\"PASSID\":\"014101202423180300098320211031204024\",\"TIMESTRING\":\"2021-11-01
                // 03:00:00\",\"LANESPINFO\":\"\",\"VLP\":\"豫RVR363\",\"VEHICLETYPE\":\"13\"}"),
                // JSON.parseObject(
                // "{\"TRANSCODE\":\"\",\"VLPC\":\"1\",\"FLOWTYPE\":\"2\",\"MEDIATYPE\":\"1\",\"STATIONID\":\"G003537001001610150\",\"TIME\":\"1635714000000\",\"ORIGINALFLAG\":\"1\",\"PROVINCEBOUND\":\"0\",\"SPECIALTYPE\":\"\",\"PASSID\":\"014101202423180300098320211031204024\",\"TIMESTRING\":\"2021-11-01
                // 05:00:00\",\"LANESPINFO\":\"\",\"VLP\":\"豫RVR363\",\"VEHICLETYPE\":\"13\"}"),
                // JSON.parseObject(
                // "{\"TRANSCODE\":\"\",\"VLPC\":\"1\",\"FLOWTYPE\":\"2\",\"MEDIATYPE\":\"1\",\"STATIONID\":\"G003537001001610150\",\"TIME\":\"1635728400000\",\"ORIGINALFLAG\":\"1\",\"PROVINCEBOUND\":\"0\",\"SPECIALTYPE\":\"\",\"PASSID\":\"014101202423180300098320211031204024\",\"TIMESTRING\":\"2021-11-01
                // 09:00:00\",\"LANESPINFO\":\"\",\"VLP\":\"豫RVR363\",\"VEHICLETYPE\":\"13\"}"))
                // .assignTimestampsAndWatermarks(new JSONObjectWatermark());
                //
                DataStream<TimerRecord> stationRecordSimple = rawGantry
                                .process(new ProcessFunction<JSONObject, TimerRecord>() {

                                        @Override
                                        public void processElement(JSONObject value,
                                                        ProcessFunction<JSONObject, TimerRecord>.Context ctx,
                                                        Collector<TimerRecord> out) throws Exception {
                                                // TODO Auto-generated method stub
                                                value.put(DataSourceJudge.timeKey, ctx.timestamp());
                                                out.collect(JSON.toJavaObject(value, TimerRecord.class));
                                        }

                                });
                stationRecordSimple = stationRecordSimple
                                .filter(new FilterFunction<TimerRecord>() {

                                        @Override
                                        public boolean filter(TimerRecord value) throws Exception {
                                                // TODO Auto-generated method stub
                                                if (!(value.getVEHICLETYPE() >= 11 && value.getVEHICLETYPE() <= 16)) {
                                                        return false;
                                                }
                                                if (value.getPROVINCEBOUND() == 1
                                                                && (value.getSPECIALTYPE().contains("154") || value
                                                                                .getSPECIALTYPE().contains("186"))) {
                                                        return false;
                                                }
                                                if (value.getPASSID().startsWith("000000")) {
                                                        return false;
                                                }
                                                return true;
                                        }

                                });
                DataStream<TimerRecord> stationRecordFixed = stationRecordSimple.keyBy(x -> "default")
                                .process(new FixTimerRecord());
                DataStream<HeartBeatAndRecord> timeoutEvent = stationRecordFixed.keyBy(x -> x.getVEHICLEID())
                                .process(new GenerateHeartBeatProcess());
                // 重新设置元素的时间戳
                timeoutEvent = timeoutEvent.assignTimestampsAndWatermarks(WatermarkStrategy
                                .<HeartBeatAndRecord>forBoundedOutOfOrderness(
                                                Duration.ofMinutes(10))
                                .withTimestampAssigner(
                                                new CEPGantryTimerTest.HeartBeatAndRecordTimestampAssigner()))
                                .setParallelism(1);

                DataStream<HeartBeatAndRecord> timeoutEventKeyby = timeoutEvent
                                .keyBy(new KeySelector<HeartBeatAndRecord, String>() {
                                        @Override
                                        public String getKey(HeartBeatAndRecord value) throws Exception {
                                                // System.out.println(value.getKey());
                                                return value.getKey();
                                        }
                                });
                // timeoutEvent.map(new MapFunction<HeartBeatAndRecord, String>() {

                // @Override
                // public String map(HeartBeatAndRecord value) throws Exception {
                // // TODO Auto-generated method stub
                // if (value.getKey().contains("鲁PC6Q78")) {
                // System.out.printf("check:%d %d\n", value.getTimestamp(),
                // value.hashCode());
                // }
                // return null;
                // }

                // });
                OutputTag<HeartBeatAndRecord> lateDataOutputTag = new OutputTag<HeartBeatAndRecord>("late-data") {
                };
                AfterMatchSkipStrategy skipStrategy = AfterMatchSkipStrategy.skipPastLastEvent();
                // 必须在检测到三个事件，以通行记录开头，三个heartbeat结束
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
                                                // if (value.getKey().contains("鲁PC6Q78")) {
                                                // System.out.printf("3:%d %d\n", value.getTimestamp(),
                                                // value.hashCode());
                                                // }
                                                return true;
                                        }
                                })
                                .next("threetimeoutevents")
                                .where(new SimpleCondition<HeartBeatAndRecord>() {
                                        @Override
                                        public boolean filter(HeartBeatAndRecord value) throws Exception {

                                                if (value.getType() == HeartBeatAndRecord.HEARTBEAT) {
                                                        // if (value.getKey().contains("鲁PC6Q78")) {
                                                        // System.out.printf("4:%d %d\n", value.getTimestamp(),
                                                        // value.hashCode());
                                                        // }
                                                        return true;
                                                } else {
                                                        return false;
                                                }

                                        }
                                })
                                .timesOrMore(5).consecutive();

                PatternStream<HeartBeatAndRecord> patternStream = CEP.pattern(timeoutEventKeyby, pattern)
                                .sideOutputLateData(lateDataOutputTag);

                SingleOutputStreamOperator<TimeoutEvent> alerts = patternStream
                                .process(new CEPGantryTimerPatternProcess());
                DataStream<HeartBeatAndRecord> lateData = alerts.getSideOutput(lateDataOutputTag);
                lateData.map(new MapFunction<HeartBeatAndRecord, String>() {

                        @Override
                        public String map(HeartBeatAndRecord value) throws Exception {
                                // TODO Auto-generated method stub
                                // if (value.getKey().contains("鲁PC6Q78")) {
                                // System.out.printf("timeout:%d %d\n", value.getTimestamp(),
                                // value.hashCode());
                                // }
                                return null;
                        }

                });
                alerts.addSink(RabbitMQDataSink.generateRMQSink("CEPAbnormalVehiclehb"))
                                .name(String.format("RMQ:%s", "CEPAbnormalVehicle"));
                env.execute();
        }

        static class timestampAssigner implements SerializableTimestampAssigner<Row> {
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public long extractTimestamp(Row element, long recordTimestamp) {
                        // TODO Auto-generated method stub

                        long timestamp = 0;

                        try {

                                timestamp = time.parse(element.getFieldAs(9)).getTime();

                        } catch (Exception e) {
                                // System.out.println(gantryCharge);
                        }

                        return timestamp;

                }

        }

        static class EventTimestampAssigner implements SerializableTimestampAssigner<TimeoutEvent> {

                @Override
                public long extractTimestamp(TimeoutEvent element, long recordTimestamp) {
                        // TODO Auto-generated method stub
                        return element.getTriggertime();
                }

        }

        static class HeartBeatAndRecordTimestampAssigner implements SerializableTimestampAssigner<HeartBeatAndRecord> {

                @Override
                public long extractTimestamp(HeartBeatAndRecord element, long recordTimestamp) {
                        // TODO Auto-generated method stub
                        return element.getTimestamp();
                }

        }

        static class JSONObjectTimestampAssigner implements SerializableTimestampAssigner<JSONObject> {
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public long extractTimestamp(JSONObject element, long recordTimestamp) {
                        // TODO Auto-generated method stub

                        long timestamp = 0;

                        try {

                                timestamp = time.parse(element.getString("TIMESTRING")).getTime();

                        } catch (Exception e) {
                                // System.out.println(gantryCharge);
                        }

                        return timestamp;
                }

        }
}
