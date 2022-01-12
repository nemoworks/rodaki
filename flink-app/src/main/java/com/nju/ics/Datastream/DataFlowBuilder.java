package com.nju.ics.Datastream;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.reflect.Reflection;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.Configs.StationPosition;
import com.nju.ics.Connectors.KafkaDataConsumer;
import com.nju.ics.Connectors.RabbitMQDataSink;
import com.nju.ics.Funcs.RawDatastreamPartitionProcess;
import com.nju.ics.Funcs.Row2JSONObject;
import com.nju.ics.ModelExtractors.*;
import com.nju.ics.Models.*;

import com.nju.ics.Operators.DataSourceFilterFunc;
import com.nju.ics.Operators.TrafficTransactionStreamGantryTrigger;
import com.nju.ics.Operators.VehicleKeyByOperator;
import com.nju.ics.Operators.VehicleRecordProcess;
import com.nju.ics.Operators.VehicleWindowsEvictor;
import com.nju.ics.Operators.VehicleWindowsProcess;
import com.nju.ics.Operators.VehicleWindowsTrigger;
import com.nju.ics.Utils.ConfigureENV;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.GetColInfo;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.flink.util.PropertiesUtil;
import org.apache.kafka.common.protocol.types.Field.Bool;
import org.apache.kafka.common.utils.Exit;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.RowCsvInputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.commons.beanutils.BeanUtils;

public class DataFlowBuilder {
	public static void generateDataStream(StreamExecutionEnvironment env, ParameterTool params) {

		// Map<String, Tuple3<String,Boolean, Boolean>> extrctors =
		// OutputTagCollection.initCollection();
		// OutputTagCollection.initCollection();
		// 输入文件路径
		String gantrycsv = "/hdd/data/1111/gantrywaste_fix.csv";
		String entrycsv = "/hdd/data/1111/enwaste.csv";
		String exitcsv = "/hdd/data/1111/exitwaste.csv";
		// 使用 RowCsvInputFormat 把每一行记录解析为一个 Row
		RowCsvInputFormat csvGantryInput = new RowCsvInputFormat(
				new Path(gantrycsv), // 文件路径
				GetColInfo.getTypeInfo("/gantrywaste.json"), // 字段类型
				"\n", // 行分隔符
				"€",
				GetColInfo.getIdx("/gantrywaste.json"),
				false); // 字段分隔符
		// System.out.println(GetColInfo.getTypeInfo("/gantrywaste.json").length);
		// System.out.println(Arrays.toString(GetColInfo.getIdx("/gantrywaste.json")));

		RowCsvInputFormat csvExitInput = new RowCsvInputFormat(
				new Path(exitcsv), // 文件路径
				GetColInfo.getTypeInfo("/exitwaste.json"), // 字段类型
				"\n", // 行分隔符
				"€",
				GetColInfo.getIdx("/exitwaste.json"),
				false); // 字段分隔符
		// System.out.println(GetColInfo.getTypeInfo("/exitwaste.json").length);
		// System.out.println(GetColInfo.getIdx("/exitwaste.json").length);

		RowCsvInputFormat csvEntryInput = new RowCsvInputFormat(
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
				});

		ENStationRecordStream.addSink(RabbitMQDataSink.generateRMQSink("ENStationRecord"))//SiteRecord
				.name(String.format("RMQ:%s", "ENStationRecordStream"));
		ENVehicleRecordStream.addSink(RabbitMQDataSink.generateRMQSink("ENVehicleRecord"))
				.name(String.format("RMQ:%s", "ENVehicleRecordStream"));
		GantryRecordStream.addSink(RabbitMQDataSink.generateRMQSink("GantryRecord"))
				.name(String.format("RMQ:%s", "GantryRecordStream"));
		// GantryRecordStream.map( new MapFunction<GantryRecord,String>() {

		// @Override
		// public String map(GantryRecord value) throws Exception {
		// // TODO Auto-generated method stub
		// System.out.println(value.getSPECIALTYPE());
		// return null;
		// }

		// });
		GantryInfoStream.addSink(RabbitMQDataSink.generateRMQSink("GantryInfo"))
				.name(String.format("RMQ:%s", "GantryInfoStream"));
		GantryVehicleRecordStream.addSink(RabbitMQDataSink.generateRMQSink("GantryVehicleRecord"))
				.name(String.format("RMQ:%s", "GantryVehicleRecord"));
		ExitStationRecordStream.addSink(RabbitMQDataSink.generateRMQSink("ExitStationRecord"))
				.name(String.format("RMQ:%s", "ExitStationRecordStream"));
		// ExitPaymentRecordStream.addSink(RabbitMQDataSink.generateRMQSink("ExitPaymentRecord"))
		// 		.name(String.format("RMQ:%s", "ExitPaymentRecord"));
		// ExitInvoiceRecordStream.addSink(RabbitMQDataSink.generateRMQSink("ExitInvoiceRecord"))
		// 		.name(String.format("RMQ:%s", "ExitInvoiceRecord"));
		ExitVehicleInfoStream.addSink(RabbitMQDataSink.generateRMQSink("ExitVehicleInfo"))
				.name(String.format("RMQ:%s", "ExitVehicleInfo"));
		
		/**
		 * 第二层模型
		 * 1 <- ENStationRecord:3
		 * 2 <- ENStationRecord:8
		 * 3 <- ENStationRecord:9
		 * 4<- ENStationRecord:10
		 * 5<-ENStationRecord:4、2、13、14、15｜GantryRecord：2、4、8、9、10、11、12｜ExitStationRecord：2、4、13、14、16、17、18
		 * 6 <- ExitStationRecord:11
		 * 7 <-ENStationRecord:12
		 * 8 <- ExitStationRecord:15
		 * 9<- ENStationRecord:11
		 * 10<-ExitStationRecord:12
		 */
		SingleOutputStreamOperator<Map> ENStationRecordOutputStream = ENStationRecordStream
				.map(new MapFunction<ENStationRecord, Map>() {
					/**
					 * 2、3、4、8、9、10、11、12、13、14、15
					 */
					@Override
					public Map map(ENStationRecord value) throws Exception {
						Map<String, Object> result = new HashMap<String, Object>();
						result.put(DataSourceJudge.sourceKey, DataSourceJudge.entryLane);
						result.put("ENTIME", value.getENTIME());
						result.put("PASSID", value.getPASSID());
						result.put("ENTOLLSTATIONID", value.getENTOLLSTATIONID());
						result.put("VEHICLEID", value.getVEHICLEID());
						result.put("MEDIATYPE", value.getMEDIATYPE());
						result.put("MEDIAID", value.getMEDIAID());
						result.put("ENWEIGHT", value.getENWEIGHT());
						result.put("ENIDENTIFY", value.getENIDENTIFY());
						result.put("LONGITUDE", value.getLONGITUDE());
						result.put("LATITUDE", value.getLATITUDE());
						result.put("STATIONNAME", value.getSTATIONNAME());

						result.put("FEEMILEAGE", 0);
						result.put("FEE", 0);
						result.put("SITETYPE", 1);

						return result;
					}
				});
		SingleOutputStreamOperator<TrafficTransaction> TrafficTransactionStreamEN = ENStationRecordOutputStream
				.map(new MapFunction<Map, TrafficTransaction>() {

					@Override
					public TrafficTransaction map(Map value) throws Exception {
						// TODO Auto-generated method stub
						TrafficTransaction entity = new TrafficTransaction();
						entity.setPASSID((String) value.get("PASSID"));
						entity.setVEHICLEID((String) value.get("VEHICLEID"));
						entity.setMEDIAID((String) value.get("MEDIAID"));
						entity.setMEDIATYPE((int) value.get("MEDIATYPE"));
						entity.setENWEIGHT((int) value.get("ENWEIGHT"));
						entity.setENIDENTIFY((String) value.get("ENIDENTIFY"));
						List<Map> tmp = new ArrayList<Map>();
						Map<String, Object> result = new HashMap<String, Object>();
						result.put("PASSID", value.get("PASSID"));
						result.put("SITEID", value.get("ENTOLLSTATIONID"));
						result.put("TIME", value.get("ENTIME"));
						result.put("FEEMILEAGE", value.get("FEEMILEAGE"));
						result.put("FEE", value.get("FEE"));
						result.put("SITETYPE", value.get("SITETYPE"));
						result.put("LONGITUDE", value.get("LONGITUDE"));
						result.put("LATITUDE", value.get("LATITUDE"));
						result.put("NAME", value.get("STATIONNAME"));
						tmp.add(result);
						entity.setPASSEDSITES(tmp);
						return entity;
					}
				});
		SingleOutputStreamOperator<Map> GantryRecordOutputStream = GantryRecordStream
				.map(new MapFunction<GantryRecord, Map>() {
					/**
					 * 2、3、4、8、9、10、11、12
					 */
					@Override
					public Map map(GantryRecord value) throws Exception {
						Map<String, Object> result = new HashMap<String, Object>();
						result.put(DataSourceJudge.sourceKey, DataSourceJudge.gantryCharge);
						result.put("TRANSTIME", value.getTRANSTIME());
						result.put("PASSID", value.getPASSID());
						result.put("GANTRYID", value.getGANTRYID());
						result.put("FEE", value.getFEE());
						result.put("FEEMILEAGE", value.getFEEMILEAGE());
						result.put("LONGITUDE", value.getLONGITUDE());
						result.put("LATITUDE", value.getLATITUDE());
						result.put("GANTRYNAME", value.getGANTRYNAME());
						result.put("VEHICLEID", value.getVEHICLEID());
						result.put("SITETYPE", 2);
						result.put("SPECIALTYPE", value.getSPECIALTYPE());

						return result;
					}
				});
		SingleOutputStreamOperator<TrafficTransaction> TrafficTransactionStreamGantry = GantryRecordOutputStream
				.map(new MapFunction<Map, TrafficTransaction>() {

					@Override
					public TrafficTransaction map(Map value) throws Exception {
						// TODO Auto-generated method stub
						TrafficTransaction entity = new TrafficTransaction();
						entity.setPASSID((String) value.get("PASSID"));
						entity.setVEHICLEID((String) value.get("VEHICLEID"));
						List<Map> tmp = new ArrayList<Map>();
						Map<String, Object> result = new HashMap<String, Object>();
						result.put("PASSID", value.get("PASSID"));
						result.put("SITEID", value.get("GANTRYID"));
						result.put("TIME", value.get("TRANSTIME"));
						result.put("FEEMILEAGE", value.get("FEEMILEAGE"));
						result.put("FEE", value.get("FEE"));
						result.put("SITETYPE", value.get("SITETYPE"));
						result.put("LONGITUDE", value.get("LONGITUDE"));
						result.put("LATITUDE", value.get("LATITUDE"));
						result.put("NAME", value.get("GANTRYNAME"));
						tmp.add(result);
						entity.setPASSEDSITES(tmp);
						entity.setSPECIALTYPE((String) value.get("SPECIALTYPE"));
						return entity;
					}
				});

		SingleOutputStreamOperator<Map> ExitStationRecordOutputStream = ExitStationRecordStream
				.map(new MapFunction<ExitStationRecord, Map>() {
					/**
					 * 2、3、4、11、12、13、14、15、16、17、18
					 */
					@Override
					public Map map(ExitStationRecord value) throws Exception {
						Map<String, Object> result = new HashMap<String, Object>();
						result.put(DataSourceJudge.sourceKey, DataSourceJudge.exitLane);
						result.put("EXTIME", value.getEXTIME());
						result.put("PASSID", value.getPASSID());
						result.put("EXTOLLSTATIONID", value.getEXTOLLSTATIONID());
						result.put("PAYID", value.getPAYID());
						result.put("EXWEIGHT", value.getEXWEIGHT());
						result.put("FEE", value.getFEE());
						result.put("FEEMILEAGE", value.getFEEMILEAGE());
						result.put("EXIDENTIFY", value.getEXIDENTIFY());
						result.put("LONGITUDE", value.getLONGITUDE());
						result.put("LATITUDE", value.getLATITUDE());
						result.put("STATIONNAME", value.getSTATIONNAME());
						result.put("VEHICLEID", value.getVEHICLEID());
						result.put("SITETYPE", 3);
						return result;
					}
				});

		SingleOutputStreamOperator<TrafficTransaction> TrafficTransactionStreamExit = ExitStationRecordOutputStream
				.map(new MapFunction<Map, TrafficTransaction>() {

					@Override
					public TrafficTransaction map(Map value) throws Exception {
						// TODO Auto-generated method stub
						TrafficTransaction entity = new TrafficTransaction();
						entity.setPAYID((String) value.get("PAYID"));
						entity.setEXIDENTIFY((String) value.get("EXIDENTIFY"));
						entity.setEXWEIGHT((int) value.get("EXWEIGHT"));
						entity.setPASSID((String) value.get("PASSID"));
						entity.setVEHICLEID((String) value.get("VEHICLEID"));
						List<Map> tmp = new ArrayList<Map>();
						Map<String, Object> result = new HashMap<String, Object>();
						result.put("PASSID", value.get("PASSID"));
						result.put("SITEID", value.get("EXTOLLSTATIONID"));
						result.put("TIME", value.get("EXTIME"));
						result.put("FEEMILEAGE", value.get("FEEMILEAGE"));
						result.put("FEE", value.get("FEE"));
						result.put("SITETYPE", value.get("SITETYPE"));
						result.put("LONGITUDE", value.get("LONGITUDE"));
						result.put("LATITUDE", value.get("LATITUDE"));
						result.put("NAME", value.get("STATIONNAME"));
						tmp.add(result);
						entity.setPASSEDSITES(tmp);
						return entity;
					}
				});

		DataStream<TrafficTransaction> TrafficTransactionStream = TrafficTransactionStreamEN
				.union(TrafficTransactionStreamGantry, TrafficTransactionStreamExit);

		// TrafficTransactionStream.addSink(RabbitMQDataSink.generateRMQSink("TrafficTransaction"))
		// 		.name(String.format("RMQ:%s", "TrafficTransactionStream"));

		// 第二层模型 Gantry
		// SingleOutputStreamOperator<Gantry> GantryStreamGantryInfo=
		// GantryInfoStream.map(new RichMapFunction<GantryInfo,Gantry>(){
		// /**
		// * GantryInfo->Gantry
		// */
		// @Override
		// public Gantry map(GantryInfo value) throws Exception {
		// // TODO Auto-generated method stub
		// Gantry entity=new Gantry();
		// entity.setGANTRYID(value.getGANTRYID());
		// entity.setGANTRYNAME(value.getGANTRYNAME());
		// entity.setGANTRYTYPE(value.getGANTRYTYPE());
		// return entity;
		// }

		// });
		// SingleOutputStreamOperator<Gantry> GantryStreamGantryRecord=
		// GantryRecordStream.map(new RichMapFunction<GantryRecord,Gantry>(){

		// @Override
		// public Gantry map(GantryRecord value) throws Exception {
		// // TODO Auto-generated method stub
		// Gantry entity=new Gantry();
		// entity.setTIME(value.getTRANSTIME());
		// entity.setGANTRYID(value.getGANTRYID());
		// entity.setLATITUDE(value.getLATITUDE());
		// entity.setLONGITUDE(value.getLONGITUDE());
		// return entity;
		// }

		// });
		// DataStream<Gantry>
		// GantryModelStream=GantryStreamGantryInfo.union(GantryStreamGantryRecord);
		// GantryModelStream.addSink(RabbitMQDataSink.generateRMQSink("Gantry"))
		// .name(String.format("RMQ:%s", "GantryModelStream"));
		// 第三层模型 Vehicle
		SingleOutputStreamOperator<Vehicle> VehicleStreamPart1 = ENVehicleRecordStream
				.map(new RichMapFunction<ENVehicleRecord, Vehicle>() {

					@Override
					public Vehicle map(ENVehicleRecord value) throws Exception {
						// TODO Auto-generated method stub
						Vehicle entity = new Vehicle();
						entity.setVEHICLEID(value.getVEHICLEID());
						entity.setVEHICLETYPE(value.getVEHICLETYPE());
						entity.setLIMITWEIGHT(value.getLIMITWEIGHT());
						return entity;
					}

				});
		SingleOutputStreamOperator<Vehicle> VehicleStreamPart2 = TrafficTransactionStream
				.map(new RichMapFunction<TrafficTransaction, Vehicle>() {

					@Override
					public Vehicle map(TrafficTransaction value) throws Exception {
						// TODO Auto-generated method stub
						Vehicle entity = new Vehicle();
						entity.setVEHICLEID(value.getVEHICLEID());
						entity.setPASSID(value.getPASSID());
						entity.setMEDIATYPE(value.getMEDIATYPE());
						entity.setMEDIAID(value.getMEDIAID());
						entity.setPASSEDSITES(value.getPASSEDSITES());
						return entity;
					}

				});
		SingleOutputStreamOperator<Vehicle> VehicleStreamPart3 = GantryVehicleRecordStream
				.map(new RichMapFunction<GantryVehicleRecord, Vehicle>() {

					@Override
					public Vehicle map(GantryVehicleRecord value) throws Exception {
						// TODO Auto-generated method stub
						Vehicle entity = new Vehicle();
						entity.setVEHICLEID(value.getVEHICLEID());
						entity.setVEHICLESEAT(value.getVEHICLESEAT());
						entity.setVEHICLELENGTH(value.getVEHICLELENGTH());
						entity.setVEHICLEWIDTH(value.getVEHICLEWIDTH());
						entity.setVEHICLEHIGHT(value.getVEHICLEHIGHT());
						entity.setAXLECOUNT(value.getAXLECOUNT());
						return entity;
					}

				});
		SingleOutputStreamOperator<Vehicle> VehicleStreamPart4 = ExitVehicleInfoStream
				.map(new RichMapFunction<ExitVehicleInfo, Vehicle>() {

					@Override
					public Vehicle map(ExitVehicleInfo value) throws Exception {
						// TODO Auto-generated method stub
						Vehicle entity = new Vehicle();
						entity.setVEHICLEID(value.getVEHICLEID());
						entity.setVEHICLETYPE(value.getVEHICLETYPE());
						entity.setAXISINFO(value.getAXISINFO());
						entity.setLIMITWEIGHT(value.getLIMITWEIGHT());
						return entity;
					}

				});
		DataStream<Vehicle> VehicleModelStream = VehicleStreamPart1.union(VehicleStreamPart2, VehicleStreamPart3,
				VehicleStreamPart4);
		// VehicleModelStream.addSink(RabbitMQDataSink.generateRMQSink("Vehicle"))
		// 		.name(String.format("RMQ:%s", "VehicleModelStream"));
	}
}
