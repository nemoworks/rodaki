package com.nju.ics.utils;

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
import com.nju.ics.configs.GantryPosition;
import com.nju.ics.configs.StationPosition;
import com.nju.ics.connectors.KafkaDataConsumer;
import com.nju.ics.connectors.RabbitMQDataSink;
import com.nju.ics.funcs.DataSourceFilterFunc;
import com.nju.ics.funcs.RawDatastreamPartitionProcess;
import com.nju.ics.funcs.Row2JSONObject;
import com.nju.ics.funcs.TrafficTransactionStreamGantryTrigger;
import com.nju.ics.funcs.VehicleKeyByOperator;
import com.nju.ics.funcs.VehicleRecordProcess;
import com.nju.ics.funcs.VehicleWindowsEvictor;
import com.nju.ics.funcs.VehicleWindowsProcess;
import com.nju.ics.funcs.VehicleWindowsTrigger;
import com.nju.ics.modelextractors.*;
import com.nju.ics.models.*;

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

public class ModelExtractor2 {
	public static void generateDataStream(StreamExecutionEnvironment env, ParameterTool params) {

		// Map<String, Tuple3<String,Boolean, Boolean>> extrctors =
		// OutputTagCollection.initCollection();
		// OutputTagCollection.initCollection();
		// 输入文件路径
		String gantrycsv = "/hdd/data/1102/gantrywaste_fix.csv";
		String entrycsv = "/hdd/data/1102/enwaste.csv";
		String exitcsv = "/hdd/data/1102/exitwaste.csv";
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

		// ENStationRecordStream.addSink(RabbitMQDataSink.generateRMQSink("SiteRecord"))
		// .name(String.format("RMQ:%s", "ENStationRecordStream"));
		// ENVehicleRecordStream.addSink(RabbitMQDataSink.generateRMQSink("ENVehicleRecord"))
		// .name(String.format("RMQ:%s", "ENVehicleRecordStream"));
		// GantryRecordStream.addSink(RabbitMQDataSink.generateRMQSink("SiteRecord"))
		// .name(String.format("RMQ:%s", "GantryRecordStream"));
		// GantryRecordStream.map( new MapFunction<GantryRecord,String>() {

		// @Override
		// public String map(GantryRecord value) throws Exception {
		// // TODO Auto-generated method stub
		// System.out.println(value.getSPECIALTYPE());
		// return null;
		// }

		// });
		// GantryInfoStream.addSink(RabbitMQDataSink.generateRMQSink("GantryInfo"))
		// .name(String.format("RMQ:%s", "GantryInfoStream"));
		// GantryVehicleRecordStream.addSink(RabbitMQDataSink.generateRMQSink("GantryVehicleRecord"))
		// .name(String.format("RMQ:%s", "GantryVehicleRecord"));
		// ExitStationRecordStream.addSink(RabbitMQDataSink.generateRMQSink("SiteRecord"))
		// .name(String.format("RMQ:%s", "ExitStationRecordStream"));
		// ExitPaymentRecordStream.addSink(RabbitMQDataSink.generateRMQSink("ExitPaymentRecord"))
		// .name(String.format("RMQ:%s", "ExitPaymentRecord"));
		// ExitInvoiceRecordStream.addSink(RabbitMQDataSink.generateRMQSink("ExitInvoiceRecord"))
		// .name(String.format("RMQ:%s", "ExitInvoiceRecord"));
		// ExitVehicleInfoStream.addSink(RabbitMQDataSink.generateRMQSink("ExitVehicleInfo"))
		// .name(String.format("RMQ:%s", "ExitVehicleInfo"));
		SingleOutputStreamOperator<JSONObject> EntryStationRecordOutputStream = ENStationRecordStream
				.map(x -> (JSONObject) JSONObject.toJSON(x));
		SingleOutputStreamOperator<JSONObject> EntryStationRecordProjectTrafficTransaction = EntryStationRecordOutputStream
				.map(new MapFunction<JSONObject, JSONObject>() {
					@Override
					public JSONObject map(JSONObject value) throws Exception {
						JSONObject result = new JSONObject();
						result.put("PASSID", value.get("PASSID"));
						result.put("VEHICLEID", value.get("VEHICLEID"));
						result.put("MEDIAID", value.get("MEDIAID"));
						result.put("MEDIATYPE", value.get("MEDIATYPE"));
						result.put("ENWEIGHT", value.get("ENWEIGHT"));
						result.put("ENIDENTIFY", value.get("ENIDENTIFY"));
						return result;
					}
				});
		SingleOutputStreamOperator<JSONObject> ExitStationRecordOutputStream = ExitStationRecordStream
				.map(x -> (JSONObject) JSONObject.toJSON(x));
		SingleOutputStreamOperator<JSONObject> ExitStationRecordProjectTrafficTransaction = ExitStationRecordOutputStream
				.map(new MapFunction<JSONObject, JSONObject>() {
					@Override
					public JSONObject map(JSONObject value) throws Exception {
						JSONObject result = new JSONObject();
						result.put("EXWEIGHT", value.get("EXWEIGHT"));
						result.put("PAYID", value.get("PAYID"));
						result.put("EXIDENTIFY", value.get("EXIDENTIFY"));
						result.put("PASSID", value.get("PASSID"));
						result.put("VEHICLEID", value.get("VEHICLEID"));
						return result;
					}
				});
		SingleOutputStreamOperator<JSONObject> GantryRecordOutputStream = GantryVehicleRecordStream
				.map(x -> (JSONObject) JSONObject.toJSON(x));
		SingleOutputStreamOperator<JSONObject> GantryRecordProjectTrafficTransaction = GantryRecordOutputStream
				.map(new MapFunction<JSONObject, JSONObject>() {
					@Override
					public JSONObject map(JSONObject value) throws Exception {
						JSONObject result = new JSONObject();
						result.put("PASSID", value.get("PASSID"));
						result.put("VEHICLEID", value.get("VEHICLEID"));
						return result;
					}
				});
		DataStream<JSONObject> ProjectionOutputTrafficTransaction = EntryStationRecordProjectTrafficTransaction
				.union(ExitStationRecordProjectTrafficTransaction, GantryRecordProjectTrafficTransaction);
	}
}
