package com.nju.ics.connectors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

public class CustomRMQSink<T> extends RMQSink<T> {
    private String myqueueName;
    private String routeKey;
    private String Exchange;

    public String getMyqueueName() {
        return myqueueName;
    }

    public void setMyqueueName(String myqueueName) {
        this.myqueueName = myqueueName;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

    public String getExchange() {
        return Exchange;
    }

    public void setExchange(String exchange) {
        Exchange = exchange;
    }
    public  CustomRMQSink(){
        super(null,null, null, null);
    }
    public CustomRMQSink(RMQConnectionConfig rmqConnectionConfig, String queuename, String routeKey,
            String Exchange, SerializationSchema<T> schema, RMQSinkPublishOptions<T> publishOptions) {
        super(rmqConnectionConfig, schema, publishOptions);
        // TODO Auto-generated constructor stub
        this.myqueueName = String.format("mqtt-subscription-%s-mqtt-connectionqos1", queuename);
        this.routeKey = routeKey;
        this.Exchange = Exchange;
    }

    @Override
    protected void setupQueue() throws IOException {
        //this.channel.exchangeDeclare(this.Exchange, "direct", true, false, null);
        Map<String,Object> args= new HashMap<String, Object>();
        args.put("x-expires", 86400000);
        this.channel.queueDeclare(this.myqueueName, true, false, false,args);// 声明queue
        this.channel.queueBind(this.myqueueName, RabbitMQDataSink.exchange, this.routeKey);// 将queuq绑定到exchange，routekey上
        
    }
}
