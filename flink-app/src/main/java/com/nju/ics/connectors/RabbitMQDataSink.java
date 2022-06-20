package com.nju.ics.connectors;

import java.io.IOException;
import java.util.Collections;

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
import com.rabbitmq.client.Channel;
import com.nju.ics.models.CPCCard;
import com.nju.ics.utils.ConfigureENV;
import com.nju.ics.utils.ObjectSerializationSchema;
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
                queueName="highway2hours";
                DummyPublishOptions<String> publishOptions = new DummyPublishOptions<String>(queueName,
                                RabbitMQDataSink.exchange);
                CustomRMQSink<String> rmqsink = new CustomRMQSink<String>(connectionConfig, queueName, queueName,
                                RabbitMQDataSink.exchange, new SimpleStringSchema(), publishOptions);

                return rmqsink;
        }

        public static RMQSink generateRMQSink(String queueName) {
                final RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
                                .setHost(ConfigureENV.prop.getProperty("RMQ.host"))
                                .setPort(Integer.parseInt(ConfigureENV.prop.getProperty("RMQ.port")))
                                .setUserName(ConfigureENV.prop.getProperty("RMQ.user"))
                                .setPassword(ConfigureENV.prop.getProperty("RMQ.password"))
                                .setVirtualHost(ConfigureENV.prop.getProperty("RMQ.vhost")).build();
                RMQSink rmqsink = new RMQSink(connectionConfig,
                                String.format("%s", queueName),
                                new ObjectSerializationSchema());
                return rmqsink;
        }

}
