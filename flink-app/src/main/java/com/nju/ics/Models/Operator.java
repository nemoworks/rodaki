package com.nju.ics.Models;

import com.nju.ics.Annotation.IotDBAnnotion.sinkIotDB;

public class Operator extends AbstractModel {
    /**入口、出口：操作员工号 班长工号 */
    public String operId;
    /**入口、出口：操作员姓名 班长名称*/
    public String operName;
    /**班次号 */
    @sinkIotDB
    public String batchNum;
    public Operator() {
    }
    
    public Operator(String operId, String operName) {
        this.operId =operId==null?null: operId.trim();
        this.operName =operName==null?null: operName.trim();
    }

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return operId;
    }
    
}
