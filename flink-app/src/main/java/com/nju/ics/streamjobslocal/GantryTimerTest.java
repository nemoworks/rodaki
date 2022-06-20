package com.nju.ics.streamjobslocal;

import java.text.SimpleDateFormat;
import java.time.Duration;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.io.RowCsvInputFormat;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nju.ics.connectors.RabbitMQDataSink;
import com.nju.ics.funcs.GantryTimer;
import com.nju.ics.funcs.Row2JSONObject;
import com.nju.ics.models.TimerRecord;
import com.nju.ics.rawtype.AbnormalVehicle;
import com.nju.ics.utils.ConfigureENV;
import com.nju.ics.utils.DataSourceJudge;

public class GantryTimerTest {
        public static void main(String[] args) throws Exception {
                // set up the streaming execution environment
                final ParameterTool params = ParameterTool.fromArgs(args);

                Configuration conf = new Configuration();
                ConfigureENV.initConfiguration("/applicationdebug.properties");
                conf.setInteger("rest.port", 9000);
                StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
                ConfigureENV.configureEnvironment(params, env);
                // 输入文件路径
                String gantrycsv = "/home/mj/data/1101/1101_sort.csv";
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
                                .assignTimestampsAndWatermarks(WatermarkStrategy.<Row>forBoundedOutOfOrderness(
                                                Duration.ofSeconds(60)).withIdleness(Duration.ofMinutes(1))
                                                .withTimestampAssigner(
                                                                new GantryTimerTest.timestampAssigner()))
                                .map(new Row2JSONObject(
                                                new String[] { "FLOWTYPE", "LANESPINFO", "MEDIATYPE", "ORIGINALFLAG",
                                                                "PASSID", "PROVINCEBOUND", "SPECIALTYPE", "STATIONID",
                                                                "TIME", "TIMESTRING",
                                                                "TRANSCODE", "VEHICLETYPE", "VLP", "VLPC"
                                                }));
                // "FLOWTYPE", "TIME", "STATIONID", "VLP", "VLPC",
                // "VEHICLETYPE", "PASSID", "TIMESTRING", "ORIGINALFLAG",
                // "PROVINCEBOUND", "MEDIATYPE", "SPECIALTYPE", "TRANSCODE", "LANESPINFO"
                DataStream<TimerRecord> gantryRecordSimple = rawGantry
                                .process(new ProcessFunction<JSONObject, TimerRecord>() {

                                        @Override
                                        public void processElement(JSONObject value,
                                                        ProcessFunction<JSONObject, TimerRecord>.Context ctx,
                                                        Collector<TimerRecord> out) throws Exception {
                                                // TODO Auto-generated method stub
                                                value.put(DataSourceJudge.timeKey, ctx.timestamp());
                                                // Thread.sleep(1); // 延时1毫秒
                                                out.collect(JSON.toJavaObject(value, TimerRecord.class));
                                        }

                                });
                DataStream<AbnormalVehicle> abnormalVehicle = gantryRecordSimple.keyBy(x -> "default")
                                .process(new GantryTimer("/hdd/users/lzm/zc/simulate/gantrytime_site.json"));

                abnormalVehicle.addSink(RabbitMQDataSink.generateRMQSink("AbnormalVehicle"))
                                .name(String.format("RMQ:%s", "AbnormalVehicle"));
                // DataStream<MultiPassIdVehicle> multiPassIdVehicle =
                // gantryRecordSimple.keyBy(x -> x.getVEHICLEID())
                // .process(new MultiPassid());
                // multiPassIdVehicle.addSink(RabbitMQDataSink.generateRMQSink("MultiPassIdVehicle"))
                // .name(String.format("RMQ:%s", "MultiPassIdVehicle"));
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
                                // System.out.println(element.getField(6));
                                // System.out.println(timestamp);
                                // System.exit(0);
                        } catch (Exception e) {
                                // System.out.println(gantryCharge);
                        }

                        return timestamp;

                }

        }

}
