package com.nju.ics.Connectors;

import java.util.Collections;
import java.util.Map;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import com.nju.ics.DBs.MongoDB;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoDBAsyncSink extends RichAsyncFunction<String, String> {
    private Map<String, String> connectionStr;

    MongoClient mongoClient = null;
    MongoDatabase mongodatabase = null;
    MongoCollection<Document> mongoCollection = null;
    String collectionName;
    //ReplaceOptions options = new ReplaceOptions().upsert(true);

    public MongoDBAsyncSink(Map connectionconfig, String collectionName) {
        this.connectionStr = connectionconfig;
        this.collectionName = collectionName;
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        if (this.connectionStr.get("user") == null) {
            mongoClient = MongoClients.create(
                    String.format("mongodb://%s:%s", this.connectionStr.get("host"), this.connectionStr.get("port")));
        } else {
            mongoClient = MongoClients.create(String.format("mongodb://%s:%s@%s:%s/?authSource=%s",
                    this.connectionStr.get("user"), this.connectionStr.get("password"), this.connectionStr.get("host"),
                    this.connectionStr.get("port"), this.connectionStr.get("authdb")));
        }

        mongodatabase = MongoDB.getDBConnect(mongoClient, this.connectionStr.get("db"));
        mongoCollection = MongoDB.getCollectionConnect(mongodatabase, this.collectionName);
    }

    @Override
    public void asyncInvoke(String input, ResultFuture<String> resultFuture) throws Exception {
        // TODO Auto-generated method stub
        Document doc= Document.parse(input);
        Bson filter = Filters.eq("_id", doc.get("_id"));
        mongoCollection.replaceOne(filter, doc, new ReplaceOptions().upsert(true),  new SingleResultCallback<UpdateResult>() {

            @Override
            public void onResult(UpdateResult result, Throwable t) {
                // TODO Auto-generated method stub
                resultFuture.complete(Collections.emptyList());
                //System.out.println(t);
            }
            
        });
    }

}
