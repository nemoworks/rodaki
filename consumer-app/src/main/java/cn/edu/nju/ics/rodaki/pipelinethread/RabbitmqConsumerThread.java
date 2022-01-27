package cn.edu.nju.ics.rodaki.pipelinethread;

import cn.edu.nju.ics.rodaki.mongodbwriter.MongodbWriter;
import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqConsumer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitmqConsumerThread implements Runnable{
    String routingKey;
    int prefetchCount;
    MongodbWriter client;
    RabbitmqConsumer consumer = null;

    public RabbitmqConsumerThread(String routingKey, int prefetchCount, MongodbWriter client) {
        this.routingKey = routingKey;
        this.prefetchCount = prefetchCount;
        this.client = client;
    }

    @Override
    public void run() {


        try {
            consumer = new RabbitmqConsumer(this.routingKey, this.prefetchCount);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


        try {
            consumer.consumeAndWriteToMongodb(client);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}