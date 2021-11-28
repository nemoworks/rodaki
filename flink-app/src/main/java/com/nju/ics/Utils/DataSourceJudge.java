package com.nju.ics.Utils;

import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;

public class DataSourceJudge {
    public static final String sourceKey = "_source";
    public static final String timeKey = "_time";
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
        if (element.containsKey("POINTTIME")) {
            // 车道牌识数据 识别时间
            element.put(sourceKey, laneDetect);
            return laneDetect;

        } else if (element.containsKey("EXTOLLSTATIONHEX")) {
            // 说明是出口车道数据 出口站HEX编码
            element.put(sourceKey, exitLane);
            return exitLane;

        } else if (element.containsKey("ENWEIGHT")) {
            // System.out.println("入口");入口重量
            element.put(sourceKey, entryLane);
            return entryLane;
        } else if (element.containsKey("TRANSTIME")) {
            // System.out.println("门架"); 计费交易时间
            // 说明是门架计费扣费数据
            element.put(sourceKey, gantryCharge);
            return gantryCharge;
        } else if (element.containsKey("CAMERANUM")) {
            // 说明是门架牌识数据，因为 牌识编号
            element.put(sourceKey, gantryDetect);
            return gantryDetect;
        }
        return unknown;
    }

    public static long typeDetectAndTime(JSONObject element, SimpleDateFormat time) {
        if (element.containsKey("POINTTIME")) {
            // 车道牌识数据 识别时间
            element.put(sourceKey, laneDetect);
            try {
                return time.parse(element.getString("POINTTIME")).getTime();
            } catch (Exception e) {
                System.out.println(laneDetect);
            }

        } else if (element.containsKey("EXTOLLSTATIONHEX")) {
            // 说明是出口车道数据 出口站HEX编码
            element.put(sourceKey, exitLane);
            try {
                return time.parse(element.getString("TRIGGERTIME")).getTime();
            } catch (Exception e) {
                System.out.println(exitLane);
            }

        } else if (element.containsKey("TRANSTIME")) {
            // System.out.println("门架"); 计费交易时间
            // 说明是门架计费扣费数据
            element.put(sourceKey, gantryCharge);
            try {
                return time.parse(element.getString("TRANSTIME")).getTime();
            } catch (Exception e) {
                System.out.println(gantryCharge);
            }
        } else if (element.containsKey("CAMERANUM")) {
            // 说明是门架牌识数据，因为 牌识编号
            element.put(sourceKey, gantryDetect);
            try {
                return time.parse(element.getString("STATIONMATCHTIME")).getTime();
            } catch (Exception e) {
                System.out.println(gantryDetect);
            }
        } else if (element.containsKey("CARDID")) {
            // System.out.println("入口");入口重量 
            element.put(sourceKey, entryLane);
            try {
                return time.parse(element.getString("TRIGGERTIME")).getTime();
            } catch (Exception e) {
                System.out.println(element);
                System.exit(0);
            }
        }
        return 0;
    }
}
