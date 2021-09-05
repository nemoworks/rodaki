package com.nju.ics.Models;

public class GantryRecord extends AbstractModel {
    /** class Gantry */
    public String gantryId;
    /** 门架：计费交易编号 */
    public String tradeId;
    /** 门架：计费交易时间 */
    public String transTime;
    /** class Media */
    public String mediaId;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return tradeId;
    }

    public GantryRecord() {
    }

    public GantryRecord( String tradeId, String transTime) {

        this.tradeId =tradeId==null?null: tradeId.trim();
        this.transTime =transTime==null?null: transTime.trim();
        
    }

}
