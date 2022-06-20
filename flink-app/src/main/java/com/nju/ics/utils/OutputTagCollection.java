package com.nju.ics.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.OutputTag;

import com.nju.ics.connectors.CouchDBSink;
import com.nju.ics.connectors.IotDBDataSink;
import com.nju.ics.connectors.MQTTSink;
import com.nju.ics.connectors.MongoDBAsyncSink;
import com.nju.ics.connectors.RabbitMQDataSink;
import com.nju.ics.dbs.CouchDB;
import com.nju.ics.dbs.MongoDB;
import com.nju.ics.modelextractors.*;
import com.nju.ics.models.*;
import com.alibaba.fastjson.JSONObject;

public class OutputTagCollection {
        public static Map<String, OutputTag> RMQoutputTags = new HashMap<>();
        public static Map<String, OutputTag> IoTDBoutputTags = new HashMap<>();
        public static Map<String, Tuple3<GeneralExtractor, Boolean, Boolean>> modelExtractors;

        /**
         * 
         * @param tuple 一个字典，存放model name与其output tag的信息，可能有些model并不是两种output
         *              tag都有，因此设置了相应的标识位 第一个bool值表示RMQ的，第二个为iotdb的
         * 
         */
        public static Map<String, Tuple2<Boolean, Boolean>> initCollection() {
                Map<String, Tuple3<GeneralExtractor, Boolean, Boolean>> tuple = new HashMap();

                tuple.put(ENVehicleRecordExtractor.class.getSimpleName(),
                                new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                                new ENVehicleRecordExtractor(ENVehicleRecord.class), true, true));
                tuple.put(ENStationRecordExtractor.class.getSimpleName(),
                                new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                                new ENStationRecordExtractor(ENStationRecord.class), true, true));
                OutputTagCollection.modelExtractors = tuple;
                return OutputTagCollection.buildModelOutputTags();

        }

        public static void addRMQOutputTag(String modelName, OutputTag outputtag) {
                OutputTagCollection.RMQoutputTags.put(modelName, outputtag);

        }

        public static void addIotDBOutputTag(String modelName, OutputTag outputtag) {

                OutputTagCollection.IoTDBoutputTags.put(modelName, outputtag);
        }

        public static Map<String, Tuple2<Boolean, Boolean>> buildModelOutputTags() {
                Map<String, Tuple2<Boolean, Boolean>> extrctors = new HashMap();
                for (Map.Entry<String, Tuple3<GeneralExtractor, Boolean, Boolean>> entry : OutputTagCollection.modelExtractors
                                .entrySet()) {
                        Tuple2 tmp = new Tuple2<Boolean, Boolean>(false, false);
                        if (entry.getValue().f1) {
                                // OutputTagCollection.addRMQOutputTag(entry.getValue().f0.modelcls.getSimpleName(),
                                // new OutputTag<String>(String.format("%s RMQ String",
                                // entry.getValue().f0.modelcls.getSimpleName())) {
                                // });
                                OutputTagCollection.addRMQOutputTag(entry.getValue().f0.modelcls.getSimpleName(),
                                                new OutputTag<String>(String.format("%s RMQ String",
                                                                entry.getValue().f0.modelcls.getSimpleName())) {
                                                });
                                // entry.getValue().f0.setRMQtag(OutputTagCollection.RMQoutputTags
                                // .get(entry.getValue().f0.modelcls.getSimpleName()));
                                tmp.f0 = true;
                        }
                        if (entry.getValue().f2) {
                                OutputTagCollection.addIotDBOutputTag(entry.getValue().f0.modelcls.getSimpleName(),
                                                new OutputTag<Map<String, String>>(String.format("%s IotDB String",
                                                                entry.getValue().f0.modelcls.getSimpleName())) {
                                                });
                                // entry.getValue().f0.setIotDBtag(OutputTagCollection.IoTDBoutputTags
                                // .get(entry.getValue().f0.modelcls.getSimpleName()));
                                tmp.f1 = true;
                        }
                        extrctors.put(entry.getKey(), tmp);
                }
                return extrctors;

        }

        public static void buildModelDataStream(SingleOutputStreamOperator stream, StreamExecutionEnvironment env) {

                for (Map.Entry<String, OutputTag> entry : OutputTagCollection.RMQoutputTags.entrySet()) {
                        // 构建将字符串写入MQTT队列的旁路输出
                        // stream.getSideOutput(entry.getValue()).addSink(new MQTTSink(entry.getKey()))
                        // .name(String.format("RMQ:%s", entry.getKey()));
                        // 构建将字符串写入Rabbimq队列的旁路输出
                        stream.getSideOutput(entry.getValue()).addSink(RabbitMQDataSink.generateRMQSink(entry.getKey()))
                                        .name(String.format("RMQ:%s", entry.getKey()));
                        // 构建将字符串写入CouchDB队列的旁路输出
                        // stream.getSideOutput(entry.getValue()).addSink(new
                        // CouchDBSink(CouchDB.getClient()))
                        // .name(String.format("CouchDB:%s", entry.getKey()));
                        // 构建将字符串写入MongoDB队列的旁路输出
                        // stream.getSideOutput(entry.getValue()).addSink(new
                        // CouchDBSink(CouchDB.getClient()))
                        // .name(String.format("CouchDB:%s", entry.getKey()));

                        // AsyncDataStream.unorderedWait(stream.getSideOutput(entry.getValue()),
                        // new MongoDBAsyncSink(MongoDB.getDBConnectConfig(), entry.getKey()), (long)
                        // 30,
                        // TimeUnit.SECONDS, 200).name(String.format("MongoDB:%s", entry.getKey()));
                }
                for (Map.Entry<String, OutputTag> entry : OutputTagCollection.IoTDBoutputTags.entrySet()) {
                        // // 构建将字符串写入iotdb的旁路输出

                        // stream.getSideOutput(entry.getValue()).addSink(IotDBDataSink.generateIoTDBSink())
                        // .name(String.format("IotDB:%s", entry.getKey()));

                }
        }

}
