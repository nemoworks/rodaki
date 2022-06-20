package com.nju.ics.fields;

public class AbnormalVehicle {
    private String vehicleid;
    private String stationid;
    private int stationtype;
    private long triggertime;
    private String passid;
    private long time;

    public AbnormalVehicle() {
    }

    public AbnormalVehicle(String vehicleid, String stationid, int stationtype, long triggertime, String passid,
            long time) {
        this.vehicleid = vehicleid;
        this.stationid = stationid;
        this.stationtype = stationtype;
        this.triggertime = triggertime;
        this.passid = passid;
        this.time = time;
    }

    public String getVehicleid() {
        return vehicleid;
    }

    public void setVehicleid(String vehicleid) {
        this.vehicleid = vehicleid;
    }

    public String getStationid() {
        return stationid;
    }

    public void setStationid(String stationid) {
        this.stationid = stationid;
    }

    public int getStationtype() {
        return stationtype;
    }

    public void setStationtype(int stationtype) {
        this.stationtype = stationtype;
    }

    public long getTriggertime() {
        return triggertime;
    }

    public void setTriggertime(long triggertime) {
        this.triggertime = triggertime;
    }

    public String getPassid() {
        return passid;
    }

    public void setPassid(String passid) {
        this.passid = passid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
