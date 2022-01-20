package cn.edu.nju.ics.rodaki.mongodbwriter;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


public interface MongodbWriter {
    MongoClient mongoClient = null;
    MongoDatabase database = null;
    MongoCollection<Document> collection = null;

    public void insertData(JSONObject obj);






}
