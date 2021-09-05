package com.nju.ics.ModelExtractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.Lane;
import com.nju.ics.Models.Shift;
import com.nju.ics.Models.StationRecord;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.OutputTagCollection;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class StationRecordExtractor extends GeneralExtractor {
    public StationRecordExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        StationRecord modelEntity;
        // 获得班次Shift 车道Lane
        Shift shift = (Shift) OutputTagCollection.modelExtractors.get(ShiftExtractor.class.getSimpleName()).f0
                .processElement(element, ctx, source, null, null);
        Lane lane = (Lane) OutputTagCollection.modelExtractors.get(LaneExtractor.class.getSimpleName()).f0
                .processElement(element, ctx, source, null, null);
        switch (source) {
            case DataSourceJudge.entryLane:

            case DataSourceJudge.exitLane:
                modelEntity = new StationRecord(element.getString("交易流水号"), element.getString("交易编码"));
                break;
            case DataSourceJudge.gantryCharge:

            default:
                return null;

        }

        // linkModel应当具有1个元素

        modelEntity.vehicleId = linkModel.get(0) == null ? null : linkModel.get(0).id();
        modelEntity.shiftId = shift == null ? null : shift.id();
        modelEntity.laneId = lane == null ? null : lane.id();
        modelEntity.stationId = lane == null ? null : lane.tollStationId;
        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
