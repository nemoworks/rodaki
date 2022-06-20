package com.nju.ics.connectors;

import com.rabbitmq.client.AMQP.BasicProperties;


import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;

public class DummyPublishOptions<T> implements RMQSinkPublishOptions<T> {
    private String routeKey;
    private String Exchange;

    // private static String routeKey="zc";
    private static BasicProperties props = new BasicProperties.Builder()
            .contentType("application/vnd.eclipse.ditto+json").contentEncoding("UTF-8").build();
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
    public DummyPublishOptions(){

    }
    public DummyPublishOptions(String routeKey, String Exchange) {
        this.Exchange = Exchange;
        this.routeKey = routeKey;
    }

    @Override
    public String computeRoutingKey(T a) {
        // TODO Auto-generated method stub
        return this.routeKey;
    }

    @Override
    public com.rabbitmq.client.AMQP.BasicProperties computeProperties(T a) {
        // TODO Auto-generated method stub
        return props;
    }

    @Override
    public String computeExchange(T a) {
        // TODO Auto-generated method stub
        return this.Exchange;
    }

}