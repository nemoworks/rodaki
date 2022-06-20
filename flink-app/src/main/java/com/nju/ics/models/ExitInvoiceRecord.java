package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.nju.ics.fastjsonutils.IntDeserializer;

/**
 * 发票记录 只在出口记录产生
 */
public class ExitInvoiceRecord extends AbstractModel {
    @JSONField(name = "PAYID")
    private String PAYID;

    private String PASSID;
    private int VLPC;
    private String VLP;

    @JSONField(name = "TIME")
    private long TIME;
    @JSONField(name = "INVOICETYPE")
    private int INVOICETYPE;
    @JSONField(name = "INVOICECNT")
    private int INVOICECNT;

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

    @JSONField(serialize = false)
    public String getPASSID() {
        return PASSID;
    }

    @JSONField(alternateNames = { "PASSID" })
    public void setPASSID(String pASSID) {
        PASSID = pASSID;
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

    public int getINVOICETYPE() {
        return INVOICETYPE;
    }

    @JSONField(deserializeUsing = IntDeserializer.class)
    public void setINVOICETYPE(int iNVOICETYPE) {
        INVOICETYPE = iNVOICETYPE;
    }

    public int getINVOICECNT() {
        return INVOICECNT;
    }

    @JSONField(deserializeUsing = IntDeserializer.class)
    public void setINVOICECNT(int iNVOICECNT) {
        INVOICECNT = iNVOICECNT;
    }

}
