package com.nju.ics.ModelExtractors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Configs.MediaType;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.CPCCard;
import com.nju.ics.Utils.ConfigureENV;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import com.nju.ics.Utils.DataSourceJudge;

public class CPCCardExtractor extends GeneralExtractor {

    public CPCCardExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }


    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        // TODO Auto-generated method stub
        // cpc车辆
        // if (element.get("通行介质").asInt() != MediaType.cpc) {
        // return null;
        // }
        CPCCard modelEntity;

        switch (source) {
            case DataSourceJudge.entryLane:
                modelEntity = new CPCCard(element.getIntValue("电量"), element.getString("ETC/CPC卡号?"),
                        element.getString("OBU发行方标识"), element.getIntValue("卡片发行版本"));

                break;
            case DataSourceJudge.exitLane:

                modelEntity = new CPCCard(element.getIntValue("电量"), element.getString("通行介质编码"),
                        element.getString("OBU发行方标识"),
                        element.containsKey("卡片发行版本") ? element.getIntValue("卡片发行版本") : -1);

                break;
            case DataSourceJudge.gantryCharge:

                modelEntity = new CPCCard(element.getIntValue("OBU/CPC卡电量百分比"), element.getString("OBU/CPC序号编码"),
                        element.getString("OBU/CPC发行方标识"), element.getIntValue("OBU/CPC版本号"));

                break;
            default:
                return null;

        }

        modelEntity.mediaType = 2;
        modelEntity.vehicleId = linkModel.get(0) == null ? null : linkModel.get(0).id();

        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
