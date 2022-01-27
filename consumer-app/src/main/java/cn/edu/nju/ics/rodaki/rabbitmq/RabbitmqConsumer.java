package cn.edu.nju.ics.rodaki.rabbitmq;

import cn.edu.nju.ics.rodaki.mongodbwriter.MongodbWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class RabbitmqConsumer {

    RabbitmqClient rabbitmqclient;
    Channel channel;
    String routingKey;
    MongodbWriter mongoconsumer;

    public RabbitmqConsumer(String routingKey, int prefetchCount) throws IOException, TimeoutException {
        this.routingKey = routingKey;
        this.rabbitmqclient = new RabbitmqClient(prefetchCount, routingKey);
        this.channel = rabbitmqclient.getChannel();
        this.mongoconsumer = mongoconsumer;
    }

    public void consumeAndWriteToMongodb(MongodbWriter mongoconsumer) throws IOException {

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,  // handle message
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                JSONObject obj = JSON.parseObject(message);

                mongoconsumer.insertData(obj);
                long deliveryTag = envelope.getDeliveryTag();
                channel.basicAck(deliveryTag, false);

            }
        };


        this.channel.basicConsume(this.routingKey, false, consumer); // start consumer

    }




}




