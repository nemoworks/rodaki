package com.nju.ics.ModelExtractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.Plate;
import com.nju.ics.Models.Vehicle;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.OutputTagCollection;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class VehicleExtractor extends GeneralExtractor {
    public VehicleExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        Vehicle modelEntity;
        // 先产生一个Plate实体
        Plate plate = (Plate) OutputTagCollection.modelExtractors.get(PlateExtractor.class.getSimpleName()).f0
                .processElement(element, ctx, source, entryRecord, null);

        if (source <= DataSourceJudge.gantryCharge) {
            modelEntity = JSONObject.toJavaObject(element, Vehicle.class);
            modelEntity.plateId = plate.id();
            //System.out.println(modelEntity.getVehicleHight());
        } else {
            return null;
        }

        return modelEntity;
    }

}
