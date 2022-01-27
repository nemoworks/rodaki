package com.nju.ics.Fields;

public class GantryPositionMsg {
    public float longitude;//经度
    public float latitude;//纬度
    public int gantryPositionFlag;//省入口：1、出口:2
    public int gantryType;//实体门架：1、虚门架:2
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
