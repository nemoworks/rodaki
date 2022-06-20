package com.nju.ics.rawtype;

public class StationPositionMsg {
    public float longitude;//经度
    public float latitude;//纬度
    public String stationName;//站点名称
    public StationPositionMsg() {
    }
    public String toString(){
        return String.format("%f,%f",longitude,latitude );
    }
    public String toIotDBString(){
        return String.format("%f;%f",longitude,latitude );
    }
}
