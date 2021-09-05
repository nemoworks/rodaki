package com.nju.ics.Utils;

import com.alibaba.fastjson.JSONObject;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;

public class DataSourceJudge {
    /** 入口车道数据 */
    public static final int entryLane = 1;
    /** 出口车道数据 */
    public static final int exitLane = 2;

    /** 门架计费扣费数据 */
    public static final int gantryCharge = 3;
    /** 车道牌识数据 */
    public static final int laneDetect = 4;

    /** 门架牌识数据 */
    public static final int gantryDetect = 5;

    public static final int unknown = -1;

    /**
     * 从数据是否含有某个字段来判断其属于哪一个来源
     * 
     * <ol>
     * <li>入口车道数据:1</li>
     * <li>出口车道数据:2</li>
     * <li>车道牌识数据:3</li>
     * <li>门架计费扣费数据:4</li>
     * <li>门架牌识数据:5</li>
     * </ol>
     * 无法识别则是-1 <br>
     * <br>
     * 
     * @param element
     * @return
     * 
     * 
     * 
     * 
     */
    public static int typeDetect(JSONObject element) {
        if (element.containsKey("识别时间")) {
            // 车道牌识数据
            return laneDetect;

        } else if (element.containsKey("出口站HEX编码")) {
            // 说明是出口车道数据
            return exitLane;

        } else if (element.containsKey("入口重量")) {
            return entryLane;
        } else if (element.containsKey("计费交易时间")) {
            // 说明是门架计费扣费数据
            return gantryCharge;
        } else if (element.containsKey("牌识编号")) {
            // 说明是门架牌识数据，因为
            return gantryDetect;
        }
        return unknown;
    }
}
