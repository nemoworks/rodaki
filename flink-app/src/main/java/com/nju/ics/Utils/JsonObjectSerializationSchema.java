package com.nju.ics.Utils;

import java.io.IOException;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import com.alibaba.fastjson.JSONObject;

public class JsonObjectSerializationSchema implements SerializationSchema<JSONObject>{

    @Override
    public byte[] serialize(JSONObject element) {
        // TODO Auto-generated method stub
        return JSONObject.toJSONBytes(element);
    }
    
}
