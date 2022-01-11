package com.nju.ics.Fields;

public class GantryPositionMsg {
    public float longtitude;//经度
    public float latitude;//纬度
    public String name;
    public GantryPositionMsg() {
    }
    public String toString(){
        return String.format("%f,%f",longtitude,latitude );
    }
    public String toIotDBString(){
        return String.format("%f;%f",longtitude,latitude );
    }
}
