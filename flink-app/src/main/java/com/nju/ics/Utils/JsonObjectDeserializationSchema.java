package com.nju.ics.Utils;

import java.io.IOException;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import com.alibaba.fastjson.JSONObject;

public class JsonObjectDeserializationSchema implements DeserializationSchema<JSONObject>{


    @Override
    public TypeInformation<JSONObject> getProducedType() {
        // TODO Auto-generated method stub
        return TypeInformation.of(JSONObject.class);
    }

    @Override
    public JSONObject deserialize(byte[] message) throws IOException {
        // TODO Auto-generated method stub
        return JSONObject.parseObject(new String(message,"UTF-8"));
    }

    @Override
    public boolean isEndOfStream(JSONObject nextElement) {
        // TODO Auto-generated method stub
        return false;
    }
    
}
