package com.nju.ics.Models;

import com.nju.ics.Annotation.IotDBAnnotion.sinkIotDB;
import com.alibaba.fastjson.annotation.JSONField;

public class Vehicle extends AbstractModel {
    /** class Plate */
    public String plateId;
    /** 入口：车种 出口：出口车种 门架：车种 */
    private int vehicleClass;
    /** 入口：收费车型 出口：出口车型 门架：计费车型 */
    private int vehicleType;
    /** 门架：车辆座位数/载重 */
    private int vehicleSeat;
    /** 门架：车辆长 */
    private int vehicleLength;
    /** 门架：车辆宽 */
    private int vehicleWidth;
    /** 门架：车辆高 */
    private int vehicleHight;
    /** 当前通行交易id */
    public String trafficTransactionid;

    /** 入口、出口：轴组信息 */
    private String axisInfo;
    /** 入口、出口：限载总重(kg) */
    private int limitWeight;
    /** 入口:入口重量 出口：出口重量 */
    @sinkIotDB
    private int weight;
    /** 车速 */
    @sinkIotDB
    public int speed;
    /** 最近经过的门架/站点 */
    @sinkIotDB
    public String lastPassStation;
    /**位置 */
    @sinkIotDB
    public String location;
    @Override
    public String id() {
        // TODO Auto-generated method stub
        return plateId;
    }

    public Vehicle() {
    }

    public Vehicle(String plateId, int vehicleClass, int vehicleType, int vehicleSeat, int vehicleLength,
            int vehicleWidth, int vehicleHight) {
        this.plateId = plateId == null ? null : plateId.trim();
        this.vehicleClass = vehicleClass;
        this.vehicleType = vehicleType;
        this.vehicleSeat = vehicleSeat;
        this.vehicleLength = vehicleLength;
        this.vehicleWidth = vehicleWidth;
        this.vehicleHight = vehicleHight;
    }
    


    public int getVehicleClass() {
        return vehicleClass;
    }

    @JSONField(alternateNames = { "车种", "出口车种" })
    public void setVehicleClass(int vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public int getVehicleType() {
        return vehicleType;
    }

    @JSONField(alternateNames = { "收费车型", "出口车型", "计费车型" })
    public void setVehicleType(int vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getVehicleSeat() {
        return vehicleSeat;
    }

    @JSONField(alternateNames = { "车辆座位数/载重" })
    public void setVehicleSeat(int vehicleSeat) {
        this.vehicleSeat = vehicleSeat;
    }

    public int getVehicleLength() {
        return vehicleLength;
    }

    @JSONField(alternateNames = { "车辆长" })
    public void setVehicleLength(int vehicleLength) {
        this.vehicleLength = vehicleLength;
    }

    public int getVehicleWidth() {
        return vehicleWidth;
    }

    @JSONField(alternateNames = { "车辆宽" })
    public void setVehicleWidth(int vehicleWidth) {
        this.vehicleWidth = vehicleWidth;
    }

    public int getVehicleHight() {
        return vehicleHight;
    }

    @JSONField(alternateNames = { "车辆高" })
    public void setVehicleHight(int vehicleHight) {
        this.vehicleHight = vehicleHight;
    }

    public String getAxisInfo() {
        return axisInfo;
    }

    @JSONField(alternateNames = { "轴组信息" })
    public void setAxisInfo(String axisInfo) {
        this.axisInfo = axisInfo;
    }

    public int getLimitWeight() {
        return limitWeight;
    }

    @JSONField(alternateNames = { "限载总重(kg)" })
    public void setLimitWeight(int limitWeight) {
        this.limitWeight = limitWeight;
    }

    public int getWeight() {
        return weight;
    }

    @JSONField(alternateNames = { "入口重量", "出口重量" })
    public void setWeight(int weight) {
        this.weight = weight;
    }

}
