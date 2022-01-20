package cn.edu.nju.ics.rodaki.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;





public class RabbitmqPublisher {

    private String queueName;
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;


    public RabbitmqPublisher(String queueName) throws IOException, TimeoutException {
        this.queueName = queueName;



        this.factory = new ConnectionFactory();

        factory.setHost("rabbitmq");
 

        this.factory.setPort(5672);
        this.factory.setVirtualHost("/");
        this.factory.setUsername("admin");
        this.factory.setPassword("admin");
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        //声明队列。
        //参数1：队列名
        //参数2：持久化 （true表示是，队列将在服务器重启时依旧存在）
        //参数3：独占队列（创建者可以使用的私有队列，断开后自动删除）
        //参数4：当所有消费者客户端连接断开时是否自动删除队列
        //参数5：队列的其他参数
        this.channel.queueDeclare(queueName,true,false,false,null);

    }


    public void pushMassage(String msg) throws IOException {
        // 基本发布消息
        // 第一个参数为交换机名称、
        // 第二个参数为队列映射的路由key、
        // 第三个参数为消息的其他属性、
        // 第四个参数为发送信息的主体
        channel.basicPublish("",queueName, null, msg.getBytes("UTF-8"));

    }



}