package com.nju.ics.Models;

public class TimeoutEvent extends AbstractModel {
    private String vehicleid;
    private long lasttimestamp;
    private String stationid;
    private long triggertime;

    public TimeoutEvent() {
    }

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return vehicleid;
    }

    public String getVehicleid() {
        return vehicleid;
    }

    public void setVehicleid(String vehicleid) {
        this.vehicleid = vehicleid;
    }

    public long getLasttimestamp() {
        return lasttimestamp;
    }

    public void setLasttimestamp(long lasttimestamp) {
        this.lasttimestamp = lasttimestamp;
    }

    public String getStationid() {
        return stationid;
    }

    public void setStationid(String stationid) {
        this.stationid = stationid;
    }

    public long getTriggertime() {
        return triggertime;
    }

    public void setTriggertime(long triggertime) {
        this.triggertime = triggertime;
    }

}
