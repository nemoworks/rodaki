package cn.edu.nju.ics.rodaki.mongodblistener;

import cn.edu.nju.ics.rodaki.mongodbwriter.MongodbClient;
import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqPublisher;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class TranListenToRabbitmq implements MongodbListener {

    private MongoClient mongoClient;
    private MongoDatabase entitydatabase;
    private MongoCollection<Document> trafficTransactionCol;

    private ChangeStreamIterable<Document> changeStream;

    RabbitmqPublisher tranPublisher;


    Document eventBody;

    public TranListenToRabbitmq(String entitydb, String trafficTransactionCol) throws IOException, TimeoutException {
        this.mongoClient = new MongodbClient().getMongoClient();
        this.entitydatabase = mongoClient.getDatabase(entitydb);
        this.trafficTransactionCol = entitydatabase.getCollection(trafficTransactionCol);

        this.tranPublisher = new RabbitmqPublisher("Tran2VehDigital");

    }



    @Override
    public void startListen() {


        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(
                        Filters.in("operationType",
                                Arrays.asList("insert", "update"))));
        this.changeStream = this.trafficTransactionCol.watch(pipeline)
                .fullDocument(FullDocument.UPDATE_LOOKUP);


        this.changeStream.forEach(event -> {


            eventBody = event.getFullDocument();




            try {
                tranPublisher.pushMassage(new Document()
                        .append("PASSID", eventBody.get("_id"))
                        .append("ENIDENTIFY", eventBody.get("ENIDENTIFY"))
                        .append("MEDIAID", eventBody.get("MEDIAID"))
                        .append("MEDIATYPE", eventBody.get("MEDIATYPE"))
                        .append("STATIONINFO", eventBody.get("STATIONINFO"))
                        .append("VEHICLEID",  eventBody.get("VEHICLEID"))
                        .append("EXIDENTIFY", eventBody.get("EXIDENTIFY")).toJson());
            } catch (IOException e) {
                e.printStackTrace();
            }



        });


    }








}
