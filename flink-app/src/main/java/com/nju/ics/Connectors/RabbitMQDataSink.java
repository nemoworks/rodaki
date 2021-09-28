package com.nju.ics.Connectors;

import java.io.IOException;
import java.util.Collections;

import com.nju.ics.Models.CPCCard;

import com.nju.ics.Utils.ConfigureENV;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.streaming.util.serialization.TypeInformationSerializationSchema;
import com.rabbitmq.client.Channel;;

public class RabbitMQDataSink {

        static String exchange = "amq.topic";
        // private static String exchange = "ics.nju";

        public static CustomRMQSink<String> generateCustomRMQSink(String queueName, ExecutionConfig config) {
                final RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
                                .setHost(ConfigureENV.prop.getProperty("RMQ.host"))
                                .setPort(Integer.parseInt(ConfigureENV.prop.getProperty("RMQ.port")))
                                .setUserName(ConfigureENV.prop.getProperty("RMQ.user"))
                                .setPassword(ConfigureENV.prop.getProperty("RMQ.password"))
                                .setVirtualHost(ConfigureENV.prop.getProperty("RMQ.vhost")).build();
                // TypeInformation<String> info = TypeInformation.of(new TypeHint<CPCCard>() {
                // });
                queueName="highway";
                DummyPublishOptions<String> publishOptions = new DummyPublishOptions<String>(queueName,
                                RabbitMQDataSink.exchange);
                CustomRMQSink<String> rmqsink = new CustomRMQSink<String>(connectionConfig, queueName, queueName,
                                RabbitMQDataSink.exchange, new SimpleStringSchema(), publishOptions);

                return rmqsink;
        }

        public static RMQSink<String> generateRMQSink(String queueName, ExecutionConfig config) {
                final RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
                                .setHost(ConfigureENV.prop.getProperty("RMQ.host"))
                                .setPort(Integer.parseInt(ConfigureENV.prop.getProperty("RMQ.port")))
                                .setUserName(ConfigureENV.prop.getProperty("RMQ.user"))
                                .setPassword(ConfigureENV.prop.getProperty("RMQ.password"))
                                .setVirtualHost(ConfigureENV.prop.getProperty("RMQ.vhost")).build();

                RMQSink rmqsink = new RMQSink(connectionConfig,
                                String.format("mqtt-subscription-%s-mqtt-connectionqos1", queueName),
                                new SimpleStringSchema());
                return rmqsink;
        }

}
