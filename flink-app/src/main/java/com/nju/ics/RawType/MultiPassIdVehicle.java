package com.nju.ics.RawType;

public class MultiPassIdVehicle {
    private String vehicleid;
    private String stationid;
    private int stationtype;
    private String passid;
    private long time;
    private String oldpassid;

    public MultiPassIdVehicle() {
    }

    public MultiPassIdVehicle(String vehicleid, String stationid, int stationtype, String passid, String oldpassid,
            long time) {
        this.vehicleid = vehicleid;
        this.stationid = stationid;
        this.stationtype = stationtype;
        this.passid = passid;
        this.oldpassid = oldpassid;
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

    public String getOldpassid() {
        return oldpassid;
    }

    public void setOldpassid(String oldpassid) {
        this.oldpassid = oldpassid;
    }

}
