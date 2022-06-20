package com.nju.ics.modelextractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.configs.GantryPosition;
import com.nju.ics.models.AbstractModel;
import com.nju.ics.models.Plate;
import com.nju.ics.models.Vehicle;
import com.nju.ics.utils.DataSourceJudge;
import com.nju.ics.utils.OutputTagCollection;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class VehicleExtractor extends GeneralExtractor {
    public VehicleExtractor(Class modelcls,Boolean rmqflag,Boolean iotdbflag) {
        super(modelcls,rmqflag,iotdbflag);
        // TODO Auto-generated constructor stub

    }
    public VehicleExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub

    }


    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        Vehicle modelEntity;

        if (source <= DataSourceJudge.gantryCharge) {
            modelEntity = JSONObject.toJavaObject(element, Vehicle.class);
            //System.out.println(modelEntity.getVehicleHight());
        } else {
            return null;
        }
        // modelEntity.setLastPassTimes(ctx.timestamp());
        // 将车辆的位置提取出来
		// if (modelEntity.lastPassStation != null) {
			// modelEntity.location = GantryPosition.geoMap.containsKey(modelEntity.lastPassStation)
			// 		? GantryPosition.geoMap.get(modelEntity.lastPassStation).toIotDBString()
			// 		: null;
		// }
        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
