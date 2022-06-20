package com.nju.ics.utils;

import com.alibaba.fastjson.JSON;

import org.apache.flink.api.common.serialization.SerializationSchema;

public class ObjectSerializationSchema implements SerializationSchema{

    @Override
    public byte[] serialize(Object element) {
        // TODO Auto-generated method stub
        return JSON.toJSONBytes(element);
    }
    
}
