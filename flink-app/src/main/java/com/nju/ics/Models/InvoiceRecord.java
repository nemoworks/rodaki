package com.nju.ics.Models;
/**
 * 发票记录 只在出口记录产生
 */
public class InvoiceRecord extends AbstractModel{
    /**出口：发票类型 */
    public String type;
    /**出口：发票代码 */
    public String code;
    /**出口：发票编号 */
    public String id;
    /**出口：发票张数 */
    public String cnt;
    @Override
    public String id() {
        // TODO Auto-generated method stub
        return id;
    }
    public InvoiceRecord(String type, String code, String id, String cnt) {
        this.type =type==null?null: type.trim();
        this.code =code==null?null: code.trim();
        this.id =id==null?null: id.trim();
        this.cnt =cnt==null?null: cnt.trim();
    }
    
}
