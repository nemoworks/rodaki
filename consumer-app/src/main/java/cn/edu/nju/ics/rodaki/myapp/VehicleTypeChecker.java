package cn.edu.nju.ics.rodaki.myapp;

import cn.edu.nju.ics.rodaki.mongodbwriter.ExitRecordWriter;
import cn.edu.nju.ics.rodaki.mongodbwriter.MongodbClient;
import cn.edu.nju.ics.rodaki.mongodbwriter.VehicleTypeWriter;
import cn.edu.nju.ics.rodaki.pipelinethread.RabbitmqConsumerThread;
import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqClient;
import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqPublisher;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class VehicleTypeChecker {
    public static void main(String[] args) throws IOException, TimeoutException {



        int prefetch = 500;

        VehicleTypeWriter exRecWriter1 = new VehicleTypeWriter("RealVehicleType", "RealVehicleType");
        RabbitmqConsumerThread Rct1 = new RabbitmqConsumerThread("CheckVehicleType", prefetch, exRecWriter1);
        Thread RThrd1 = new Thread(Rct1);
        RThrd1.start();



        while(true) {
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
}
