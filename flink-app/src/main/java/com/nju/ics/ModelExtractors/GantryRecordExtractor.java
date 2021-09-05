package com.nju.ics.ModelExtractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.Gantry;
import com.nju.ics.Models.GantryRecord;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.OutputTagCollection;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class GantryRecordExtractor extends GeneralExtractor {

    public GantryRecordExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        GantryRecord modelEntity;
        Map<String, String> tuple;
        // 先获得一个Gantry对象
        Gantry gantry = (Gantry) OutputTagCollection.modelExtractors.get(GantryExtractor.class.getSimpleName()).f0
                .processElement(element, ctx, source, entryRecord, null);
        switch (source) {
            case DataSourceJudge.entryLane:

            case DataSourceJudge.exitLane:

                return null;
            case DataSourceJudge.gantryCharge:

                modelEntity = new GantryRecord(element.getString("计费交易编号"), element.getString("计费交易时间"));

                break;
            default:
                return null;

        }

        // linkModel应当有一个元素，要么是OBU，要么是CPC
        modelEntity.gantryId = gantry == null ? null : gantry.id();
        modelEntity.mediaId = linkModel.get(0) == null ? null : linkModel.get(0).id();
        tuple = modelEntity.generateIotMsg(ctx.timestamp());

        // System.out.println(tuple);
        if (this.RMQtag != null) {
            ctx.output(this.RMQtag, this.toJSONString(modelEntity));
        }

        if (this.IotDBtag != null) {
            ctx.output(this.IotDBtag, tuple);
        }
        return modelEntity;
    }

}
