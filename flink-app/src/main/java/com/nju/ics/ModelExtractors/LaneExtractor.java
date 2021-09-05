package com.nju.ics.ModelExtractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.Lane;
import com.nju.ics.Models.LaneApp;
import com.nju.ics.Models.TollStation;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.OutputTagCollection;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class LaneExtractor extends GeneralExtractor {
    public LaneExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        Lane modelEntity;

        // 先获得LaneApp、一个收费站
        LaneApp laneapp = (LaneApp) OutputTagCollection.modelExtractors.get(LaneAppExtractor.class.getSimpleName()).f0
                .processElement(element, ctx, source, entryRecord, null);
        TollStation tollstation = (TollStation) OutputTagCollection.modelExtractors
                .get(TollStationExtractor.class.getSimpleName()).f0.processElement(element, ctx, source, entryRecord,
                        null);
        switch (source) {
            case DataSourceJudge.entryLane:
                modelEntity = new Lane(element.getIntValue("入口车道类型"), element.getString("入口车道号"),
                        element.getString("入口车道号(国标)"), element.getString("入口车道HEX编码"));

                break;
            case DataSourceJudge.exitLane:

                modelEntity = new Lane(element.getIntValue("出口车道类型"), element.getString("出口车道号"),
                        element.getString("出口车道号(国标)"), element.getString("出口车道HEX编码"));

                break;
            case DataSourceJudge.gantryCharge:

            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }
        modelEntity.laneApp = laneapp == null ? null : laneapp.id();
        modelEntity.tollStationId = tollstation == null ? null : tollstation.id();
        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
