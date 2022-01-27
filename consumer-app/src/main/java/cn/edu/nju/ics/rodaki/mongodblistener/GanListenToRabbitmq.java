package cn.edu.nju.ics.rodaki.mongodblistener;

import cn.edu.nju.ics.rodaki.mongodbwriter.MongodbClient;
import cn.edu.nju.ics.rodaki.rabbitmq.RabbitmqPublisher;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class GanListenToRabbitmq implements MongodbListener{


    private MongoClient mongoClient;
    private MongoDatabase entitydatabase;
    private MongoCollection<Document> gantry;
    private ChangeStreamIterable<Document> changeStream;



    List<Bson> pipeline;
    Document eventBody;
    RabbitmqPublisher ganPublisher;

    public GanListenToRabbitmq(String entitydb, String gantry) throws IOException, TimeoutException {
        this.mongoClient = new MongodbClient().getMongoClient();
        this.entitydatabase = mongoClient.getDatabase(entitydb);
        this.gantry = entitydatabase.getCollection(gantry);

        this.ganPublisher = new RabbitmqPublisher("Gan2GanDigital");
    }



    @Override
    public void startListen() {
        pipeline = Arrays.asList(
                Aggregates.match(
                        Filters.in("operationType",
                                Arrays.asList("insert", "update"))));
        this.changeStream = this.gantry.watch(pipeline)
                .fullDocument(FullDocument.UPDATE_LOOKUP);


        this.changeStream.forEach(event -> {


            eventBody = event.getFullDocument();



            try {
                ganPublisher.pushMassage(new Document()
                        .append("GANTRYID", eventBody.get("_id"))
                        .append("LONGITUDE", eventBody.get("LONGITUDE"))
                        .append("LATITUDE", eventBody.get("LATITUDE"))
                        .append("GANTRYTYPE", eventBody.get("GANTRYTYPE"))
                        .append("GANTRYNAME", eventBody.get("GANTRYNAME")).toJson());
            } catch (IOException e) {
                e.printStackTrace();
            }



        });
    }




}
