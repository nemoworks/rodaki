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

public class VehListenToRabbitmq implements MongodbListener{



    private MongoClient mongoClient;
    private MongoDatabase entitydatabase;
    private MongoCollection<Document> vehicle;
    private ChangeStreamIterable<Document> changeStream;


    List<Bson> pipeline;
    Document eventBody;
    RabbitmqPublisher vehPublisher;

    public VehListenToRabbitmq(String entitydb, String vehicle) throws IOException, TimeoutException {
        this.mongoClient = new MongodbClient().getMongoClient();
        this.entitydatabase = mongoClient.getDatabase(entitydb);
        this.vehicle = entitydatabase.getCollection(vehicle);

        this.vehPublisher = new RabbitmqPublisher("Veh2VehDigital");
    }



    @Override
    public void startListen() {
        pipeline = Arrays.asList(
                Aggregates.match(
                        Filters.in("operationType",
                                Arrays.asList("insert", "update"))));
        this.changeStream = this.vehicle.watch(pipeline)
                .fullDocument(FullDocument.UPDATE_LOOKUP);


        this.changeStream.forEach(event -> {

            eventBody = event.getFullDocument();



            try {
                vehPublisher.pushMassage(new Document()
                        .append("VEHICLEID", eventBody.get("_id"))
                        .append("AXLECOUNT", eventBody.get("AXLECOUNT"))
                        .append("VEHICLEHIGHT", eventBody.get("VEHICLEHIGHT"))
                        .append("VEHICLELENGTH", eventBody.get("VEHICLELENGTH"))
                        .append("VEHICLESEAT", eventBody.get("VEHICLESEAT"))
                        .append("VEHICLEWIDTH", eventBody.get("VEHICLEWIDTH"))
                        .append("VEHICLETYPE", eventBody.get("VEHICLETYPE"))
                        .append("LIMITWEIGHT", eventBody.get("LIMITWEIGHT"))
                        .append("AXISINFO", eventBody.get("AXISINFO")).toJson());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

}
