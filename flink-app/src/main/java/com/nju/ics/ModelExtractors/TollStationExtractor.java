package com.nju.ics.ModelExtractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.TollStation;
import com.nju.ics.Utils.DataSourceJudge;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class TollStationExtractor extends GeneralExtractor {
    public TollStationExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        TollStation modelEntity;


        switch (source) {
            case DataSourceJudge.entryLane:
                modelEntity = new TollStation(element.getString("入口站号"), element.getString("入口站号(国标)"),
                        element.getString("入口站HEX编码"));

                break;
            case DataSourceJudge.exitLane:

                modelEntity = new TollStation(element.getString("出口站号"), element.getString("出口站号(国标)"),
                        element.getString("出口站HEX编码"));

                break;
            case DataSourceJudge.gantryCharge:

            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }

        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
