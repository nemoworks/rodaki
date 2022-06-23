package com.nju.ics.streamjobslocal;

import java.text.SimpleDateFormat;
import java.time.Duration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nju.ics.abnormalsituation.FrequentLeast;
import com.nju.ics.abnormalsituation.OverlapPassid;
import com.nju.ics.abnormalsituation.TimeoutSituation;
import com.nju.ics.funcs.FixTimerRecord;
import com.nju.ics.funcs.Row2JSONObject;
import com.nju.ics.models.TimerRecord;
import com.nju.ics.utils.ConfigureENV;
import com.nju.ics.utils.DataSourceJudge;

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
 * 将所有的异常情况监测放在一起，目前包括以下几类：
 * 1. 超时情况（更接近有进无出）
 * 2. 同时在途
 * 3. 频繁兜底
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
        String csvfile = "/home/mj/data/1101/1101_sort_new_head1000000.csv";
        // 使用 RowCsvInputFormat 把每一行记录解析为一个 Row
        RowCsvInputFormat csvRecordInput = new RowCsvInputFormat(
                new Path(csvfile), // 文件路径
                new TypeInformation[] { Types.STRING, Types.STRING, Types.STRING, Types.STRING,
                        Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING,
                        Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING }, // 字段类型
                "\n", // 行分隔符
                ",",
                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 },
                false); // 字段分隔符
        csvRecordInput.setSkipFirstLineAsHeader(true);
        csvRecordInput.setLenient(true);
        // 从文件中创建初始数据流rawRecord
        DataStream<JSONObject> rawRecord = env.readFile(csvRecordInput, csvfile)
                .map(new Row2JSONObject(
                        new String[] { "FLOWTYPE", "LANESPINFO", "MEDIATYPE", "ORIGINALFLAG",
                                "PASSID", "PROVINCEBOUND", "SPECIALTYPE", "STATIONID",
                                "TIME", "TIMESTRING",
                                "TRANSCODE", "VEHICLETYPE", "VLP", "VLPC", "ACTUALFEECLASS"
                        }))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(
                        Duration.ofSeconds(10)).withIdleness(Duration.ofMinutes(1))
                        .withTimestampAssigner(
                                new TimestampAssigners.JSONObjectTimestampAssigner()));

        // DataStream<JSONObject> rawRecord = env.fromElements(
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
        DataStream<TimerRecord> recordSimple = rawRecord
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
        recordSimple = recordSimple
                .filter(new FilterFunction<TimerRecord>() {
                    // 对数据做预处理，过滤不考虑的数据
                    @Override
                    public boolean filter(TimerRecord value) throws Exception {
                        // 过滤非货车数据
                        if (!(value.getVEHICLETYPE() >= 11 && value.getVEHICLETYPE() <= 16)) {
                            return false;
                        }
                        // 过滤两类特情数据
                        if (value.getPROVINCEBOUND() == 1
                                && (value.getSPECIALTYPE().contains("154") || value
                                        .getSPECIALTYPE().contains("186"))) {
                            return false;
                        }
                        // 过滤passid=000000的异常数据
                        if (value.getPASSID().startsWith("000000")) {
                            return false;
                        }
                        return true;
                    }

                });
        DataStream<TimerRecord> recordFixed = recordSimple.keyBy(x -> "default")
                .process(new FixTimerRecord());
        // 通用过程结束，最后一步为修复记录
        // 1.超时情况
        TimeoutSituation.generateStream(recordFixed);
        // 2.同时在途
        OverlapPassid.generateStream(recordFixed);
        // 3.频繁兜底
        FrequentLeast.generateStream(recordFixed);
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
