package com.nju.ics.modelextractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.models.AbstractModel;
import com.nju.ics.models.InvoiceRecord;
import com.nju.ics.models.PaymentRecord;
import com.nju.ics.utils.DataSourceJudge;
import com.nju.ics.utils.OutputTagCollection;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class PaymentRecordExtractor extends GeneralExtractor {

    public PaymentRecordExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        
        PaymentRecord modelEntity;
        // 获得一个发票记录 InvoiceRecord
        InvoiceRecord invoiceRecord = (InvoiceRecord) OutputTagCollection.modelExtractors
                .get(InvoiceRecordExtractor.class.getSimpleName()).f0.processElement(element, ctx, source, null, null);
        switch (source) {
            case DataSourceJudge.exitLane:
                //System.out.println(element);
                // modelEntity = new PaymentRecord(element.getString("通行标识 ID"));
                try{
                    modelEntity = JSONObject.toJavaObject(element,PaymentRecord.class);
                }
                catch(Exception e){
                    //System.out.println(e);
                    //System.out.println(element);
                    return null;
                }
                break;
            case DataSourceJudge.entryLane:

            case DataSourceJudge.gantryCharge:
            default:
                return null;
            // ctx.output(this.RMQtag, objectMapper.writeValueAsString(modelentity));

        }
        modelEntity.invoiceRecordId = invoiceRecord == null ? null : invoiceRecord.id();
        
        this.sinkEntity(modelEntity, ctx);
        //System.exit(0);
        return modelEntity;
    }

}
