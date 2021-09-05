package com.nju.ics.ModelExtractors;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.OutputTag;
import org.apache.iotdb.tsfile.file.metadata.enums.CompressionType;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.file.metadata.enums.TSEncoding;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.nju.ics.Models.AbstractModel;

import org.apache.iotdb.flink.options.IoTDBSinkOptions;

public abstract class GeneralExtractor {
    public Class modelcls;
    public OutputTag IotDBtag;
    public OutputTag RMQtag;

    public String toJSONString(AbstractModel model) {
        return JSON.toJSONString(model);
    }

    public GeneralExtractor(Class modelcls) {
        this.modelcls = modelcls;
    }

    public OutputTag getIotDBtag() {
        return IotDBtag;
    }

    public void setIotDBtag(OutputTag iotDBtag) {
        IotDBtag = iotDBtag;
    }

    public OutputTag getRMQtag() {
        return RMQtag;
    }

    public void setRMQtag(OutputTag rMQtag) {
        RMQtag = rMQtag;
    }

    public AbstractModel processElement_string(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, Object entryRecord,
            List<String> linkModel) {
        return null;
    };

    public abstract AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel);

    public void sinkEntity(AbstractModel modelEntity, KeyedProcessFunction<String, JSONObject, String>.Context ctx) {

        // System.out.println(tuple);
        if (this.RMQtag != null) {
            ctx.output(this.RMQtag, this.toJSONString(modelEntity));
        }

        if (this.IotDBtag != null) {
            Map<String, String> a = modelEntity.generateIotMsg(ctx.timestamp());
            if (a != null) {
                ctx.output(this.IotDBtag, a);
            }

        }
    }

}
