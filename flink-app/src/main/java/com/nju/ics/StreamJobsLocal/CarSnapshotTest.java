package com.nju.ics.StreamJobsLocal;

import java.text.SimpleDateFormat;
import java.time.Duration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.datastax.driver.mapping.Mapper;
import com.nju.ics.Connectors.HadoopFS;
import com.nju.ics.Connectors.RabbitMQDataSink;
import com.nju.ics.Funcs.CarSnapshotProcess;
import com.nju.ics.Funcs.Row2JSONObject;
import com.nju.ics.Models.TimerRecord;
import com.nju.ics.Snapshots.CarSnapshot;
import com.nju.ics.Utils.ConfigureENV;
import com.nju.ics.Utils.DataSourceJudge;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.io.RowCsvInputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.apache.hadoop.io.Text;

public class CarSnapshotTest {
        public static void main(String[] args) throws Exception {
                // set up the streaming execution environment
                final ParameterTool params = ParameterTool.fromArgs(args);

                Configuration conf = new Configuration();
                ConfigureENV.initConfiguration("/applicationdebug.properties");
                conf.setInteger("rest.port", 9000);
                StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
                env.setRuntimeMode(RuntimeExecutionMode.BATCH);
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
                                .assignTimestampsAndWatermarks(WatermarkStrategy.<Row>forBoundedOutOfOrderness(
                                                Duration.ofSeconds(60)).withIdleness(Duration.ofMinutes(1))
                                                .withTimestampAssigner(
                                                                new CarSnapshotTest.timestampAssigner()))
                                .map(new Row2JSONObject(
                                                new String[] { "FLOWTYPE", "LANESPINFO", "MEDIATYPE", "ORIGINALFLAG",
                                                                "PASSID", "PROVINCEBOUND", "SPECIALTYPE", "STATIONID",
                                                                "TIME", "TIMESTRING",
                                                                "TRANSCODE", "VEHICLETYPE", "VLP", "VLPC"
                                                }));
                DataStream<TimerRecord> stationRecordSimple = rawGantry
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
                DataStream<CarSnapshot> carSnapshot = stationRecordSimple.keyBy(x -> x.getVEHICLEID())
                                .process(new CarSnapshotProcess());
                // carSnapshot.addSink(RabbitMQDataSink.generateRMQSink("carSnapshot"))
                // .name(String.format("RMQ:%s", "carSnapshot"));
                DataStream<Tuple2<Text, Text>> hadoopResult = carSnapshot
                                .map(new MapFunction<CarSnapshot, Tuple2<Text, Text>>() {

                                        @Override
                                        public Tuple2<Text, Text> map(CarSnapshot value) throws Exception {
                                                // TODO Auto-generated method stub
                                                Tuple2<Text, Text> x = new Tuple2<Text, Text>();
                                                x.f0 = new Text(value.id());
                                                x.f1 = new Text(JSON.toJSONString(value));

                                                return x;
                                        }

                                });
                hadoopResult.sinkTo(HadoopFS.generateHadoopSink("/hdd/data/hadooptest"));
                // CassandraSink.addSink(carSnapshot)
                // .setHost("127.0.0.1", 17000)
                // .setMapperOptions(() -> new Mapper.Option[] {
                // Mapper.Option.saveNullFields(true) })
                // .build();
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
}
