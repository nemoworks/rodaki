package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 通行交易，入口就开始产生，不断更新信息
 */
public class TrafficTransaction extends AbstractModel {
    /** 入口、出口:通行标识ID 门架：通行标识 ID */
    private String id;
    /** 出口： 交易描述 */
    private String description;
    /** */
    public String trafficRecordId;
    public String paymentRecordId;
    /** */
    public String vehicleId;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return id;
    }

    public TrafficTransaction() {

    }

    public TrafficTransaction(String id, String description) {
        this.id = id == null ? null : id.trim();
        this.description = description == null ? null : description.trim();
    }

    public String getId() {
        return id;
    }

    @JSONField(alternateNames = { "通行标识ID", "通行标识 ID" })
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getDescription() {
        return description;
    }

    @JSONField(alternateNames = "交易描述")
    public void setDescription(String description) {
        this.description = description;
    }

}
