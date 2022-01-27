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
import java.util.Date;

public class ExitRecordWriter implements MongodbWriter {


    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> exCol;
    MongoCollection<Document> traTran;

    List<WriteModel<Document>> exRecBulkOperations = new ArrayList<>();
    List<WriteModel<Document>> traTranBulkOperations = new ArrayList<>();
    BulkWriteOptions bulkOptions = new BulkWriteOptions().ordered(false);

    Bson filter;
    Bson update;
    UpdateOptions options = new UpdateOptions().upsert(true);
    Document exitRecord;

    public ExitRecordWriter(String db, String exCol, String traTran) {

        this.mongoClient = new MongodbClient().getMongoClient();
        this.database = mongoClient.getDatabase(db);
        this.exCol = database.getCollection(exCol);
        this.traTran = database.getCollection(traTran);


    }


    @Override
    public void insertData(JSONObject obj) {


        Date date = new Date(obj.getLong("EXTIME"));

        exRecBulkOperations.add(new InsertOneModel<>(new Document()
                .append("_id", obj.get("EXITID"))
                .append("TIMESTRING", date)
                .append("TIME", obj.getLong("EXTIME"))
                .append("PID", obj.get("PASSID"))
                .append("SID", obj.get("EXTOLLSTATIONID"))
                .append("VID", obj.get("VEHICLEID"))));


        if (exRecBulkOperations.size() >= 500) {
            try {
                exCol.bulkWrite(exRecBulkOperations,bulkOptions);
                exRecBulkOperations.clear();
            } catch (MongoBulkWriteException e) {
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }
        }


        exitRecord = new Document().append("SID", obj.get("EXTOLLSTATIONID"))
                .append("TIME", obj.get("EXTIME"))
                .append("STYPE", 3)
                .append("LOCATION", new ArrayList(List.of(new BigDecimal[]{(BigDecimal) obj.get("LATITUDE"), (BigDecimal) obj.get("LONGITUDE")})));


        filter = Filters.eq("_id", obj.get("PASSID"));
        update = Updates.combine(Updates.set("VEHICLEID", obj.get("VEHICLEID")),
                Updates.set("MEDIATYPE", obj.get("MEDIATYPE")),
                Updates.set("MEDIAID", obj.get("MEDIAID")),
                Updates.set("EXIDENTIFY", obj.get("EXIDENTIFY")),
                Updates.addToSet("STATIONINFO", exitRecord));

        traTranBulkOperations.add(new UpdateOneModel<>(filter, update, options));

        if (traTranBulkOperations.size() >= 500) {
            try {
                traTran.bulkWrite(traTranBulkOperations,bulkOptions);
                traTranBulkOperations.clear();
            } catch (MongoBulkWriteException e) {
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }
        }

    }






}

