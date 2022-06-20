package com.nju.ics.models;

/**
 * ETCCard记录只能在
 */
public class ETCCard extends AbstractModel {
    /** 入口：ETC卡类型 门架：CPU卡类型,出口：无 */
    public int type;
    /** 入口、出口：卡片发行版本，门架：CPU卡版本号 */

    public int cardVersion;
    /** 门架：CPU卡片网络编号 */
    public String netID;
    /** 入口：ETC/CPC卡号? 出口：通行介质编码 门架：CPU卡类型.1 (这里数据文件的字段与数据库字段不符) */
    public String id;
    /** 门架：CPU 起始日期 */
    public String startDate;
    /** 门架：CPU 截止日期 */
    public String endDate;

    /** 门架：CPU内车牌号 */
    public String Vlp;
    /** 门架：CPU内车牌颜色 */
    public int Vlpc;
    /** 门架：CPU内车型 */
    public int VehicleType;
    /** 门架：车辆用户类型 */
    public int vehicleUserType;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return id;
    }

    public ETCCard() {
    }

    public ETCCard(int type, int cardVersion, String netID, String id, String startDate, String endDate, String vlp,
            int vlpc, int vehicleType, int vehicleUserType) {
        this.type = type;
        this.cardVersion = cardVersion;
        this.netID = netID == null ? null : netID.trim();
        this.id = id == null ? null : id.trim();
        this.startDate = startDate == null ? null : startDate.trim();
        this.endDate = endDate == null ? null : endDate.trim();
        Vlp = vlp == null ? null : vlp.trim();
        Vlpc = vlpc;
        VehicleType = vehicleType;
        this.vehicleUserType = vehicleUserType;
    }

}
