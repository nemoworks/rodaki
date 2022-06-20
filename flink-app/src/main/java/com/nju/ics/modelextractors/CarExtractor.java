package com.nju.ics.modelextractors;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.nju.ics.models.AbstractModel;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

public class CarExtractor {
    Class modelcls;

    public CarExtractor(Class modelcls) {
        this.modelcls = modelcls;
        // TODO Auto-generated constructor stub
    }

    
    public AbstractModel processElement() {
        // TODO Auto-generated method stub
        return null;
    }
}
