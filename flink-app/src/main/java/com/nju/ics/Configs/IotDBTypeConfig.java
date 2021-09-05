package com.nju.ics.Configs;
import org.apache.iotdb.tsfile.file.metadata.enums.TSEncoding;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.apache.iotdb.tsfile.file.metadata.enums.CompressionType;

public class IotDBTypeConfig {
    public static  TSEncoding TSDefaultEncoding=TSEncoding.PLAIN;
    public static CompressionType TSDefaultCompressionType=CompressionType.SNAPPY;

    public static Map<String,String> TSDataTYpeMap= new HashMap<String,String>(){{
            put("boolean","BOOLEAN");
            put("int","INT32");
            put("long","INT64");
            put("float","FLOAT");
            put("double","DOUBLE");
            put("String","TEXT");

    }};
}
