package cn.edu.nju.ics.rodaki.mongodbwriter;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GantryRecordWriter implements MongodbWriter {

    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> ganCol;
    MongoCollection<Document> traTran;

    List<WriteModel<Document>> ganRecBulkOperations = new ArrayList<>();
    List<WriteModel<Document>> traTranBulkOperations = new ArrayList<>();


    Bson filter;
    Bson update;
    UpdateOptions options = new UpdateOptions().upsert(true);
    Document gantryRecord;

    public GantryRecordWriter(String db, String ganCol, String traTran) {

        this.mongoClient = new MongodbClient().getMongoClient();
        this.database = mongoClient.getDatabase(db);
        this.ganCol = database.getCollection(ganCol);
        this.traTran = database.getCollection(traTran);


    }



    @Override
    public void insertData(JSONObject obj) {




        ganRecBulkOperations.add(new InsertOneModel<>(new Document()
                .append("_id", obj.get("TRADEID"))
                .append("TIME", obj.getLong("TRANSTIME"))
                .append("PID", obj.get("PASSID"))
                .append("SID", obj.get("GANTRYID"))
                .append("VID", obj.get("VEHICLEID"))));


        if (ganRecBulkOperations.size() >= 500){
            try {
                ganCol.bulkWrite(ganRecBulkOperations);
                ganRecBulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }
        }



        gantryRecord = new Document().append("SID", obj.get("GANTRYID"))
                .append("TIME", obj.get("TRANSTIME"))
                .append("STYPE", 2)
                .append("SPECIALTYPE", obj.get("SPECIALTYPE"))
                .append("ORIGINALFLAG", obj.get("ORIGINALFLAG"))
                .append("GANTRYPOSITIONFLAG", obj.get("GANTRYPOSITIONFLAG"))
                .append("LOCATION",  new ArrayList(List.of(new BigDecimal[]{(BigDecimal) obj.get("LATITUDE"), (BigDecimal) obj.get("LONGITUDE")}))
                );



        filter = Filters.eq("_id", obj.get("PASSID"));
        update = Updates.combine(Updates.set("VEHICLEID", obj.get("VEHICLEID")),
                Updates.set("MEDIATYPE", obj.get("MEDIATYPE")),
                Updates.set("MEDIAID", obj.get("MEDIAID")),
                Updates.addToSet("STATIONINFO", gantryRecord));



        traTranBulkOperations.add(new UpdateOneModel<>(filter, update, options));




        if (traTranBulkOperations.size() >= 500){
            try {
                traTran.bulkWrite(traTranBulkOperations);
                traTranBulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }
        }


    }

    public void writerExit(){

        if(ganRecBulkOperations.size()!=0){
            try {

                ganCol.bulkWrite(ganRecBulkOperations);
                ganRecBulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }

        }
        if(traTranBulkOperations.size()!=0){
            try {
                traTran.bulkWrite(traTranBulkOperations);
                traTranBulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }

        }
    }


    }
