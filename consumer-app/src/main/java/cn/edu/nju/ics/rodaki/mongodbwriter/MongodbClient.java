package cn.edu.nju.ics.rodaki.mongodbwriter;

import com.mongodb.client.*;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesGranularity;
import com.mongodb.client.model.TimeSeriesOptions;


public class MongodbClient {
    private MongoClient mongoClient;

    public MongodbClient() {

            this.mongoClient = MongoClients.create("mongodb://mymongodb:27017");

    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }



    public void createTimeSeriesCollection(String db, String col, String metaData, String timeField, String granularity){
        try{

            MongoDatabase database = this.mongoClient.getDatabase(db);
            TimeSeriesOptions tsOptions = new TimeSeriesOptions(timeField);
            tsOptions.metaField(metaData);
            if(granularity == "hours"){
                tsOptions.granularity(TimeSeriesGranularity.HOURS);
            } else if (granularity == "minutes"){
                tsOptions.granularity(TimeSeriesGranularity.MINUTES);
            } else if (granularity == "seconds"){
                tsOptions.granularity(TimeSeriesGranularity.SECONDS);
            } else {
                throw new Exception("granularity must be one of [hours, minutes, seconds]!");
            }

//            System.out.println(tsOptions.getTimeField());
//            System.out.println(tsOptions.getGranularity());
//            System.out.println(tsOptions.getMetaField());

            CreateCollectionOptions collOptions = new CreateCollectionOptions().timeSeriesOptions(tsOptions);
            database.createCollection(col, collOptions);
        } catch (Exception e){
            System.out.println("create timeseries collection failed!");
        }
    }

}
