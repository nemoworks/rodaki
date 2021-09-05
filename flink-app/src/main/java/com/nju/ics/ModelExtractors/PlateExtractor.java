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
                modelEntity = new Plate(element.getIntValue("车牌颜色"), element.getString("车牌号"));

                break;
            case DataSourceJudge.exitLane:

                modelEntity = new Plate(element.getIntValue("出口实际车牌颜色"), element.getString("出口实际车牌号"));

                break;
            case DataSourceJudge.gantryCharge:

                modelEntity = new Plate(element.getIntValue("计费车牌颜色"), element.getString("计费车牌号"));

                break;
            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }

        this.sinkEntity(modelEntity, ctx);

        return modelEntity;
    }

}
