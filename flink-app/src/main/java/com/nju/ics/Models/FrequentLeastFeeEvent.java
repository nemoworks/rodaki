package com.nju.ics.Models;

public class FrequentLeastFeeEvent {
    private String vehicleId;
    private long start;
    private long end;

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public FrequentLeastFeeEvent() {
    }

    public FrequentLeastFeeEvent(String vehicleid, long start, long end) {
        this.vehicleId = vehicleid;
        this.start = start;
        this.end = end;
    }

    public static FrequentLeastFeeEvent build(String vehicleid, long start, long end) {
        return new FrequentLeastFeeEvent(vehicleid, start, end);
    }

}
