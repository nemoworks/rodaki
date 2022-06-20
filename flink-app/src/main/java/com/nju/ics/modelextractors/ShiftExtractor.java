package com.nju.ics.modelextractors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.models.AbstractModel;
import com.nju.ics.models.Operator;
import com.nju.ics.models.Shift;
import com.nju.ics.utils.DataSourceJudge;
import com.nju.ics.utils.OutputTagCollection;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class ShiftExtractor extends GeneralExtractor {
    public ShiftExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        Shift modelEntity;

        switch (source) {
            case DataSourceJudge.entryLane:

            case DataSourceJudge.exitLane:

                modelEntity = new Shift(element.getString("批次号"), element.getString("上班时间"), element.getString("授权时间"),
                        element.getString("逻辑日期"));

                break;
            case DataSourceJudge.gantryCharge:

            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }
        List<AbstractModel> links=Arrays.asList(modelEntity);
        // 先获得一个操作员、一个班长
        Operator monitor = (Operator) OutputTagCollection.modelExtractors
                .get(monitorOperatorExtractor.class.getSimpleName()).f0.processElement(element, ctx, source,
                        entryRecord, links);
        Operator collector = (Operator) OutputTagCollection.modelExtractors
                .get(tollCollectorOperatorExtractor.class.getSimpleName()).f0.processElement(element, ctx, source,
                        entryRecord, links);
        modelEntity.monitorId = monitor == null ? null : monitor.id();
        modelEntity.tollCollectorId = collector == null ? null : collector.id();
        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
