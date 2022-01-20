package cn.edu.nju.ics.rodaki.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitmqClient {

    int prefetchCount;
    String routingKey;
    Connection connection;
    Channel channel;


    public RabbitmqClient(int prefetchCount, String routingKey) throws IOException, TimeoutException {
        this.prefetchCount = prefetchCount;
        this.routingKey = routingKey;

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setVirtualHost("/");
        factory.setUsername("admin");
        factory.setPassword("admin");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(prefetchCount);
    }

    public Channel getChannel() {
        return channel;
    }
}
