package com.nju.ics.ModelExtractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.Plate;
import com.nju.ics.Utils.DataSourceJudge;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class PlateExtractor extends GeneralExtractor {
    public PlateExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {

        Plate modelEntity;

        switch (source) {
            case DataSourceJudge.entryLane:
               
            case DataSourceJudge.exitLane:
                
            case DataSourceJudge.gantryCharge:

                modelEntity = JSONObject.toJavaObject(element, Plate.class);

                break;
            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }

        this.sinkEntity(modelEntity, ctx);

        return modelEntity;
    }

}
