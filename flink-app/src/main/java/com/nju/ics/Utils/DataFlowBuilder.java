package com.nju.ics.Utils;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.Configs.StationPosition;
import com.nju.ics.Connectors.KafkaDataConsumer;
import com.nju.ics.Connectors.RabbitMQDataSink;
import com.nju.ics.Funcs.RawDatastreamPartitionProcess;
import com.nju.ics.ModelExtractors.*;
import com.nju.ics.Models.*;

import com.nju.ics.Operators.DataSourceFilterFunc;
import com.nju.ics.Operators.TrafficTransactionStreamGantryTrigger;
import com.nju.ics.Operators.VehicleKeyByOperator;
import com.nju.ics.Operators.VehicleRecordProcess;
import com.nju.ics.Operators.VehicleWindowsEvictor;
import com.nju.ics.Operators.VehicleWindowsProcess;
import com.nju.ics.Operators.VehicleWindowsTrigger;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
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
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.common.protocol.types.Field.Bool;
import org.apache.kafka.common.utils.Exit;
import org.apache.flink.api.common.functions.AggregateFunction;
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
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;

public class DataFlowBuilder {
	public static void generateDataStream(StreamExecutionEnvironment env, ParameterTool params) {
		
		// Map<String, Tuple3<String,Boolean, Boolean>> extrctors =
		// OutputTagCollection.initCollection();
		//OutputTagCollection.initCollection();
		
		FlinkKafkaConsumer<JSONObject> dataConsumer = KafkaDataConsumer.generateKafkaConsumer(params);
		DataStream<JSONObject> stream = env.addSource(dataConsumer).rebalance();
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
						GantryPosition.initGantryPosition();// 初始化门架经纬度
						StationPosition.initStationPosition();
					}
					
					@Override
					public ENStationRecord map(JSONObject value) throws Exception {
						return JSONObject.toJavaObject(value, ENStationRecord.class);
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
						StationPosition.initStationPosition();
					}
					@Override
					public GantryRecord map(JSONObject value) throws Exception {
						return JSONObject.toJavaObject(value, GantryRecord.class);
					}
				});
		// 第一层模型 ExitStationRecord
		SingleOutputStreamOperator<ExitStationRecord> ExitStationRecordStream = ExitStream
				.map(new RichMapFunction<JSONObject, ExitStationRecord>() {
					@Override
					public void open(Configuration parameters) throws Exception {
						// TODO Auto-generated method stub
						super.open(parameters);
						GantryPosition.initGantryPosition();// 初始化门架经纬度
						StationPosition.initStationPosition();
					}
					@Override
					public ExitStationRecord map(JSONObject value) throws Exception {
						return JSONObject.toJavaObject(value, ExitStationRecord.class);
					}
				});
		
		ENStationRecordStream.addSink(RabbitMQDataSink.generateRMQSink("testqueue"))
		.name(String.format("RMQ:%s", "ENStationRecordStream"));
		GantryRecordStream.addSink(RabbitMQDataSink.generateRMQSink("testqueue"))
		.name(String.format("RMQ:%s", "GantryRecordStream"));
		ExitStationRecordStream.addSink(RabbitMQDataSink.generateRMQSink("testqueue"))
		.name(String.format("RMQ:%s", "ExitStationRecordStream"));
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
						result.put("LONGTITUDE", value.getLONGTITUDE());
						result.put("LATITUDE", value.getLATITUDE());
						result.put("STATIONNAME", value.getSTATIONNAME());
		
						result.put("FEEMILEAGE", 0);
						result.put("FEE", 0);
						result.put("SITETYPE", 1);
						
						return result;
					}
				});
		SingleOutputStreamOperator<TrafficTransaction> TrafficTransactionStreamEN=ENStationRecordOutputStream.map(new MapFunction<Map, TrafficTransaction>(){

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
						result.put("FEEMILEAGE",  value.get("FEEMILEAGE"));
						result.put("FEE", value.get("FEE"));
						result.put("SITETYPE", value.get("SITETYPE"));
						result.put("LONGTITUDE", value.get("LONGTITUDE"));
						result.put("LATITUDE", value.get("LATITUDE"));
						result.put("NAME", value.get("STATIONNAME"));
						tmp.add(result);
						entity.setPASSEDSITES(tmp);
						return entity;
					}}
		);
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
						result.put("LONGTITUDE", value.getLONGTITUDE());
						result.put("LATITUDE", value.getLATITUDE());
						result.put("GANTRYNAME", value.getGANTRYNAME());

						result.put("SITETYPE", 2);
						return result;
					}
				});
			SingleOutputStreamOperator<TrafficTransaction> TrafficTransactionStreamGantry=GantryRecordOutputStream.map(new MapFunction<Map, TrafficTransaction>(){

					@Override
					public TrafficTransaction map(Map value) throws Exception {
						// TODO Auto-generated method stub
						TrafficTransaction entity = new TrafficTransaction();
						List<Map> tmp = new ArrayList<Map>();
						Map<String, Object> result = new HashMap<String, Object>();
						result.put("PASSID", value.get("PASSID"));
						result.put("SITEID", value.get("GANTRYID"));
						result.put("TIME", value.get("TRANSTIME"));
						result.put("FEEMILEAGE", value.get("FEEMILEAGE"));
						result.put("FEE", value.get("FEE"));
						result.put("SITETYPE", value.get("SITETYPE"));
						result.put("LONGTITUDE", value.get("LONGTITUDE"));
						result.put("LATITUDE", value.get("LATITUDE"));
						result.put("NAME", value.get("GANTRYNAME"));
						tmp.add(result);
						entity.setPASSEDSITES(tmp);
						return entity;
					}}
		);
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
						result.put("LONGTITUDE", value.getLONGTITUDE());
						result.put("LATITUDE", value.getLATITUDE());
						result.put("STATIONNAME", value.getSTATIONNAME());

						result.put("SITETYPE", 3);
						return result;
					}
				});
		
		
		SingleOutputStreamOperator<TrafficTransaction> TrafficTransactionStreamExit=ExitStationRecordOutputStream.map(new MapFunction<Map, TrafficTransaction>(){

			@Override
			public TrafficTransaction map(Map value) throws Exception {
				// TODO Auto-generated method stub
				TrafficTransaction entity = new TrafficTransaction();
				entity.setPAYID((String) value.get("PAYID"));
				entity.setEXIDENTIFY((String) value.get("EXIDENTIFY"));
				entity.setEXWEIGHT((int) value.get("EXWEIGHT"));
				
				List<Map> tmp = new ArrayList<Map>();
				Map<String, Object> result = new HashMap<String, Object>();
				result.put("PASSID", value.get("PASSID"));
				result.put("SITEID", value.get("EXTOLLSTATIONID"));
				result.put("TIME", value.get("EXTIME"));
				result.put("FEEMILEAGE", value.get("FEEMILEAGE"));
				result.put("FEE", value.get("FEE"));
				result.put("SITETYPE", value.get("SITETYPE"));
				result.put("LONGTITUDE", value.get("LONGTITUDE"));
				result.put("LATITUDE", value.get("LATITUDE"));
				result.put("NAME", value.get("STATIONNAME"));
				tmp.add(result);
				entity.setPASSEDSITES(tmp);
				return entity;
			}}
		);
		TrafficTransactionStreamEN.addSink(RabbitMQDataSink.generateRMQSink("testqueue"))
		.name(String.format("RMQ:%s", "TrafficTransactionStreamEN"));
		TrafficTransactionStreamGantry.addSink(RabbitMQDataSink.generateRMQSink("testqueue"))
		.name(String.format("RMQ:%s", "TrafficTransactionStreamGantry"));
		TrafficTransactionStreamExit.addSink(RabbitMQDataSink.generateRMQSink("testqueue"))
		.name(String.format("RMQ:%s", "TrafficTransactionStreamExit"));



		//passed sites 
		// SingleOutputStreamOperator<Map> ENStationRecordOutputPassedSites = ENStationRecordOutputStream
		// 		.map(new MapFunction<Map, Map>() {
		// 			/**
		// 			 * 2、3、4、8、9、10、11、12、13、14、15
		// 			 */
		// 			@Override
		// 			public Map map(Map value) throws Exception {
		// 				Map<String, Object> result = new HashMap<String, Object>();
		// 				result.put("PASSID", value.get("PASSID"));
		// 				result.put("SITEID", value.get("ENTOLLSTATIONID"));
		// 				result.put("TIME", value.get("ENTIME"));
		// 				result.put("FEEMILEAGE", 0);
		// 				result.put("FEE", 0);
		// 				result.put("SITETYPE", 1);
		// 				result.put("LONGTITUDE", value.get("LONGTITUDE"));
		// 				result.put("LATITUDE", value.get("LATITUDE"));
		// 				result.put("NAME", value.get("STATIONNAME"));
		// 				return result;
		// 			}
		// 		});
		// SingleOutputStreamOperator<Map> GantryRecordOutputPassedSites = GantryRecordOutputStream
		// 		.map(new MapFunction<Map, Map>() {
		// 			/**
		// 			 * 2、3、4、8、9、10、11、12
		// 			 */
		// 			@Override
		// 			public Map map(Map value) throws Exception {
		// 				Map<String, Object> result = new HashMap<String, Object>();
		// 				result.put("PASSID", value.get("PASSID"));
		// 				result.put("SITEID", value.get("GANTRYID"));
		// 				result.put("TIME", value.get("TRANSTIME"));
		// 				result.put("FEEMILEAGE", value.get("FEEMILEAGE"));
		// 				result.put("FEE", value.get("FEE"));
		// 				result.put("SITETYPE", 2);
		// 				result.put("LONGTITUDE", value.get("LONGTITUDE"));
		// 				result.put("LATITUDE", value.get("LATITUDE"));
		// 				result.put("NAME", value.get("GANTRYNAME"));
		// 				return result;
		// 			}
		// 		});
		// SingleOutputStreamOperator<Map> ExitStationRecordOutputPassedSites = ExitStationRecordOutputStream
		// 		.map(new MapFunction<Map, Map>() {
		// 			/**
		// 			 * 2、3、4、11、12、13、14、15、16、17、18
		// 			 */
		// 			@Override
		// 			public Map map(Map value) throws Exception {
		// 				Map<String, Object> result = new HashMap<String, Object>();
		// 				result.put("PASSID", value.get("PASSID"));
		// 				result.put("SITEID", value.get("EXTOLLSTATIONID"));
		// 				result.put("TIME", value.get("EXTIME"));
		// 				result.put("FEEMILEAGE", value.get("FEEMILEAGE"));
		// 				result.put("FEE", value.get("FEE"));
		// 				result.put("SITETYPE", 3);
		// 				result.put("LONGTITUDE", value.get("LONGTITUDE"));
		// 				result.put("LATITUDE", value.get("LATITUDE"));
		// 				result.put("NAME", value.get("STATIONNAME"));
		// 				return result;
		// 			}
		// 		});
		// SingleOutputStreamOperator<TrafficTransaction> TrafficTransactionStreamGantry=ENStationRecordOutputPassedSites
		// .union(GantryRecordOutputPassedSites, ExitStationRecordOutputPassedSites).keyBy(new KeySelector<Map, String>() {

		// 	@Override
		// 	public String getKey(Map value) throws Exception {
		// 		// TODO Auto-generated method stub
		// 		return (String) value.get("PASSID");

		// 	}

		// }).window(GlobalWindows.create()).trigger(new TrafficTransactionStreamGantryTrigger<>()).aggregate(new AggregateFunction<Map,TrafficTransaction,TrafficTransaction>(){

		// 	@Override
		// 	public TrafficTransaction createAccumulator() {
		// 		// TODO Auto-generated method stub
		// 		return new TrafficTransaction();
		// 	}

		// 	@Override
		// 	public TrafficTransaction add(Map value, TrafficTransaction accumulator) {
		// 		// TODO Auto-generated method stub
		// 		List<Map> tmp = new ArrayList<Map>();
		// 		if (accumulator.getPASSEDSITES()!=null){
					
				
		// 			tmp.addAll(accumulator.getPASSEDSITES());
		// 			tmp.add(value);
		// 			accumulator.setPASSEDSITES(tmp);
		// 		}
		// 		accumulator.setPASSEDSITES(tmp);
				
		// 		return accumulator;
		// 	}

		// 	@Override
		// 	public TrafficTransaction getResult(TrafficTransaction accumulator) {
		// 		// TODO Auto-generated method stub
		// 		return accumulator;
		// 	}

		// 	@Override
		// 	public TrafficTransaction merge(TrafficTransaction a, TrafficTransaction b) {
		// 		// TODO Auto-generated method stub
		// 		List<Map> tmp = new ArrayList<Map>();
		// 		if (a.getPASSEDSITES()!=null){
		// 			tmp.addAll(a.getPASSEDSITES());
		// 		}
		// 		if (b.getPASSEDSITES()!=null){
		// 			tmp.addAll(b.getPASSEDSITES());
		// 		}
		// 		TrafficTransaction accumulator=new TrafficTransaction();
		// 		accumulator.setPASSEDSITES(tmp);
		// 		return accumulator;
		// 	}
			
		// });
		// SingleOutputStreamOperator<TrafficTransaction> TrafficTransactionStream = ENStationRecordOutputStream
		// 		.union(GantryRecordOutputStream, ExitStationRecordOutputStream).keyBy(new KeySelector<Map, String>() {

		// 			@Override
		// 			public String getKey(Map value) throws Exception {
		// 				// TODO Auto-generated method stub
		// 				return (String) value.get("PASSID");

		// 			}

		// 		}).process(new KeyedProcessFunction<String, Map, TrafficTransaction>() {
		// 			/** 入站记录 */
		// 			ValueState<Map> ENStationRecord;
		// 			/** 出站记录 */
		// 			ValueState<Map> ExitStationRecord;
		// 			ValueState<Long> triggerTime;
		// 			/** 门架记录 */
		// 			StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.days(1))
		// 					.setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite)
		// 					.setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired).build();
		// 			ListState<Map> GantryStationRecord;

		// 			@Override
		// 			public void open(Configuration parameters) throws Exception {
		// 				// TODO Auto-generated method stub
		// 				super.open(parameters);
		// 				ValueStateDescriptor<Map> ENStationRecordDescriptor = new ValueStateDescriptor<>(
		// 						"ENStationRecord", Map.class);
		// 				ENStationRecordDescriptor.enableTimeToLive(ttlConfig);
		// 				ENStationRecord = getRuntimeContext().getState(ENStationRecordDescriptor);

		// 				ListStateDescriptor<Map> GantryStationRecordDescriptor = new ListStateDescriptor<>(
		// 						"GantryStationRecord", Map.class);
		// 				GantryStationRecordDescriptor.enableTimeToLive(ttlConfig);
		// 				GantryStationRecord = getRuntimeContext().getListState(GantryStationRecordDescriptor);

		// 				ValueStateDescriptor<Long> triggerTimeDescriptor = new ValueStateDescriptor<>("nextTriggerTime",
		// 						Long.class);
		// 				triggerTimeDescriptor.enableTimeToLive(ttlConfig);
		// 				triggerTime = getRuntimeContext().getState(triggerTimeDescriptor);

		// 				ValueStateDescriptor<Map> ExitStationRecordDescriptor = new ValueStateDescriptor<>(
		// 						"ExitStationRecord", Map.class);
		// 				ExitStationRecordDescriptor.enableTimeToLive(ttlConfig);
		// 				ExitStationRecord = getRuntimeContext().getState(ExitStationRecordDescriptor);

		// 			}

		// 			@Override
		// 			public void processElement(Map value, Context ctx, Collector out) throws Exception {
		// 				// TODO Auto-generated method stub
		// 				switch ((int) value.get(DataSourceJudge.sourceKey)) {
		// 					case DataSourceJudge.entryLane:
		// 						ENStationRecord.update(value);
		// 						clearTimer(ctx);
		// 						registerTimer(ctx, ctx.timestamp());
		// 						break;
		// 					case DataSourceJudge.gantryCharge:
		// 						GantryStationRecord.add(value);
		// 						clearTimer(ctx);
		// 						registerTimer(ctx, ctx.timestamp());
		// 						break;
		// 					case DataSourceJudge.exitLane:
		// 						ExitStationRecord.update(value);
		// 						generate(out);
		// 						clearTimer(ctx);
		// 						clear();
		// 						break;
		// 					default:
		// 						break;
		// 				}

		// 			}

		// 			@Override
		// 			public void onTimer(long timestamp,
		// 					KeyedProcessFunction<String, Map, TrafficTransaction>.OnTimerContext ctx,
		// 					Collector<TrafficTransaction> out) throws Exception {
		// 				// TODO Auto-generated method stub
		// 				super.onTimer(timestamp, ctx, out);
		// 				generate(out);
		// 				clearTimer(ctx);
		// 				clear();
		// 			}

		// 			private void generate(Collector out) {
		// 				TrafficTransaction entity = new TrafficTransaction();
		// 				List<Map> tmp = new ArrayList<Map>();
		// 				Map en = new HashMap<>();
		// 				try {
		// 					if (ENStationRecord.value() != null) {
		// 						entity.setPASSID((String) ENStationRecord.value().get("PASSID"));
		// 						entity.setVEHICLEID((String) ENStationRecord.value().get("VEHICLEID"));
		// 						entity.setMEDIAID((String) ENStationRecord.value().get("MEDIAID"));
		// 						entity.setMEDIATYPE((int) ENStationRecord.value().get("MEDIATYPE"));
		// 						entity.setENWEIGHT((int) ENStationRecord.value().get("ENWEIGHT"));
		// 						entity.setENIDENTIFY((String) ENStationRecord.value().get("ENIDENTIFY"));
		// 						en.put("SITEID", ENStationRecord.value().get("ENTOLLSTATIONID"));
		// 						en.put("TIME", ENStationRecord.value().get("ENTIME"));
		// 						en.put("FEEMILEAGE", 0);
		// 						en.put("FEE", 0);
		// 						en.put("SITETYPE", 1);
		// 						en.put("LONGTITUDE", ENStationRecord.value().get("LONGTITUDE"));
		// 						en.put("LATITUDE", ENStationRecord.value().get("LATITUDE"));
		// 						en.put("NAME", ENStationRecord.value().get("STATIONNAME"));
		// 						tmp.add(en);
		// 					}

		// 					if (GantryStationRecord.get() != null) {
		// 						Iterator<Map> gantryIter = GantryStationRecord.get().iterator();
		// 						Map gantry;

		// 						while (gantryIter.hasNext()) {
		// 							gantry = gantryIter.next();
		// 							en = new HashMap<>();
		// 							en.put("SITEID", gantry.get("GANTRYID"));
		// 							en.put("TIME", gantry.get("TRANSTIME"));
		// 							en.put("FEEMILEAGE", gantry.get("FEEMILEAGE"));
		// 							en.put("FEE", gantry.get("FEE"));
		// 							en.put("SITETYPE", 2);
		// 							en.put("LONGTITUDE", gantry.get("LONGTITUDE"));
		// 							en.put("LATITUDE", gantry.get("LATITUDE"));
		// 							en.put("NAME", gantry.get("GANTRYNAME"));
		// 							tmp.add(en);
		// 						}
		// 					}
		// 					if (ExitStationRecord.value() != null) {
		// 						entity.setPAYID((String) ExitStationRecord.value().get("PAYID"));
		// 						entity.setEXIDENTIFY((String) ExitStationRecord.value().get("EXIDENTIFY"));
		// 						entity.setEXWEIGHT((int) ExitStationRecord.value().get("EXWEIGHT"));
		// 						en = new HashMap<>();
		// 						en.put("SITEID", ExitStationRecord.value().get("EXTOLLSTATIONID"));
		// 						en.put("TIME", ExitStationRecord.value().get("EXTIME"));
		// 						en.put("FEEMILEAGE", ExitStationRecord.value().get("FEEMILEAGE"));
		// 						en.put("FEE", ExitStationRecord.value().get("FEE"));
		// 						en.put("SITETYPE", 3);
		// 						en.put("LONGTITUDE", ExitStationRecord.value().get("LONGTITUDE"));
		// 						en.put("LATITUDE", ExitStationRecord.value().get("LATITUDE"));
		// 						en.put("NAME", ExitStationRecord.value().get("STATIONNAME"));
		// 						tmp.add(en);
		// 					}

		// 				} catch (Exception e) {
		// 					// TODO Auto-generated catch block
		// 					e.printStackTrace();
		// 				}
		// 				entity.setPASSEDSITES(tmp);
		// 				out.collect(entity);
		// 			}

		// 			private void clear() {
		// 				ENStationRecord.clear();
		// 				GantryStationRecord.clear();
		// 				triggerTime.clear();
		// 			}

		// 			private void clearTimer(Context ctx) {
		// 				try {
		// 					if (triggerTime.value() != null) {
		// 						ctx.timerService().deleteEventTimeTimer(triggerTime.value());
		// 					}
		// 				} catch (IOException e) {
		// 					// TODO Auto-generated catch block
		// 					e.printStackTrace();
		// 				}
		// 			}

		// 			private void registerTimer(Context ctx, Long timestamp) {
		// 				try {
		// 					triggerTime.update(timestamp + Duration.ofMinutes(30).toMillis());
		// 					ctx.timerService().registerEventTimeTimer(triggerTime.value());
		// 				} catch (IOException e) {
		// 					// TODO Auto-generated catch block
		// 					e.printStackTrace();
		// 				}

		// 			}
		// 		});
	
		//TrafficTransactionStream.map(x -> JSONObject.toJSON(x)).print();
		// TrafficTransactionStream.print();
		// SingleOutputStreamOperator<String> KeyByVehicleRecordsStream =
		// FilterdVehicleRecordsStream
		// .keyBy(new VehicleKeyByOperator()).process(new VehicleRecordProcess());

		// WindowedStream<JSONObject, String, GlobalWindow> mainDataStream =
		// KeyByVehicleRecordsStream
		// .window(GlobalWindows.create());
		// mainDataStream.trigger(new VehicleWindowsTrigger<>()).evictor(new
		// VehicleWindowsEvictor<>()).process(new VehicleWindowsProcess<>());
		// OutputTagCollection.buildModelDataStream(KeyByVehicleRecordsStream, env);

	}
}
