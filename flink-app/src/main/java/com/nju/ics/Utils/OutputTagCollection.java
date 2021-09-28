package com.nju.ics.Utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.OutputTag;
import com.nju.ics.ModelExtractors.*;
import com.nju.ics.Models.*;
import com.nju.ics.Connectors.IotDBDataSink;
import com.nju.ics.Connectors.MQTTSink;
import com.nju.ics.Connectors.RabbitMQDataSink;
import com.nju.ics.ModelExtractors.GeneralExtractor;

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
        public static void initCollection() {
                Map<String, Tuple3<GeneralExtractor, Boolean, Boolean>> tuple = new HashMap();
                tuple.put(CPCCardExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new CPCCardExtractor(CPCCard.class), true, true));
                tuple.put(OBUCardExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new OBUCardExtractor(OBUCard.class), true, true));
                tuple.put(ETCCardExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new ETCCardExtractor(ETCCard.class), true, true));
                tuple.put(GantryExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new GantryExtractor(Gantry.class), true, false));
                tuple.put(GantryRecordExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new GantryRecordExtractor(GantryRecord.class), true, false));
                tuple.put(LaneAppExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new LaneAppExtractor(LaneApp.class), false, false));
                tuple.put(LaneExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new LaneExtractor(Lane.class), true, false));
                tuple.put(monitorOperatorExtractor.class.getSimpleName(),
                                new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                                new monitorOperatorExtractor(Operator.class), true, true));
                tuple.put(PlateExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new PlateExtractor(Plate.class), true, false));
                tuple.put(ShiftExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new ShiftExtractor(Shift.class), true, false));
                tuple.put(StationRecordExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new StationRecordExtractor(StationRecord.class), true, false));
                tuple.put(tollCollectorOperatorExtractor.class.getSimpleName(),
                                new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                                new tollCollectorOperatorExtractor(Operator.class), true, true));
                tuple.put(TrafficRecordExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new TrafficRecordExtractor(TrafficRecord.class), true, false));
                tuple.put(VehicleExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new VehicleExtractor(Vehicle.class), true, true));
                tuple.put(TollStationExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new TollStationExtractor(TollStation.class), true, false));
                tuple.put(TrafficTransactionExtractor.class.getSimpleName(),
                                new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                                new TrafficTransactionExtractor(TrafficTransaction.class), true,
                                                false));
                tuple.put(PaymentRecordExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new PaymentRecordExtractor(PaymentRecord.class), true, false));
                tuple.put(InvoiceRecordExtractor.class.getSimpleName(), new Tuple3<GeneralExtractor, Boolean, Boolean>(
                                new InvoiceRecordExtractor(InvoiceRecord.class), true, false));
                OutputTagCollection.modelExtractors = tuple;
                OutputTagCollection.buildModelOutputTags();

        }

        public static void addRMQOutputTag(String modelName, OutputTag outputtag) {
                OutputTagCollection.RMQoutputTags.put(modelName, outputtag);

        }

        public static void addIotDBOutputTag(String modelName, OutputTag outputtag) {

                OutputTagCollection.IoTDBoutputTags.put(modelName, outputtag);
        }

        public static void buildModelOutputTags() {

                for (Map.Entry<String, Tuple3<GeneralExtractor, Boolean, Boolean>> entry : OutputTagCollection.modelExtractors
                                .entrySet()) {
                        if (entry.getValue().f1) {
                                OutputTagCollection.addRMQOutputTag(entry.getValue().f0.modelcls.getSimpleName(),
                                                new OutputTag<String>(String.format("%s RMQ String",
                                                                entry.getValue().f0.modelcls.getSimpleName())) {
                                                });
                                entry.getValue().f0.setRMQtag(OutputTagCollection.RMQoutputTags
                                                .get(entry.getValue().f0.modelcls.getSimpleName()));
                        }
                        if (entry.getValue().f2) {
                                OutputTagCollection.addIotDBOutputTag(entry.getValue().f0.modelcls.getSimpleName(),
                                                new OutputTag<Map<String, String>>(String.format("%s IotDB String",
                                                                entry.getValue().f0.modelcls.getSimpleName())) {
                                                });
                                entry.getValue().f0.setIotDBtag(OutputTagCollection.IoTDBoutputTags
                                                .get(entry.getValue().f0.modelcls.getSimpleName()));
                        }

                }
        }

        public static void buildModelDataStream(SingleOutputStreamOperator stream, StreamExecutionEnvironment env) {

                for (Map.Entry<String, OutputTag> entry : OutputTagCollection.RMQoutputTags.entrySet()) {
                        // 构建将字符串写入Rabbimq队列的旁路输出
                        // stream.getSideOutput(entry.getValue()).addSink(new MQTTSink(entry.getKey()))
                        //                 .name(String.format("RMQ:%s", entry.getKey()));
                        // 构建将字符串写入Rabbimq队列的旁路输出
                        stream.getSideOutput(entry.getValue()).addSink(
                                        RabbitMQDataSink.generateCustomRMQSink(entry.getKey(), env.getConfig()))
                                        .name(String.format("RMQ:%s", entry.getKey()));
                }
                for (Map.Entry<String, OutputTag> entry : OutputTagCollection.IoTDBoutputTags.entrySet()) {
                        // 构建将字符串写入Rabbimq队列的旁路输出

                        stream.getSideOutput(entry.getValue()).addSink(IotDBDataSink.generateIoTDBSink())
                                        .name(String.format("IotDB:%s", entry.getKey()));

                }
        }

}
