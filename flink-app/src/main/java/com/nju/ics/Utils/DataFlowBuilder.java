package com.nju.ics.Utils;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Configs.GantryPosition;
import com.nju.ics.Connectors.KafkaDataConsumer;
import com.nju.ics.Connectors.RabbitMQDataSink;
import com.nju.ics.ModelExtractors.*;
import com.nju.ics.Models.*;

import com.nju.ics.Operators.DataSourceFilterFunc;

import com.nju.ics.Operators.VehicleKeyByOperator;
import com.nju.ics.Operators.VehicleRecordProcess;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.common.protocol.types.Field.Bool;

public class DataFlowBuilder {
	public static void generateDataStream(StreamExecutionEnvironment env, ParameterTool params) {
		GantryPosition.initGantryPosition();// 初始化门架经纬度
		OutputTagCollection.initCollection();
		FlinkKafkaConsumer<JSONObject> dataConsumer = KafkaDataConsumer.generateKafkaConsumer(params);
		DataStream<JSONObject> stream = env.addSource(dataConsumer).rebalance();
		DataStream<JSONObject> FilterdVehicleRecordsStream = stream.filter(new DataSourceFilterFunc());

		KeyedStream<JSONObject, String> KeyByVehicleRecordsStream = FilterdVehicleRecordsStream
				.keyBy(new VehicleKeyByOperator());
		SingleOutputStreamOperator<String> mainDataStream = KeyByVehicleRecordsStream
				.process(new VehicleRecordProcess()).setMaxParallelism(10).setParallelism(1);
		OutputTagCollection.buildModelDataStream(mainDataStream, env);

	}
}
