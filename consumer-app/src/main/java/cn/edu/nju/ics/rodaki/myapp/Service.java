package cn.edu.nju.ics.rodaki.myapp;

import cn.edu.nju.ics.rodaki.mongodbwriter.MongodbClient;
import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqClient;
import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqPublisher;
import cn.edu.nju.ics.rodaki.servicethread.CalTrafficFlow;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Service {


    public static void main(String[] args) {
        long startTime = 1635699600000L;
        long timespan = 3600000;
        long frequency = 30000;


        if (args.length == 3) {
            startTime = Long.valueOf(args[0]);
            timespan = Long.valueOf(args[1]);
            frequency = Long.valueOf(args[2]);
        }

        long currentTime = new Date().getTime();
        System.out.println();

        Timer timer = new Timer(true);
        TimerTask task = new CalTrafficFlow(startTime, timespan, frequency);
        timer.schedule(task, new Date(currentTime + frequency), frequency);

        while(true){
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }



        }

    }

}
