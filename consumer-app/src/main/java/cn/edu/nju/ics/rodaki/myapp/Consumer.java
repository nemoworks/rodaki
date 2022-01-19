package cn.edu.nju.ics.rodaki.myapp;



import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqClient;
import cn.edu.nju.ics.rodaki.pipelinethread.MongodbListenerThread;
import cn.edu.nju.ics.rodaki.pipelinethread.RabbitmqConsumerThread;
import cn.edu.nju.ics.rodaki.mongodbwriter.*;
import cn.edu.nju.ics.rodaki.mongodblistener.*;
import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqPublisher;

import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;


public class Consumer {



    public static void main(String[] args) throws IOException, TimeoutException, ParseException, InterruptedException {

        int prefetch = 500;

        String entitydb = "EntityModel";
        String digitaldb = "DigitalModel";



        if (args.length == 2) {
            entitydb = args[0];
            digitaldb = args[1];


            System.out.println(" =========================== Use parameters =========================== ");
            for (int i = 0; i < args.length; i++) {
                System.out.println(args[i]);
            }

        }



        // First, construct a MyThread object.
        TranListenToRabbitmq traTranListener = new TranListenToRabbitmq(entitydb, "TrafficTransaction");
        GanListenToRabbitmq ganListener = new GanListenToRabbitmq(entitydb, "Gantry");
        VehListenToRabbitmq vehListener = new VehListenToRabbitmq(entitydb, "Vehicle");

        MongodbListenerThread Mlt1 = new MongodbListenerThread(traTranListener);
        MongodbListenerThread Mlt2 = new MongodbListenerThread(ganListener);
        MongodbListenerThread Mlt3 = new MongodbListenerThread(vehListener);


        // Next, construct a thread from that object.

        Thread MThrd1 = new Thread(Mlt1);
        Thread MThrd2 = new Thread(Mlt2);
        Thread MThrd3 = new Thread(Mlt3);


        // Finally, start execution of the thread.

        MThrd1.start();
        MThrd2.start();
        MThrd3.start();





        // First, construct a MyThread object.
        EntryRecordWriter enRecWriter = new EntryRecordWriter(entitydb, "EntryRecord", "TrafficTransaction");
        GantryRecordWriter ganRecWriter1 = new GantryRecordWriter(entitydb, "GantryRecord", "TrafficTransaction");
        GantryRecordWriter ganRecWriter2 = new GantryRecordWriter(entitydb, "GantryRecord", "TrafficTransaction");
        GantryRecordWriter ganRecWriter3 = new GantryRecordWriter(entitydb, "GantryRecord", "TrafficTransaction");
        ExitRecordWriter exRecWriter = new ExitRecordWriter(entitydb, "ExitRecord", "TrafficTransaction");
        EntryAndExitVehicleWriter enAndExVehWriter1 = new EntryAndExitVehicleWriter(entitydb, "Vehicle");
        EntryAndExitVehicleWriter enAndExVehWriter2 = new EntryAndExitVehicleWriter(entitydb, "Vehicle");
        GantryVehicleWriter ganVehWriter = new GantryVehicleWriter(entitydb, "Vehicle");
        GantryWriter ganWriter = new GantryWriter(entitydb, "Gantry");
        TranToVehDigTwinWriter tranToVehDigTwinWriter1 = new TranToVehDigTwinWriter(digitaldb, "VehicleDigital");
        TranToVehDigTwinWriter tranToVehDigTwinWriter2 = new TranToVehDigTwinWriter(digitaldb, "VehicleDigital");
        TranToVehDigTwinWriter tranToVehDigTwinWriter3 = new TranToVehDigTwinWriter(digitaldb, "VehicleDigital");
        VehToVehDigTwinWriter vehToVehDigTwinWriter = new VehToVehDigTwinWriter(digitaldb, "VehicleDigital");
        GanToGanDigTwinWriter ganToGanDigTwinWriter = new GanToGanDigTwinWriter(digitaldb, "GantryDigital");





        RabbitmqConsumerThread Rct1 = new RabbitmqConsumerThread("ENStationRecord", prefetch, enRecWriter);

        RabbitmqConsumerThread Rct2 = new RabbitmqConsumerThread("GantryRecord", prefetch, ganRecWriter1);
        RabbitmqConsumerThread Rct3 = new RabbitmqConsumerThread("GantryRecord", prefetch, ganRecWriter2);
        RabbitmqConsumerThread Rct4 = new RabbitmqConsumerThread("GantryRecord", prefetch, ganRecWriter3);

        RabbitmqConsumerThread Rct5 = new RabbitmqConsumerThread("ExitStationRecord", prefetch, exRecWriter);


        RabbitmqConsumerThread Rct6 = new RabbitmqConsumerThread("ENVehicleRecord", prefetch, enAndExVehWriter1);
        RabbitmqConsumerThread Rct7 = new RabbitmqConsumerThread("ExitVehicleInfo", prefetch, enAndExVehWriter2);
        RabbitmqConsumerThread Rct8 = new RabbitmqConsumerThread("GantryVehicleRecord", prefetch, ganVehWriter);
        RabbitmqConsumerThread Rct9 = new RabbitmqConsumerThread("GantryInfo", prefetch, ganWriter);
        RabbitmqConsumerThread Rct10 = new RabbitmqConsumerThread("Tran2VehDigital", prefetch, tranToVehDigTwinWriter1);
        RabbitmqConsumerThread Rct11 = new RabbitmqConsumerThread("Tran2VehDigital", prefetch, tranToVehDigTwinWriter2);
        RabbitmqConsumerThread Rct12 = new RabbitmqConsumerThread("Tran2VehDigital", prefetch, tranToVehDigTwinWriter3);
        RabbitmqConsumerThread Rct13 = new RabbitmqConsumerThread("Veh2VehDigital", prefetch, vehToVehDigTwinWriter);
        RabbitmqConsumerThread Rct14 = new RabbitmqConsumerThread("Gan2GanDigital", prefetch, ganToGanDigTwinWriter);


        // Next, construct a thread from that object.
        Thread RThrd1 = new Thread(Rct1);
        Thread RThrd2 = new Thread(Rct2);
        Thread RThrd3 = new Thread(Rct3);
        Thread RThrd4 = new Thread(Rct4);
        Thread RThrd5 = new Thread(Rct5);
        Thread RThrd6 = new Thread(Rct6);
        Thread RThrd7 = new Thread(Rct7);
        Thread RThrd8 = new Thread(Rct8);
        Thread RThrd9 = new Thread(Rct9);
        Thread RThrd10 = new Thread(Rct10);
        Thread RThrd11 = new Thread(Rct11);
        Thread RThrd12 = new Thread(Rct12);
        Thread RThrd13 = new Thread(Rct13);
        Thread RThrd14 = new Thread(Rct14);


        // Finally, start execution of the thread.
        RThrd1.start();
        RThrd2.start();
        RThrd3.start();
        RThrd4.start();
        RThrd5.start();
        RThrd6.start();
        RThrd7.start();
        RThrd8.start();
        RThrd9.start();
        RThrd10.start();
        RThrd11.start();
        RThrd12.start();
        RThrd13.start();
        RThrd14.start();

        while(true) {
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }



    }


}
