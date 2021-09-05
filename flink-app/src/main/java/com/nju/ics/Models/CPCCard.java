package com.nju.ics.Models;

import com.nju.ics.Annotation.IotDBAnnotion.sinkIotDB;

public class CPCCard extends Media {
    /** 入口、出口：电量，门架：OBU/CPC卡电量百分比 */
    @sinkIotDB
    public int batteryPercentage;
    /** 入口：ETC/CPC卡号?，门架：OBU/CPC序号编码,出口：通行介质编码 */
    public String id;
    /** 入口、出口：OBU发行方标识 门架：OBU/CPC发行方标识 */
    public String issueFlag;

    /** 入口、出口：卡片发行版本 门架：OBU/CPC版本号 */
    public int cardVersion;

    public CPCCard() {
    }

    public CPCCard(int batteryPercentage, String id, String issueFlag, int cardVersion) {
        this.batteryPercentage = batteryPercentage;
        this.id = id == null ? null : id.trim();
        this.issueFlag = issueFlag == null ? null : issueFlag.trim();
        this.cardVersion = cardVersion;
    }

    @Override
    public String toString() {
        return String.format("%s:%f", this.id, this.batteryPercentage);
    }

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return  this.id;
    }
}
