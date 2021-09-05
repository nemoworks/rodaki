package com.cvicse.highway.domain;

import java.security.AlgorithmConstraints;

public class Msg {
    public float longitude;//经度
    public float latitude;//纬度
    public String hex;//门架hex
    public Msg(float a,float b){
        this.longitude=a;
        this.latitude=b;
    }
    public Msg(){
    }
}