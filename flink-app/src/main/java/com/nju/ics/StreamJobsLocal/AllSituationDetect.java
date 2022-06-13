package com.nju.ics.StreamJobsLocal;

import java.text.SimpleDateFormat;
import java.time.Duration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nju.ics.AbnormalSituation.FrequentLeast;
import com.nju.ics.AbnormalSituation.OverlapPassid;
import com.nju.ics.AbnormalSituation.TimeoutSituation;
import com.nju.ics.Funcs.FixTimerRecord;
import com.nju.ics.Funcs.Row2JSONObject;
import com.nju.ics.Models.TimerRecord;
import com.nju.ics.Utils.ConfigureENV;
import com.nju.ics.Utils.DataSourceJudge;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.io.RowCsvInputFormat;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 将所有的异常情况监测放在一起
 */
public class AllSituationDetect {
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
        String gantrycsv = "/hdd/data/1101/1101_sort_new.csv";
        // 使用 RowCsvInputFormat 把每一行记录解析为一个 Row
        RowCsvInputFormat csvGantryInput = new RowCsvInputFormat(
                new Path(gantrycsv), // 文件路径
                new TypeInformation[] { Types.STRING, Types.STRING, Types.STRING, Types.STRING,
                        Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING,
                        Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING }, // 字段类型
                "\n", // 行分隔符
                ",",
                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 },
                false); // 字段分隔符
        csvGantryInput.setSkipFirstLineAsHeader(true);
        csvGantryInput.setLenient(true);
        DataStream<JSONObject> rawGantry = env.readFile(csvGantryInput, gantrycsv)
                .map(new Row2JSONObject(
                        new String[] { "FLOWTYPE", "LANESPINFO", "MEDIATYPE", "ORIGINALFLAG",
                                "PASSID", "PROVINCEBOUND", "SPECIALTYPE", "STATIONID",
                                "TIME", "TIMESTRING",
                                "TRANSCODE", "VEHICLETYPE", "VLP", "VLPC", "ACTUALFEECLASS"
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
        // 通用过程结束，最后一步为修复记录
        // 1.超时情况
        TimeoutSituation.generateStream(stationRecordFixed);
        // 2.同时在途
        OverlapPassid.generateStream(stationRecordFixed);
        // 3.频繁兜底
        FrequentLeast.generateStream(stationRecordFixed);
        env.execute();
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
            }

            return timestamp;
        }

    }
}
