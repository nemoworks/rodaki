package com.nju.ics.Models;

public class StationRecord extends AbstractModel {
    /** 入口、出口： 交易流水号 */
    public String id;
    /** 入口、出口：交易编码 */
    public String transCode;
    /** class Shift */
    public String shiftId;
    /** class Vehicle */
    public String vehicleId;
    /** class lane */
    public String laneId;

    /** class station */
    public String stationId;
    @Override
    public String id() {
        // TODO Auto-generated method stub
        return id;
    }

    public StationRecord() {
    }

    public StationRecord(String id, String transCode) {
        this.id = id == null ? null : id.trim();
        this.transCode = transCode == null ? null : transCode.trim();
    }

}
