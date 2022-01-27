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
import java.util.Date;
import java.util.List;
import java.util.Date;

public class EntryRecordWriter implements MongodbWriter {

    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> enCol;
    MongoCollection<Document> traTran;

    List<WriteModel<Document>> enRecBulkOperations = new ArrayList<>();
    List<WriteModel<Document>> traTranBulkOperations = new ArrayList<>();
    BulkWriteOptions bulkOptions = new BulkWriteOptions().ordered(false);

    Bson filter;
    Bson update;
    UpdateOptions options = new UpdateOptions().upsert(true);
    Document entryRecord;

    public EntryRecordWriter(String db, String enCol, String traTran) {

        this.mongoClient = new MongodbClient().getMongoClient();
        this.database = mongoClient.getDatabase(db);
        this.enCol = database.getCollection(enCol);
        this.traTran = database.getCollection(traTran);


    }

    @Override
    public void insertData(JSONObject obj) {
        Date date = new Date(obj.getLong("ENTIME"));

        enRecBulkOperations.add(new InsertOneModel<>(new Document()
                .append("_id", obj.get("ENTRYID"))
                .append("TIMESTRING", date)
                .append("TIME", obj.getLong("ENTIME"))
                .append("PID", obj.get("PASSID"))
                .append("SID", obj.get("ENTOLLSTATIONID"))
                .append("VID", obj.get("VEHICLEID"))));

        if (enRecBulkOperations.size() >= 500){
            try {
                enCol.bulkWrite(enRecBulkOperations,bulkOptions);
                enRecBulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }
        }



        entryRecord = new Document().append("SID", obj.get("ENTOLLSTATIONID"))
                .append("TIME", obj.get("ENTIME"))
                .append("STYPE", 1)
                .append("LOCATION",new ArrayList(List.of(new BigDecimal[]{(BigDecimal) obj.get("LATITUDE"), (BigDecimal) obj.get("LONGITUDE")})) );



        filter = Filters.eq("_id", obj.get("PASSID"));
        update = Updates.combine(Updates.set("VEHICLEID", obj.get("VEHICLEID")),
                Updates.set("MEDIATYPE", obj.get("MEDIATYPE")),
                Updates.set("MEDIAID", obj.get("MEDIAID")),
                Updates.set("ENIDENTIFY", obj.get("ENIDENTIFY")),
                Updates.addToSet("STATIONINFO", entryRecord));

        traTranBulkOperations.add(new UpdateOneModel<>(filter, update, options));



        if (traTranBulkOperations.size() >= 500){
            try {
                traTran.bulkWrite(traTranBulkOperations,bulkOptions);
                traTranBulkOperations.clear();
            } catch (MongoBulkWriteException e){
                System.out.println("A MongoBulkWriteException occured with the following message: " + e.getMessage());
            }
        }



    }



}
