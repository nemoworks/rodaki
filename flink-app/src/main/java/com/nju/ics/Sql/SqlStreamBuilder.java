package com.nju.ics.Sql;

import java.text.SimpleDateFormat;

import org.apache.flink.configuration.Configuration;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.Configs.StationPosition;
import com.nju.ics.Connectors.FixedRowCSV;
import com.nju.ics.Fields.TrafficTransactionPASSEDSITES;
import com.nju.ics.Funcs.RawDatastreamPartitionProcess;
import com.nju.ics.Funcs.Row2JSONObject;
import com.nju.ics.Models.ENStationRecord;
import com.nju.ics.Models.ENVehicleRecord;
import com.nju.ics.Models.ExitInvoiceRecord;
import com.nju.ics.Models.ExitPaymentRecord;
import com.nju.ics.Models.ExitStationRecord;
import com.nju.ics.Models.ExitVehicleInfo;
import com.nju.ics.Models.GantryInfo;
import com.nju.ics.Models.GantryRecord;
import com.nju.ics.Models.GantryVehicleRecord;
import com.nju.ics.Operators.DataSourceFilterFunc;
import com.nju.ics.Utils.ConfigureENV;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.GetColInfo;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.io.RowCsvInputFormat;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.Path;
import org.apache.flink.shaded.jackson2.org.yaml.snakeyaml.events.StreamStartEvent;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.OutputTag;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableDescriptor;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamStatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.types.UnresolvedDataType;

import static org.apache.flink.table.api.Expressions.*;

public class SqlStreamBuilder {
        public static void generateDataStream(StreamExecutionEnvironment env, ParameterTool params) {
                StreamTableEnvironment tenv = StreamTableEnvironment.create(env);

                // 输入文件路径
                String gantrycsv = "/hdd/data/1101/gantrywaste_fix.csv";
                String entrycsv = "/hdd/data/1101/enwaste.csv";
                String exitcsv = "/hdd/data/1101/exitwaste.csv";
                // 使用 RowCsvInputFormat 把每一行记录解析为一个 Row
                FixedRowCSV csvGantryInput = new FixedRowCSV(
                                (Path) new Path(gantrycsv), // 文件路径
                                GetColInfo.getTypeInfo("/gantrywaste.json"), // 字段类型
                                "\n", // 行分隔符
                                "€",
                                GetColInfo.getIdx("/gantrywaste.json"),
                                false); // 字段分隔符
                // System.out.println(GetColInfo.getTypeInfo("/gantrywaste.json").length);
                // System.out.println(Arrays.toString(GetColInfo.getIdx("/gantrywaste.json")));

                FixedRowCSV csvExitInput = new FixedRowCSV(
                                (Path) new Path(exitcsv), // 文件路径
                                GetColInfo.getTypeInfo("/exitwaste.json"), // 字段类型
                                "\n", // 行分隔符
                                "€",
                                GetColInfo.getIdx("/exitwaste.json"),
                                false); // 字段分隔符
                // System.out.println(GetColInfo.getTypeInfo("/exitwaste.json").length);
                // System.out.println(GetColInfo.getIdx("/exitwaste.json").length);

                FixedRowCSV csvEntryInput = new FixedRowCSV(
                                new Path(entrycsv), // 文件路径
                                GetColInfo.getTypeInfo("/enwaste.json"), // 字段类型
                                "\n", // 行分隔符
                                "€",
                                GetColInfo.getIdx("/enwaste.json"),
                                false); // 字段分隔符
                // System.out.println(GetColInfo.getTypeInfo("/enwaste.json").length);
                // System.out.println(GetColInfo.getIdx("/enwaste.json").length);

                csvGantryInput.setSkipFirstLineAsHeader(true);
                csvGantryInput.setLenient(true);
                csvExitInput.setSkipFirstLineAsHeader(true);
                csvExitInput.setLenient(true);
                csvEntryInput.setSkipFirstLineAsHeader(true);
                csvEntryInput.getCharset();
                csvEntryInput.setLenient(true);
                DataStream<JSONObject> rawGantry = env.readFile(csvGantryInput, gantrycsv)
                                .map(new Row2JSONObject(GetColInfo.getColNames("/gantrywaste.json")));
                DataStream<JSONObject> rawEntry = env.readFile(csvEntryInput, entrycsv)
                                .map(new Row2JSONObject(GetColInfo.getColNames("/enwaste.json")));
                DataStream<JSONObject> rawExit = env.readFile(csvExitInput, exitcsv)
                                .map(new Row2JSONObject(GetColInfo.getColNames("/exitwaste.json")));
                DataStream<JSONObject> stream = rawGantry.union(rawEntry, rawExit);
                // DataStream<JSONObject> stream = env.addSource(dataConsumer);
                if (!params.has(ConfigureENV.EVENTTIMEOPTION)) {
                        stream = stream.map(new MapFunction<JSONObject, JSONObject>() {
                                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                @Override
                                public JSONObject map(JSONObject value) throws Exception {
                                        // TODO Auto-generated method stub
                                        DataSourceJudge.typeDetectAndTime(value, time);
                                        return value;
                                }

                        });
                }
                DataStream<JSONObject> FilterdVehicleRecordsStream = stream.filter(new DataSourceFilterFunc());
                SingleOutputStreamOperator<JSONObject> StreamPartition = FilterdVehicleRecordsStream
                                .process(new RawDatastreamPartitionProcess());
                OutputTag<JSONObject> EntryStreamTag = new OutputTag<JSONObject>("EntryStream") {
                };
                OutputTag<JSONObject> GantryStreamTag = new OutputTag<JSONObject>("GantryStream") {
                };
                OutputTag<JSONObject> ExitStreamTag = new OutputTag<JSONObject>("ExitStream") {
                };
                DataStream<JSONObject> EntryStream = StreamPartition.getSideOutput(EntryStreamTag);
                DataStream<JSONObject> GantryStream = StreamPartition.getSideOutput(GantryStreamTag);
                DataStream<JSONObject> ExitStream = StreamPartition.getSideOutput(ExitStreamTag);
                // 第一层模型 ENStationRecord
                SingleOutputStreamOperator<ENStationRecord> ENStationRecordStream = EntryStream
                                .map(new RichMapFunction<JSONObject, ENStationRecord>() {
                                        @Override
                                        public void open(Configuration parameters) throws Exception {
                                                // TODO Auto-generated method stub
                                                super.open(parameters);
                                                StationPosition.initStationPosition();
                                        }

                                        @Override
                                        public ENStationRecord map(JSONObject value) throws Exception {
                                                return JSONObject.toJavaObject(value, ENStationRecord.class);
                                        }
                                });
                // 第一层模型 ENVehicleRecord
                SingleOutputStreamOperator<ENVehicleRecord> ENVehicleRecordStream = EntryStream
                                .map(new RichMapFunction<JSONObject, ENVehicleRecord>() {
                                        @Override
                                        public void open(Configuration parameters) throws Exception {
                                                // TODO Auto-generated method stub
                                                super.open(parameters);

                                        }

                                        @Override
                                        public ENVehicleRecord map(JSONObject value) throws Exception {
                                                return JSONObject.toJavaObject(value, ENVehicleRecord.class);
                                        }
                                });
                // 第一层模型 GantryRecord
                SingleOutputStreamOperator<GantryRecord> GantryRecordStream = GantryStream
                                .map(new RichMapFunction<JSONObject, GantryRecord>() {
                                        @Override
                                        public void open(Configuration parameters) throws Exception {
                                                // TODO Auto-generated method stub
                                                super.open(parameters);
                                                GantryPosition.initGantryPosition();// 初始化门架经纬度

                                        }

                                        @Override
                                        public GantryRecord map(JSONObject value) throws Exception {
                                                return JSONObject.toJavaObject(value, GantryRecord.class);
                                        }
                                });
                // 第一层模型 GantryInfo
                SingleOutputStreamOperator<GantryInfo> GantryInfoStream = GantryStream
                                .map(new RichMapFunction<JSONObject, GantryInfo>() {
                                        @Override
                                        public void open(Configuration parameters) throws Exception {
                                                // TODO Auto-generated method stub
                                                super.open(parameters);
                                                GantryPosition.initGantryPosition();// 初始化门架经纬度

                                        }

                                        @Override
                                        public GantryInfo map(JSONObject value) throws Exception {
                                                return JSONObject.toJavaObject(value, GantryInfo.class);
                                        }
                                });
                // 第一层模型 GantryVehicleRecord
                SingleOutputStreamOperator<GantryVehicleRecord> GantryVehicleRecordStream = GantryStream
                                .map(new RichMapFunction<JSONObject, GantryVehicleRecord>() {
                                        @Override
                                        public void open(Configuration parameters) throws Exception {
                                                // TODO Auto-generated method stub
                                                super.open(parameters);

                                        }

                                        @Override
                                        public GantryVehicleRecord map(JSONObject value) throws Exception {
                                                return JSONObject.toJavaObject(value, GantryVehicleRecord.class);
                                        }
                                });
                // 第一层模型 ExitStationRecord
                SingleOutputStreamOperator<ExitStationRecord> ExitStationRecordStream = ExitStream
                                .map(new RichMapFunction<JSONObject, ExitStationRecord>() {
                                        @Override
                                        public void open(Configuration parameters) throws Exception {
                                                // TODO Auto-generated method stub
                                                super.open(parameters);
                                                StationPosition.initStationPosition();
                                        }

                                        @Override
                                        public ExitStationRecord map(JSONObject value) throws Exception {
                                                return JSONObject.toJavaObject(value, ExitStationRecord.class);
                                        }
                                });
                // 第一层模型 ExitPaymentRecord
                SingleOutputStreamOperator<ExitPaymentRecord> ExitPaymentRecordStream = ExitStream
                                .map(new RichMapFunction<JSONObject, ExitPaymentRecord>() {
                                        @Override
                                        public void open(Configuration parameters) throws Exception {
                                                // TODO Auto-generated method stub
                                                super.open(parameters);

                                        }

                                        @Override
                                        public ExitPaymentRecord map(JSONObject value) throws Exception {
                                                return JSONObject.toJavaObject(value, ExitPaymentRecord.class);
                                        }
                                });
                // 第一层模型 ExitInvoiceRecord
                SingleOutputStreamOperator<ExitInvoiceRecord> ExitInvoiceRecordStream = ExitStream
                                .map(new RichMapFunction<JSONObject, ExitInvoiceRecord>() {
                                        @Override
                                        public void open(Configuration parameters) throws Exception {
                                                // TODO Auto-generated method stub
                                                super.open(parameters);

                                        }

                                        @Override
                                        public ExitInvoiceRecord map(JSONObject value) throws Exception {
                                                return JSONObject.toJavaObject(value, ExitInvoiceRecord.class);
                                        }
                                });
                // 第一层模型 ExitVehicleInfo
                SingleOutputStreamOperator<ExitVehicleInfo> ExitVehicleInfoStream = ExitStream
                                .map(new RichMapFunction<JSONObject, ExitVehicleInfo>() {
                                        @Override
                                        public void open(Configuration parameters) throws Exception {
                                                // TODO Auto-generated method stub
                                                super.open(parameters);

                                        }

                                        @Override
                                        public ExitVehicleInfo map(JSONObject value) throws Exception {
                                                return JSONObject.toJavaObject(value, ExitVehicleInfo.class);
                                        }
                                }).name("ExitVehicleInfo");
                // Schema.newBuilder().
                // columnByMetadata("proctime", "TIMESTAMP_LTZ(3)").
                // watermark("proctime","SOURCE_WATERMARK()");
                String ENStationRecordT = tenv.fromDataStream(ENStationRecordStream).toString();
                String ENVehicleRecordT = tenv.fromDataStream(ENVehicleRecordStream).toString();
                String GantryRecordT = tenv.fromDataStream(GantryRecordStream).toString();
                String GantryInfoT = tenv.fromDataStream(GantryInfoStream).toString();
                String GantryVehicleRecordT = tenv.fromDataStream(GantryVehicleRecordStream).toString();
                String ExitStationRecordT = tenv.fromDataStream(ExitStationRecordStream).toString();
                String ExitPaymentRecordT = tenv.fromDataStream(ExitPaymentRecordStream).toString();
                String ExitInvoiceRecordT = tenv.fromDataStream(ExitInvoiceRecordStream).toString();
                String ExitVehicleInfoT = tenv.fromDataStream(ExitVehicleInfoStream).toString();
               
                String PASSEDSITES = DataTypes.of(TrafficTransactionPASSEDSITES.class).toString();
                System.out.println(PASSEDSITES);
                tenv.executeSql("CREATE TABLE TrafficTransaction"
                                + "("
                                + "PASSID STRING,"
                                + "VEHICLEID STRING,"
                                + "MEDIATYPE INT,"
                                + "MEDIAID INT,"
                                + "PAYID STRING,"
                                + "ENIDENTIFY STRING,"
                                + "ENWEIGHT INT,"
                                + "EXIDENTIFY STRING,"
                                + "EXWEIGHT INT,"
                                + String.format("PASSEDSITES ARRAY<ROW<PASSID STRING,SITEID STRING,TIME2 INT>>,", PASSEDSITES)
                                + "SPECIALTYPE STRING"
                                + ")"
                                + "WITH ("
                                + "'connector' = 'print'"
                                // + "'topic' = 'TrafficTransactionTable',"
                                // + "'properties.bootstrap.servers' = 'localhost:9092',"
                                // + "'format' = 'json'"
                                + ")"

                );
                StreamStatementSet statement = tenv.createStatementSet();
                statement.addInsertSql(
                                "INSERT INTO TrafficTransaction(PASSID,ENWEIGHT) "
                                                + "SELECT t1.PASSID,t1.ENWEIGHT FROM "
                                                + ENStationRecordT + " t1 "
                                                + " INNER JOIN "
                                                + GantryRecordT + " t2"
                                                + " ON " + " t1.PASSID=t2.PASSID "

                                                + "");
                //statement.addInsert("TrafficTransaction", TrafficTransactionT);
                statement.execute();

        }
}
