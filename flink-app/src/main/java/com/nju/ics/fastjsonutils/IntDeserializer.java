package com.nju.ics.fastjsonutils;

import java.lang.reflect.Type;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class IntDeserializer implements ObjectDeserializer{
    @Override
    public Integer deserialze(DefaultJSONParser parser, Type type, Object fieldName){
        try{
            return parser.lexer.intValue();
        }
        catch (Exception e){
            return -1;
        }
        
    }

    @Override
    public int getFastMatchToken() {
        // TODO Auto-generated method stub
        return 0;
    }
}
