package com.nju.ics.connectors;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nju.ics.dbs.CouchDB;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

public class CouchDBSink extends RichSinkFunction<String> {
    CouchDbClient dbClient;
    String properties;
    public CouchDBSink(CouchDbProperties properties){
        this.properties=JSON.toJSONString(properties) ;
    }
    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        super.close();
        if (dbClient != null) {
            dbClient.shutdown();
            dbClient.close();
        }
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        // TODO Auto-generated method stub
        super.open(parameters);
        dbClient = new CouchDbClient(JSON.parseObject(this.properties,CouchDbProperties.class));
    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        // TODO Auto-generated method stub
        JSONObject json = null;
        Map m=JSON.toJavaObject(JSON.parseObject(value),Map.class);
        try{
            json=dbClient.find(JSONObject.class, m.get("_id").toString());
        }
        catch (Exception e){
            json=null;
        }
       
        if (json==null){
            dbClient.save(m);
        }
        else{
            m.put("_rev", json.getString("_rev"));
            dbClient.update(m);
        }
        
    }

}
