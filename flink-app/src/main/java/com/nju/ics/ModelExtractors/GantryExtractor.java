package com.nju.ics.ModelExtractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.Gantry;
import com.nju.ics.Utils.DataSourceJudge;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class GantryExtractor extends GeneralExtractor {

    public GantryExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        Gantry modelEntity;
        switch (source) {
            case DataSourceJudge.entryLane:

            case DataSourceJudge.exitLane:

                return null;
            case DataSourceJudge.gantryCharge:

                modelEntity = new Gantry(element.getString("门架编号"), element.getIntValue("控制器序号"),
                        element.getString("门架HEX值"), element.getString("对向门架HEX值"));

                break;
            default:
                return null;

        }

        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
