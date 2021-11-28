package com.nju.ics.ModelExtractors;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class GantryMsgExtractor extends GeneralExtractor {

    public GantryMsgExtractor(Class modelcls) {
        super(modelcls);
        //TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
