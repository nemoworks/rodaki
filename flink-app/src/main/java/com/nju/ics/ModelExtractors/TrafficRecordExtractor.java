package com.nju.ics.ModelExtractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.TrafficRecord;
import com.nju.ics.Utils.DataSourceJudge;
import java.util.stream.Collectors;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class TrafficRecordExtractor extends GeneralExtractor {
    public TrafficRecordExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    public AbstractModel processElement_string(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, Object entryRecord,
            List<String> linkModel) {
        TrafficRecord modelEntity;
        switch (source) {
            case DataSourceJudge.entryLane:

            case DataSourceJudge.exitLane:

                modelEntity = new TrafficRecord(element.getString("通行标识ID"));

                break;
            case DataSourceJudge.gantryCharge:

                modelEntity = new TrafficRecord(element.getString("通行标识 ID"));

                break;
            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }

        // linkModel应当具有三个及以上数目的元素
        // 第一个是入口记录 type：StationRecord
        // 第二个是出口记录 type：StationRecord
        // 第三个是OBU或者CPC type：Media
        // 如果有第四个就是代表门架记录 type：GantryRecord
        //System.out.println(modelEntity.id());
        modelEntity.entryStationId = linkModel.get(0);
        modelEntity.exitStationId = linkModel.get(1);
        modelEntity.mediaId = linkModel.get(2);
        modelEntity.gantryRecordIds = linkModel.subList(3, linkModel.size()).stream().map((x) -> x)
                .collect(Collectors.joining(";"));
        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        TrafficRecord modelEntity;
        switch (source) {
            case DataSourceJudge.entryLane:

            case DataSourceJudge.exitLane:

                modelEntity = new TrafficRecord(element.getString("通行标识ID"));
                break;
            case DataSourceJudge.gantryCharge:
                modelEntity = new TrafficRecord(element.getString("通行标识 ID"));
                break;
            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }

        // linkModel应当具有三个及以上数目的元素
        // 第一个是入口记录 type：StationRecord
        // 第二个是出口记录 type：StationRecord
        // 第三个是OBU或者CPC type：Media
        // 如果有第四个就是代表门架记录 type：GantryRecord
        modelEntity.entryStationId = linkModel.get(0) == null ? null : linkModel.get(0).id();
        modelEntity.exitStationId = linkModel.get(1) == null ? null : linkModel.get(1).id();
        modelEntity.mediaId = linkModel.get(2) == null ? null : linkModel.get(2).id();
        modelEntity.gantryRecordIds = linkModel.subList(3, linkModel.size()).stream().map((x) -> x.id())
                .collect(Collectors.joining(";"));

        // System.out.println(tuple);
       
        return modelEntity;

    }

}
