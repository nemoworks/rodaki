package com.nju.ics.ModelExtractors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.ETCCard;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.ConfigureENV;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class ETCCardExtractor extends GeneralExtractor {

    public ETCCardExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        // TODO Auto-generated method stub
        // obu车辆
        // if (element.get("通行介质类型").asInt() != 1) {
        // return null;
        // }
        ETCCard modelEntity;

        switch (source) {
            case DataSourceJudge.entryLane:
                return null;
            case DataSourceJudge.exitLane:

                return null;
            case DataSourceJudge.gantryCharge:
                modelEntity = new ETCCard(element.getIntValue("CPU卡类型"), element.getIntValue("CPU卡版本号"),
                        element.getString("CPU卡片网络编号"), element.getString("CPU卡类型.1"), element.getString("CPU 起始日期"),
                        element.getString("CPU 截止日期"), element.getString("CPU内车牌号"), element.getIntValue("CPU内车牌颜色"),
                        element.getIntValue("CPU内车型"), element.getIntValue("车辆用户类型"));
                break;
            default:
                return null;

        }
        this.sinkEntity(modelEntity, ctx);
        return modelEntity;

    }

}
