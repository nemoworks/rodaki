package com.nju.ics.Fields;

public class GantryPositionMsg {
    public float longitude;//经度
    public float latitude;//纬度
    public String gantryPositionFlag;//省入口、出口
    public String name;
    public GantryPositionMsg() {
    }
    public String toString(){
        return String.format("%f,%f",longitude,latitude );
    }
    public String toIotDBString(){
        return String.format("%f;%f",longitude,latitude );
    }
}
