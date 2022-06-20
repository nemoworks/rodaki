package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;
import com.nju.ics.fastjsonutils.IntDeserializer;

/**
 * 支付记录,只在出口记录产生
 */
@JSONType(parseFeatures = { Feature.IgnoreNotMatch })
public class ExitPaymentRecord extends AbstractModel {
    @JSONField(name = "PAYID")
    private String PAYID;

    @JSONField(name = "TIME")
    private long TIME;

    /** 通行交易id */
    @JSONField(name = "PASSID")
    private String PASSID;

    /** 车辆Id */
    @JSONField(name = "VEHICLEID")
    private String VEHICLEID;
    private int VLPC;
    private String VLP;
    @JSONField(name = "PAYTYPE")
    private int PAYTYPE;
    /**
     * 通行费用 入口： 门架：交易金额 出口：总交易金额
     */
    @JSONField(name = "FEE")
    private int FEE;
    /**
     * 通行里程 入口： 门架：计费里程数 出口：计费总里程数
     */
    @JSONField(name = "FEEMILEAGE")
    private int FEEMILEAGE;

    @JSONField(name = "INVOICEID")
    private String INVOICEID;

    @JSONField(name = "TRANSPAYTYPE")
    private int TRANSPAYTYPE;
    @JSONField(name = "EXVEHICLETYPE")
    private int VEHICLETYPE;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return PAYID;
    }

    public String getPAYID() {
        return VLP + "-" + VLPC + "-" + PASSID;
    }

    @JSONField(deserialize = false)
    public void setPAYID(String pAYID) {
        PAYID = pAYID;
    }

    public long getTIME() {
        return TIME;
    }

    @JSONField(alternateNames = { "_time" })
    public void setTIME(long eXTIME) {
        TIME = eXTIME;
    }

    public String getPASSID() {
        return PASSID;
    }

    @JSONField(alternateNames = { "PASSID" })
    public void setPASSID(String pASSID) {
        PASSID = pASSID;
    }

    public String getVEHICLEID() {
        return VLP + "-" + VLPC;
    }

    @JSONField(deserialize = false)
    public void setVEHICLEID(String vEHICLEID) {
        VEHICLEID = vEHICLEID;
    }

    @JSONField(serialize = false)
    public int getVLPC() {
        return VLPC;
    }

    @JSONField(alternateNames = { "EXVLPC" }, deserializeUsing = IntDeserializer.class)
    public void setVLPC(int vLPC) {
        VLPC = vLPC;
    }

    @JSONField(serialize = false)
    public String getVLP() {
        return VLP;
    }

    @JSONField(alternateNames = { "EXVLP" })
    public void setVLP(String vLP) {
        VLP = vLP;
    }

    public int getPAYTYPE() {
        return PAYTYPE;
    }

    @JSONField(name = "PAYTYPE", deserializeUsing = IntDeserializer.class)
    public void setPAYTYPE(int pAYTYPE) {
        PAYTYPE = pAYTYPE;
    }

    public int getFEE() {
        return FEE;
    }

    @JSONField(name = "FEE", deserializeUsing = IntDeserializer.class)
    public void setFEE(int fEE) {
        FEE = fEE;
    }

    public int getFEEMILEAGE() {
        return FEEMILEAGE;
    }

    @JSONField(name = "FEEMILEAGE", deserializeUsing = IntDeserializer.class)
    public void setFEEMILEAGE(int fEEMILEAGE) {
        FEEMILEAGE = fEEMILEAGE;
    }

    public String getINVOICEID() {
        return INVOICEID;
    }

    @JSONField(name = "INVOICEID")
    public void setINVOICEID(String iNVOICEID) {
        INVOICEID = iNVOICEID;
    }

    public int getTRANSPAYTYPE() {
        return TRANSPAYTYPE;
    }

    @JSONField(name = "TRANSPAYTYPE", deserializeUsing = IntDeserializer.class)
    public void setTRANSPAYTYPE(int tRANSPAYTYPE) {
        TRANSPAYTYPE = tRANSPAYTYPE;
    }

    public int getVEHICLETYPE() {
        return VEHICLETYPE;
    }

    @JSONField(name = "EXVEHICLETYPE", deserializeUsing = IntDeserializer.class)
    public void setVEHICLETYPE(int vEHICLETYPE) {
        VEHICLETYPE = vEHICLETYPE;
    }

}
