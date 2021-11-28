package com.nju.ics.ModelExtractors;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.ENStationRecord;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class ENStationRecordExtractor extends GeneralExtractor {

    public ENStationRecordExtractor(Class modelcls) {
        super(modelcls);
        //TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        // TODO Auto-generated method stub
        ENStationRecord modelEntity;
        modelEntity=JSONObject.toJavaObject(element, ENStationRecord.class);
        modelEntity.setENTIME(ctx.timestamp());
        //System.out.println(this.toJSONString(modelEntity));
        this.sinkEntity(modelEntity, ctx);
        return null;
    }
    
}
