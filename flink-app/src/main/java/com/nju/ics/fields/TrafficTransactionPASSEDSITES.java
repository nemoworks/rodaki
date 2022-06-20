package com.nju.ics.fields;

import org.apache.flink.table.annotation.DataTypeHint;

@DataTypeHint(value = "RAW",bridgedTo = TrafficTransactionPASSEDSITES.class)
public class TrafficTransactionPASSEDSITES {
    public String PASSID;
    public String SITEID;
    public long TIME;
    public int FEEMILEAGE;
    public int FEE;
    public int SITETYPE;
    public float LONGITUDE;
    public float LATITUDE;
    public String NAME;
    public TrafficTransactionPASSEDSITES() {
    }
    
}
