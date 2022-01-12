package com.nju.ics.Funcs;

import com.alibaba.fastjson.JSONObject;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.types.Row;

public class Row2JSONObject implements MapFunction<Row,JSONObject> {
    private String[] cols;
    public Row2JSONObject(String[] cols){
        this.cols=cols;
    }
    @Override
    public JSONObject map(Row value) throws Exception {
        // TODO Auto-generated method stub
        JSONObject entity=new JSONObject();
        for (int i=0;i<cols.length;i++){
            entity.put(cols[i], value.getField(i));
        }
        
        return entity;
    }
    
}
