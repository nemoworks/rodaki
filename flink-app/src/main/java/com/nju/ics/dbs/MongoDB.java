package com.nju.ics.dbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.connection.ClusterSettings;

import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static java.util.Arrays.asList;
import com.mongodb.connection.ConnectionPoolSettings;
import com.nju.ics.utils.ConfigureENV;

public class MongoDB {
    public static CodecRegistry pojoCodecRegistry = fromRegistries(MongoClients.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().register("com.nju.ics.Models").automatic(true).build()));

    public static MongoClient getClient() {

        // 通过连接认证获取MongoDB连接

        MongoCredential credential = MongoCredential.createScramSha256Credential(
                ConfigureENV.prop.getProperty("mongo.user"), ConfigureENV.prop.getProperty("mongo.authdb"),
                ConfigureENV.prop.getProperty("mongo.password").toCharArray());
        // MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
        // .applyToConnectionPoolSettings(builder ->
        // builder.applySettings(ConnectionPoolSettings.builder()))
        // .applyToClusterSettings(
        // builder -> builder.hosts(asList(new
        // ServerAddress(ConfigureENV.prop.getProperty("mongo.host"),
        // Integer.parseInt(ConfigureENV.prop.getProperty("mongo.port")
        // )
        // ))))
        // .credential(credential).build());

        MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%s@%s/?authSource=%s",
                ConfigureENV.prop.getProperty("mongo.user"), ConfigureENV.prop.getProperty("mongo.password"),
                ConfigureENV.prop.getProperty("mongo.host"), ConfigureENV.prop.getProperty("mongo.authdb")));
        return mongoClient;

    }

    public static Map getDBConnectConfig() {
        Map config = new HashMap<String, String>();
        config.put("user", ConfigureENV.prop.getProperty("mongo.user"));
        config.put("password", ConfigureENV.prop.getProperty("mongo.password"));
        config.put("host", ConfigureENV.prop.getProperty("mongo.host"));
        config.put("port",ConfigureENV.prop.getProperty("mongo.port"));
        config.put("authdb", ConfigureENV.prop.getProperty("mongo.authdb"));
        config.put("db", ConfigureENV.prop.getProperty("mongo.db"));
        return config;
    }
    public static Map getDBConnectConfig(String connectionName) {
        Map config = new HashMap<String, String>();
        config.put("user", ConfigureENV.prop.getProperty("mongo.user"));
        config.put("password", ConfigureENV.prop.getProperty("mongo.password"));
        config.put("host", ConfigureENV.prop.getProperty("mongo.host"));
        config.put("port",ConfigureENV.prop.getProperty("mongo.port"));
        config.put("authdb", ConfigureENV.prop.getProperty("mongo.authdb"));
        config.put("db", ConfigureENV.prop.getProperty("mongo.db"));
        config.put("connectionName", connectionName);
        return config;
    }
    public static MongoDatabase getDBConnect(MongoClient client, String db) {
        return client.getDatabase(db).withCodecRegistry(pojoCodecRegistry);
    }

    public static MongoCollection getCollectionConnect(MongoDatabase database, String collection) {
        return database.getCollection(collection);
    }

    public static MongoCollection getCollectionConnect(MongoDatabase database, String collection, Class type) {
        return database.getCollection(collection, type);
    }
}
