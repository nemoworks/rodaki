package com.nju.ics.ModelExtractors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Configs.MediaType;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.ETCCard;
import com.nju.ics.Models.OBUCard;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.OutputTagCollection;
import com.nju.ics.Utils.ConfigureENV;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class OBUCardExtractor extends GeneralExtractor {
    public OBUCardExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        // TODO Auto-generated method stub
        // obu车辆
        // if (element.get("通行介质").asInt() != MediaType.obu) {
        // return null;
        // }
        OBUCard modelEntity;
        // 先获得一个etccard
        ETCCard etcCard = (ETCCard) OutputTagCollection.modelExtractors.get(ETCCardExtractor.class.getSimpleName()).f0
                .processElement(element, ctx, source, entryRecord, null);
        switch (source) {
            case DataSourceJudge.entryLane:
                modelEntity = new OBUCard(element.getString("OBU发行方标识"), element.getIntValue("OBU单/双片标识"),
                        element.getString("OBU编号"), element.getString("厂商编号"), element.getIntValue("电量"),
                        element.getIntValue("OBU发行版本"));

                break;
            case DataSourceJudge.exitLane:

                modelEntity = new OBUCard(element.getString("OBU发行方标识"), element.getIntValue("OBU单/双片标识"),
                        element.getString("OBU编号"), element.getString("厂商编号"), element.getIntValue("电量"),
                        element.getIntValue("OBU发行版本"));

                break;
            case DataSourceJudge.gantryCharge:

                modelEntity = new OBUCard(element.getString("OBU/CPC发行方标识"), element.getIntValue("OBU单/双片标识"),
                        element.getString("OBU/CPC序号编码"), null, element.getIntValue("OBU/CPC卡电量百分比"),
                        element.getIntValue("OBU/CPC版本号"));

                break;
            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }
        modelEntity.ETCcardId = etcCard == null ? null : etcCard.id();
        modelEntity.mediaType = 2;
        modelEntity.vehicleId = linkModel.get(0) == null ? null : linkModel.get(0).id();

        this.sinkEntity(modelEntity, ctx);
        return modelEntity;

    }

}
