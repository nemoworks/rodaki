package cn.edu.nju.ics.rodaki.servicethread;

import cn.edu.nju.ics.rodaki.mongodbwriter.MongodbClient;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.SimpleDateFormat;
import java.util.*;








public class CalTrafficFlow extends TimerTask{

    private MongoClient mongoClient;
    private MongoDatabase fromdb;
    private MongoDatabase todb;
    private MongoCollection<Document> fromcol;
    private MongoCollection<Document> tocol;
    long timespan;
    long frequency;
    List<WriteModel<Document>> BulkOperations = new ArrayList<>();
    long sTime;
    long eTime;
    MongoCursor<Document> cursor;
    Document item_doc;
    AggregateIterable<Document> gantryIDs;
    AggregateIterable<Document> trafficFlows;

    Dictionary<String, Integer> dict = new Hashtable<String, Integer>();
    Iterator<String> keys;
    String key;
    int allZeroFlag;
    int trafficFlow;
    SimpleDateFormat sdf;


    public CalTrafficFlow(long startTime, long timespan, long frequency){
        this.mongoClient = new MongodbClient().getMongoClient();
        this.fromdb = mongoClient.getDatabase("EntityModel");
        this.fromcol = this.fromdb.getCollection("GantryRecord");

        this.todb = mongoClient.getDatabase("TestDigitalModel");
        this.tocol = this.todb.getCollection("GantryDigital");



        this.timespan = timespan;
        this.sTime = startTime;
        this.frequency = frequency;

        this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    }
    @Override
    public void run() {
        this.allZeroFlag = 0;
        System.out.println("start timer task " + this.sdf.format(new Date()));

        // 从 GantryDigital 中获取 gantryID 列表，为每个门架的 trafficFlow 赋默认值 0
        gantryIDs = this.tocol.aggregate(
                Arrays.asList(
                        Aggregates.project(Projections.fields(Projections.include("_id"))),
                        Aggregates.addFields(new Field("trafficFlow",0)))
        );

        cursor = gantryIDs.iterator();
        while(cursor.hasNext()) {
            item_doc = cursor.next();
            dict.put(item_doc.get("_id").toString(), (Integer) item_doc.get("trafficFlow"));
        }




        eTime = sTime + timespan;


        // 从 GantryRecord 中统计每个门架在某一时间段的车流量，写入每个门架的 trafficFlow
        trafficFlows = this.fromcol.aggregate(
                Arrays.asList(
                        Aggregates.project(Projections.fields(Projections.include("_id"),
                                Projections.include("SID"),
                                Projections.include("TIME"))),
                        Aggregates.match(Filters.and(Filters.gt("TIME", sTime),Filters.lte("TIME", eTime))),
                        Aggregates.group("$SID", Accumulators.sum("trafficFlow", 1))

                ));


        cursor = trafficFlows.iterator();
        while(cursor.hasNext()) {
            item_doc = cursor.next();
            dict.put(item_doc.get("_id").toString(), (Integer) item_doc.get("trafficFlow"));
        }



        // 将每个门架的车流加入 bulkwrite 列表
        keys = dict.keys().asIterator();

        while (keys.hasNext()){

            key = keys.next();
            trafficFlow = dict.get(key);
            if(trafficFlow!=0){
                allZeroFlag = 1;
            }

            Bson filter = Filters.eq("_id", key);
            Bson update = Updates.combine(Updates.set("TIME", eTime),
                    Updates.set("TRAFFICFLOW", trafficFlow),
                    Updates.push("TRAFFICFLOWHISTORY",
                            new ArrayList<>(){{add(trafficFlow);add(eTime);}}));

            UpdateOptions options = new UpdateOptions().upsert(true);
            BulkOperations.add(new UpdateOneModel<>(filter, update, options));

        }





        // 如果所有门架的车流量都是0，即没有新的门架记录数据，拒绝写入，结束循环，如果有非零元素则写入，之后睡眠 frequency 时间
        if (allZeroFlag == 0){


            BulkOperations.clear();

            try {
                System.out.println("No records in (" +  sTime + "," + eTime + "]" + ", (" + sdf.format(sTime) + "," + sdf.format(eTime) + "] !");
                Thread.sleep(600*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } else {

            try {
                tocol.bulkWrite(BulkOperations);
                BulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }


            System.out.println(sdf.format(new Date()) + ": append trafficflow in (" + sTime + "," + eTime + "]" + ", (" + sdf.format(sTime) + "," + sdf.format(eTime) + "]");


            // 将每个门架的车流置重新置为0
            keys = dict.keys().asIterator();
            while (keys.hasNext()){
                key = keys.next();
                dict.put(key, 0);
            }

            sTime += timespan;

        }



    }
}
