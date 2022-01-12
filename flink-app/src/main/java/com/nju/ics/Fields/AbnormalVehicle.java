package com.nju.ics.Fields;

public class AbnormalVehicle {
    private String vehicleid;
    private String gantryname;
    public AbnormalVehicle() {
    }
    
    public AbnormalVehicle(String vehicleid, String gantryname) {
        this.vehicleid = vehicleid;
        this.gantryname = gantryname;
    }

    public String getVehicleid() {
        return vehicleid;
    }

    public void setVehicleid(String vehicleid) {
        this.vehicleid = vehicleid;
    }

    public String getGantryname() {
        return gantryname;
    }

    public void setGantryname(String gantryname) {
        this.gantryname = gantryname;
    }
    
}
