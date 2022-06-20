package com.nju.ics.modelextractors;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.models.AbstractModel;
import com.nju.ics.models.ENVehicleRecord;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class ENVehicleRecordExtractor  extends GeneralExtractor{

    public ENVehicleRecordExtractor(Class modelcls) {
        super(modelcls);
        //TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        // TODO Auto-generated method stub
        ENVehicleRecord modelEntity;
        modelEntity=JSONObject.toJavaObject(element, ENVehicleRecord.class);
        this.sinkEntity(modelEntity, ctx);
        return null;
    }
    
}
