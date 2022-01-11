package com.nju.ics.DBs;

import com.nju.ics.Utils.ConfigureENV;

import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

public class CouchDB {
        public static CouchDbProperties getClient() {
               
                CouchDbProperties properties = new CouchDbProperties()
                                .setDbName(ConfigureENV.prop.getProperty("couchdb.name"))
                                .setCreateDbIfNotExist(Boolean.parseBoolean(
                                                ConfigureENV.prop.getProperty("couchdb.createdb.if-not-exist")))
                                .setProtocol(ConfigureENV.prop.getProperty("couchdb.protocol"))
                                .setHost(ConfigureENV.prop.getProperty("couchdb.host"))
                                .setPort(Integer.parseInt(ConfigureENV.prop.getProperty("couchdb.port")))
                                .setUsername(ConfigureENV.prop.getProperty("couchdb.username"))
                                .setPassword(ConfigureENV.prop.getProperty("couchdb.password"))
                                .setMaxConnections(Integer
                                                .parseInt(ConfigureENV.prop.getProperty("couchdb.max.connections")))
                                .setConnectionTimeout(Integer.parseInt(
                                                ConfigureENV.prop.getProperty("couchdb.http.connection.timeout")))
                                .setSocketTimeout(Integer.parseInt(
                                                ConfigureENV.prop.getProperty("couchdb.http.socket.timeout")));
                // CouchDbClient dbClient = new
                // CouchDbClient(ConfigureENV.prop.getProperty("couchdb.name"),
                // Boolean.parseBoolean(ConfigureENV.prop.getProperty("couchdb.createdb.if-not-exist")),
                // ConfigureENV.prop.getProperty("couchdb.protocol"),
                // ConfigureENV.prop.getProperty("couchdb.host"),
                // Integer.parseInt(ConfigureENV.prop.getProperty("couchdb.port")),
                // ConfigureENV.prop.getProperty("couchdb.username"),
                // ConfigureENV.prop.getProperty("couchdb.password"));
                return properties;
        }
}
