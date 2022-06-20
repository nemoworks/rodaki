package com.nju.ics.modelextractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.models.AbstractModel;
import com.nju.ics.models.LaneApp;
import com.nju.ics.utils.DataSourceJudge;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class LaneAppExtractor extends GeneralExtractor {
    public LaneAppExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        LaneApp modelEntity;
  

        switch (source) {
            case DataSourceJudge.entryLane:

            case DataSourceJudge.exitLane:

                modelEntity = new LaneApp(element.getString("车道程序版本号"));

                break;
            case DataSourceJudge.gantryCharge:

                return null;
            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }

        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
