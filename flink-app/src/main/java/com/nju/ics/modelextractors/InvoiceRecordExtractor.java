package com.nju.ics.modelextractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.models.AbstractModel;
import com.nju.ics.models.InvoiceRecord;
import com.nju.ics.utils.DataSourceJudge;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class InvoiceRecordExtractor extends GeneralExtractor {

    public InvoiceRecordExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        InvoiceRecord modelEntity;
        switch (source) {
            case DataSourceJudge.entryLane:
                return null;
            case DataSourceJudge.exitLane:

                modelEntity = new InvoiceRecord(element.getString("发票类型"), element.getString("发票代码"),
                        element.getString("发票编号"), element.getString("发票张数"));

                break;
            case DataSourceJudge.gantryCharge:

                return null;
            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }

        this.sinkEntity(modelEntity, ctx);
        return modelEntity;
    }

}
