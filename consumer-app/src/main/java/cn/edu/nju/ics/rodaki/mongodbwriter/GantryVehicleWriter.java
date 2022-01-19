package cn.edu.nju.ics.rodaki.mongodbwriter;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class GantryVehicleWriter implements MongodbWriter {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;
    List<WriteModel<Document>> BulkOperations = new ArrayList<>();

    Bson filter;
    Bson update;
    UpdateOptions options = new UpdateOptions().upsert(true);

    public GantryVehicleWriter(String db, String col) {
        this.mongoClient = new MongodbClient().getMongoClient();
        this.database = mongoClient.getDatabase(db);
        this.collection = database.getCollection(col);
    }

    @Override
    public void insertData(JSONObject obj) {


        filter = Filters.eq("_id", obj.get("VEHICLEID"));

        update = Updates.combine(Updates.set("VEHICLESEAT", obj.get("VEHICLESEAT")),
                Updates.set("VEHICLELENGTH", obj.get("VEHICLELENGTH")),
                Updates.set("VEHICLEWIDTH", obj.get("VEHICLEWIDTH")),
                Updates.set("VEHICLEHIGHT", obj.get("VEHICLEHIGHT")),
                Updates.set("AXLECOUNT", obj.get("AXLECOUNT")));

        BulkOperations.add(new UpdateOneModel<>(filter, update, options));

        if (BulkOperations.size() >= 900){
            try {
                collection.bulkWrite(BulkOperations);
                BulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }
        }

    }


    public void writerExit(){
        if(BulkOperations.size()!=0){
            try {
                collection.bulkWrite(BulkOperations);
                BulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }

        }

    }


}
