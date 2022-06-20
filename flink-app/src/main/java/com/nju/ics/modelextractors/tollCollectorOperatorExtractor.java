package com.nju.ics.modelextractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.models.AbstractModel;
import com.nju.ics.models.Operator;
import com.nju.ics.utils.DataSourceJudge;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class tollCollectorOperatorExtractor extends GeneralExtractor {
    public tollCollectorOperatorExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element, KeyedProcessFunction<String, JSONObject, String>.Context ctx,
            int source, JSONObject entryRecord, List<AbstractModel> linkModel) {
        Operator modelEntity;


        switch (source) {
            case DataSourceJudge.entryLane:

            case DataSourceJudge.exitLane:

                modelEntity = new Operator(element.getString("操作员工号"), element.getString("操作员姓名"));

                break;
            case DataSourceJudge.gantryCharge:

            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }
        modelEntity.batchNum=linkModel.get(0).id();
        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
