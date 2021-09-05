package com.nju.ics.Models;

import java.util.List;

public class TrafficRecord extends AbstractModel {
    /**入口、出口:通行标识ID 门架：通行标识 ID */
    private String id;
    /** class Media */
    public String mediaId;
    /** class GantryRecord */
    public String gantryRecordIds;
    /** class StationRecord */
    public String entryStationId;
    /** class StationRecord */
    public String exitStationId;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return id;
    }

    public TrafficRecord() {
    }

    public TrafficRecord(String id) {
        this.id =id==null?null: id.trim();
    }
    

}
