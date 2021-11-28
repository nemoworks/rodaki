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
        return null;
    }

}
