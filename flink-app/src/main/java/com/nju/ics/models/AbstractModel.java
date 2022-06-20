package com.nju.ics.models;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nju.ics.annotation.IotDBAnnotion.sinkIotDB;
import com.nju.ics.configs.IotDBTypeConfig;
import com.nju.ics.utils.ConfigureENV;

import org.apache.flink.util.OutputTag;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class AbstractModel {
    // private String model;

    @JSONField(name="_model")
    public String get_model() {
        return this.getClass().getSimpleName();
    }
    // @JSONField(deserialize=false)
    // public void set_model(String _model) {
    //     this.model = model;
    // }
    // @JSONField(name="_id")
    // public String get_id() {
    //     return String.format("%s-%s", this.getClass().getSimpleName(),this.id());
    // }

    public abstract String id();

    public Map<String, String> generateIotMsg(long timestamp) {
        Map<String, String> tuple = new HashMap();
        tuple.put("device", String.format("%s.%s.%s", ConfigureENV.prop.getProperty("IotDB.StorageGroup"),
                this.getClass().getSimpleName(), this.id()));
        tuple.put("timestamp", String.valueOf(timestamp));
        Field[] fields = this.getClass().getFields();
        List<Field> publicFields = new ArrayList<Field>() {
        };
        for (Field field : fields) {// 先筛选含有sinkIotDB注解的field
            if (field.isAnnotationPresent(sinkIotDB.class)) {
                publicFields.add(field);
            }
        }
        if (publicFields.size() == 0) {
            return null;
        }
        // 再把各种类型为null的feild删除
        // List<Field> publicFieldsWithoutNull = new ArrayList<Field>() {
        // };
        tuple.put("measurements", publicFields.stream().map((x) -> x.getName()).collect(Collectors.joining(",")));
        tuple.put("types",
                publicFields.stream().map((x) -> IotDBTypeConfig.TSDataTYpeMap.get(x.getType().getSimpleName()))
                        .collect(Collectors.joining(",")));
        tuple.put("values", publicFields.stream().map((x) -> {
            try {
                return String.valueOf(x.get(this));
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "null";
        }).collect(Collectors.joining(",")));
        return tuple;

    }
}
