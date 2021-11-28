package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;

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

    @JSONField(deserialize = false)
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

    @JSONField(name = "EXVLPC")
    public void setVLPC(int vLPC) {
        VLPC = vLPC;
    }

    @JSONField(serialize = false)
    public String getVLP() {
        return VLP;
    }

    @JSONField(name = "EXVLP")
    public void setVLP(String vLP) {
        VLP = vLP;
    }

    public int getINVOICETYPE() {
        return INVOICETYPE;
    }

    public void setINVOICETYPE(int iNVOICETYPE) {
        INVOICETYPE = iNVOICETYPE;
    }

    public int getINVOICECNT() {
        return INVOICECNT;
    }

    public void setINVOICECNT(int iNVOICECNT) {
        INVOICECNT = iNVOICECNT;
    }
    
}
