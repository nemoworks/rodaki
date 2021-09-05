package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;

public class Plate extends AbstractModel {
    /**入口：车牌颜色  出口： 出口实际车牌颜色 门架：计费车牌颜色*/
    public int color;
    /**入口：车牌号  出口：出口实际车牌号 门架：计费车牌号*/
    public String number;

    public Plate() {
    }

    public Plate(int color, String number) {
        this.color = color;
        this.number =number==null?null: number.trim();
    }

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return String.format("%s-%d", number, color);
    }

    
    
}
