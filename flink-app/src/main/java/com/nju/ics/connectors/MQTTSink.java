package com.nju.ics.connectors;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.nju.ics.utils.ConfigureENV;

public class MQTTSink extends RichSinkFunction<String> {
    MqttClient mqttclient;
    String topic;
    int qos = Integer.parseInt(ConfigureENV.prop.getProperty("mqtt.qos"));
    String broker;

    public MQTTSink(String topic) {
        this.broker = String.format("tcp://%s:%d", ConfigureENV.prop.getProperty("mqtt.host"),
                Integer.parseInt(ConfigureENV.prop.getProperty("mqtt.port")));
        this.topic = topic;
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        // mqttclient.disconnect();
        mqttclient.close();
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        MemoryPersistence persistence = new MemoryPersistence();
        mqttclient = new MqttClient(this.broker, topic,persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);
        connOpts.setMaxInflight(100);
        //mqttclient.setCallback(new OnMessageCallback());
        mqttclient.connect(connOpts);
    }

    @Override
    public void invoke(String value) throws Exception {
        // TODO Auto-generated method stub
        // 消息发布所需参数
        MqttMessage message = new MqttMessage(value.getBytes());
        message.setQos(qos);
        message.setRetained(true);
        mqttclient.publish(this.topic, message);
        // System.out.println("Message published");
    }

    class OnMessageCallback implements MqttCallback {
        public void connectionLost(Throwable cause) {
            // 连接丢失后，一般在这里面进行重连
            System.out.println("连接断开，可以做重连");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            // subscribe后得到的消息会执行到这里面
            System.out.println("接收消息主题:" + topic);
            System.out.println("接收消息Qos:" + message.getQos());
            System.out.println("接收消息内容:" + new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            System.out.println("deliveryComplete---------" + token.isComplete());
        }

    }
}
