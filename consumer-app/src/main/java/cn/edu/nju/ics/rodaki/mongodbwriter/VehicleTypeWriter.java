package cn.edu.nju.ics.rodaki.mongodbwriter;

import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqPublisher;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class VehicleTypeWriter implements MongodbWriter {


    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> vehType;

    List<WriteModel<Document>> vehTypeBulkOperations = new ArrayList<>();
    BulkWriteOptions bulkOptions = new BulkWriteOptions().ordered(false);

    Bson filter;
    Bson update;
    UpdateOptions options = new UpdateOptions().upsert(true);

    String vehicleId;
    int vehicleType;
    String transPayType;
    int trueVehicleType;

    RabbitmqPublisher vehTypePublisher;
    Map m1;

    public VehicleTypeWriter(String db, String vehCol) throws IOException, TimeoutException {

        this.mongoClient = new MongodbClient().getMongoClient();
        this.database = mongoClient.getDatabase(db);
        this.vehType = database.getCollection(vehCol);

        this.vehTypePublisher = new RabbitmqPublisher("ErrorVehicleType");

        m1 = new HashMap();

//        输出到文件，添加列名
//        writeResult("./ErrorVehicleType.txt","PASSID,VEHICLEID,VEHICLETYPE,TRUEVEHICLETYPE,EXITID\n");

    }


    @Override
    public void insertData(JSONObject obj) {




        vehicleId = obj.getString("VEHICLEID");
        vehicleType = obj.getInteger("VEHICLETYPE");
        transPayType = obj.getString("TRANSPAYTYPE");

//      判断 TRANSPAYTYPE, 如果不是 1，则为人工通道，将该记录中的车型作为该 VEHICLEID 的真实车型，存入数据库
        if (!(transPayType.equals("1"))) {
            filter = Filters.eq("_id", vehicleId);
            update = Updates.combine(Updates.set("VEHICLETYPE", vehicleType),
                    Updates.set("TRANSPAYTYPE", transPayType));
            vehTypeBulkOperations.add(new UpdateOneModel<>(filter, update, options));

            if (vehTypeBulkOperations.size() >= 500) {
                try {
                    vehType.bulkWrite(vehTypeBulkOperations,bulkOptions);
                    vehTypeBulkOperations.clear();
                } catch (MongoBulkWriteException e) {
                    System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
                }
            }

//      判断 TRANSPAYTYPE, 如果是 1， 则认为不是人工通道，对比当前车型与数据库中真实车型，若当前车型小于真实车型，则发送消息到 rabbitmq 队列
        } else {
            m1.put(vehicleId, vehicleType);
            if (m1.size() >= 200) {
                Bson filter = Filters.in("_id", m1.keySet());
                vehType.find(filter).forEach(doc -> {
                    trueVehicleType = doc.getInteger("VEHICLETYPE");


                    if ((int) m1.get(doc.getString("_id")) < trueVehicleType) {
//                      发送消息到 rabbitmq 队列
                        try {
                            vehTypePublisher.pushMassage(new Document()
                                    .append("PASSID", obj.get("PASSID"))
                                    .append("VEHICLEID", vehicleId)
                                    .append("VEHICLETYPE", vehicleType)
                                    .append("TRUEVEHICLETYPE", trueVehicleType)
                                    .append("EXITID", obj.get("EXITID")).toJson());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

//                        输出到文件
//                        writeResult("./ErrorVehicleType.txt",obj.get("PASSID").toString() +','+ vehicleId +','+ vehicleType +','+ trueVehicleType  +','+ obj.get("EXITID").toString() + '\n');
                    }

                });

                m1.clear();

            }


        }


    }


    public void writeResult(String filePath, String content) {


        File thisFile = new File(filePath);
        try

        {
            if (!thisFile.exists()) {
                thisFile.createNewFile();
            }
            FileWriter fw = new FileWriter(filePath, true);
            fw.write(content);
            fw.close();
        } catch(
                IOException e)

        {
            e.printStackTrace();
        }

    }




}
