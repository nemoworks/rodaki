package com.nju.ics.Connectors;

import java.time.Duration;
import java.util.Properties;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Utils.ConfigureENV;
import com.nju.ics.Utils.InputTimeStampAssigner;
import com.nju.ics.Utils.JsonObjectDeserializationSchema;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

public class KafkaDataConsumer {
    public static FlinkKafkaConsumer<JSONObject> generateKafkaConsumer(ParameterTool params) {
        String inputTopic = params.get("input-topic", ConfigureENV.prop.getProperty("flink.inputTopic"));
        String brokers = params.get("bootstrap.servers", ConfigureENV.prop.getProperty("kafka.bootstrap.servers"));
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        kafkaProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "highway-data1");
        kafkaProps.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // kafkaProps.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, "analyse");
        // 将卡夫卡的数据转为jsonnode
        FlinkKafkaConsumer<JSONObject> dataConsumer = new FlinkKafkaConsumer<JSONObject>(inputTopic,
                new JsonObjectDeserializationSchema(), kafkaProps);
        // 设置成从最早的记录开始读取，方便调试
        dataConsumer.setStartFromEarliest();
        dataConsumer.assignTimestampsAndWatermarks(
                WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(60))
                        .withIdleness(Duration.ofMinutes(1)).withTimestampAssigner(new InputTimeStampAssigner()));
        return dataConsumer;

    }
}
