package com.nju.ics.Models;

public class OverlapPassidEvent {
    public static byte on = 1;
    public static byte off = 2;
    public static byte passidChanged = 3;
    /**
     * 事件类型
     */
    private byte type;

    /**
     * 发生passid字段变化后，之前是哪个passid
     */
    private String prePassid;
    /**
     * 发生passid字段变化后，现在是哪个passid
     */
    private String curPassid;
    /**
     * 车辆id
     */
    private String vehicleid;
    private long timestamp;

    /**
     * @return the type
     */
    public byte getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(byte type) {
        this.type = type;
    }

    /**
     * @return the prePassid
     */
    public String getPrePassid() {
        return prePassid;
    }

    /**
     * @param prePassid the prePassid to set
     */
    public void setPrePassid(String prePassid) {
        this.prePassid = prePassid;
    }

    /**
     * @return the curPassid
     */
    public String getCurPassid() {
        return curPassid;
    }

    /**
     * @param curPassid the curPassid to set
     */
    public void setCurPassid(String curPassid) {
        this.curPassid = curPassid;
    }

    /**
     * @return the vehicleid
     */
    public String getVehicleid() {
        return vehicleid;
    }

    /**
     * @param vehicleid the vehicleid to set
     */
    public void setVehicleid(String vehicleid) {
        this.vehicleid = vehicleid;
    }

    /**
     * @param type
     */
    public OverlapPassidEvent(byte type, String vehicleid, long timestamp) {
        this.type = type;
        this.vehicleid = vehicleid;
        this.timestamp = timestamp;
    }

    /**
     * @param type
     * @param prePassid
     * @param curPassid
     * @param vehicleid
     */
    public OverlapPassidEvent(byte type, String prePassid, String curPassid, String vehicleid, long timestamp) {
        this.type = type;
        this.prePassid = prePassid;
        this.curPassid = curPassid;
        this.vehicleid = vehicleid;
        this.timestamp = timestamp;
    }

    public OverlapPassidEvent() {
    }

    public static OverlapPassidEvent buildOnEvent(String vehicleid, long timestamp) {
        return new OverlapPassidEvent(OverlapPassidEvent.on, vehicleid, timestamp);
    }

    public static OverlapPassidEvent buildOffEvent(String vehicleid, long timestamp) {
        return new OverlapPassidEvent(OverlapPassidEvent.off, vehicleid, timestamp);
    }

    public static OverlapPassidEvent buildPassidChangedEvent(String prepassid, String curpassid, String vehicleid,
            long timestamp) {
        return new OverlapPassidEvent(OverlapPassidEvent.passidChanged, prepassid, curpassid, vehicleid, timestamp);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
