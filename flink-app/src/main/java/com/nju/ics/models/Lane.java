package com.nju.ics.models;

public class Lane extends AbstractModel {
    /**class LaneApp */
    public String laneApp;
    /** 入口：入口车道类型 出口：出口车道类型 */
    public int laneType;

    /** 入口：入口车道号 出口：出口车道号 */
    public String laneNumber;

    /** 入口：入口车道号(国标) 出口：出口车道号(国标) */
    public String laneNumberId;
    /** 入口：入口车道HEX编码 出口：出口车道HEX编码 */
    public String laneHex;
    /**class TollStation */
    public String tollStationId;
    @Override
    public String id() {
        // TODO Auto-generated method stub
        return laneNumberId;
    }
    public Lane() {
    }
    public Lane( int laneType, String laneNumber, String laneNumberId, String laneHex) {

        this.laneType = laneType;
        this.laneNumber =laneNumber==null?null: laneNumber.trim();
        this.laneNumberId =laneNumberId==null?null: laneNumberId.trim();
        this.laneHex =laneHex==null?null: laneHex.trim();

    }

}
