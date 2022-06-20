package com.nju.ics.models;

public class Shift extends AbstractModel {
    /** 入口、出口：批次号 */
    public String batchNum;
    /** class Operator */
    public String tollCollectorId;
    /** class Operator */
    public String monitorId;
    /** 入口、出口：上班时间 */
    public String loginTime;
    /** 入口、出口：授权时间 */
    public String monitorTime;
    /** 入口、出口：逻辑日期 */
    public String lDate;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return batchNum;
    }

    public Shift() {
    }

    public Shift(String batchNum,  String loginTime, String monitorTime,
            String lDate) {
        this.batchNum =batchNum==null?null: batchNum.trim();
        
        this.loginTime =loginTime==null?null: loginTime.trim();
        this.monitorTime =monitorTime==null?null: monitorTime.trim();
        this.lDate =lDate==null?null: lDate.trim();
    }

}
