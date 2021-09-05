package com.nju.ics.ModelExtractors;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.Models.AbstractModel;
import com.nju.ics.Models.PaymentRecord;
import com.nju.ics.Models.TrafficTransaction;
import com.nju.ics.Utils.DataSourceJudge;
import com.nju.ics.Utils.OutputTagCollection;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class TrafficTransactionExtractor extends GeneralExtractor {

    public TrafficTransactionExtractor(Class modelcls) {
        super(modelcls);
        // TODO Auto-generated constructor stub
    }

    @Override
    public AbstractModel processElement(JSONObject element,
            KeyedProcessFunction<String, JSONObject, String>.Context ctx, int source, JSONObject entryRecord,
            List<AbstractModel> linkModel) {
        TrafficTransaction modelEntity = JSONObject.toJavaObject(element,TrafficTransaction.class);
        // 产生一个支付记录
        PaymentRecord payment = (PaymentRecord) OutputTagCollection.modelExtractors
                .get(PaymentRecordExtractor.class.getSimpleName()).f0.processElement(element, ctx, source, null, null);
        modelEntity.paymentRecordId = payment == null ? null : payment.id();
        return modelEntity;
    }

}
