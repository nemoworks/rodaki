package com.nju.ics.models;

import com.nju.ics.annotation.IotDBAnnotion.sinkIotDB;

public class OBUCard extends Media {
    /** 入口、出口：OBU发行方标识，门架：OBU/CPC发行方标识 */
    public String obuIssueFlag;
    /** 入口、出口、门架：OBU单/双片标识 */
    public int obuSign;
    /** 入口、出口：OBU编号 门架：OBU/CPC序号编码 */
    public String id;
    /** 入口、出口：厂商编号 门架：无 */
    public String supplierId;
    /** 入口、出口：电量，门架：OBU/CPC卡电量百分比 */
    @sinkIotDB
    public int batteryPercentage;
    /** 入口、出口：OBU发行版本，门架：OBU/CPC版本号 */
    public int obuVersion;
    /** class ETCCard */
    public String ETCcardId;
        //车辆id
        public String vehicleId;
    public OBUCard() {
    }

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return this.id;
    }

    public OBUCard(String obuIssueFlag, int obuSign, String id, String supplierId, int batteryPercentage,
            int obuVersion) {
        this.obuIssueFlag = obuIssueFlag==null?null:obuIssueFlag.trim();
        this.obuSign = obuSign;
        this.id = id==null?null:id.trim();
        this.supplierId = supplierId==null?null:supplierId.trim();
        this.batteryPercentage = batteryPercentage;
        this.obuVersion = obuVersion;
       
    }
    
}
