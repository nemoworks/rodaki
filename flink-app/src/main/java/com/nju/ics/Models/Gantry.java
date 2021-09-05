package com.nju.ics.Models;

import com.nju.ics.Configs.GantryPosition;

public class Gantry extends AbstractModel {
    /** 门架：门架编号 */
    public String id;
    /** 门架：控制器序号 */
    public int computerOrder;
    /** 门架：门架HEX值 */
    public String hex;
    /** 门架：对向门架HEX值 */
    public String hexOpposite;
    /** 经度 */
    public float longtitude;
    /** 纬度 */
    public float latitude;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return id;
    }

    public Gantry() {
    }

    public Gantry(String id, int computerOrder, String hex, String hexOpposite) {
        this.id =id==null?null: id.trim();
        this.computerOrder = computerOrder;
        this.hex =hex==null?null: hex.trim();
        this.hexOpposite =hexOpposite==null?null: hexOpposite.trim();
        this.latitude = (GantryPosition.geoMap.containsKey(id) ? GantryPosition.geoMap.get(id).latitude : 0.0f);
        this.longtitude = (GantryPosition.geoMap.containsKey(id) ? GantryPosition.geoMap.get(id).longtitude:0.0f) ;
    }

}
