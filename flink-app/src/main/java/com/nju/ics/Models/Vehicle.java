package com.nju.ics.Models;

import com.nju.ics.Annotation.IotDBAnnotion.sinkIotDB;
import com.nju.ics.FastJsonUtils.IntDeserializer;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.Feature;

/**
 * 收集模型里的
 * 
 * 1. 2. 4. 5. 6. 10. 11. 15. 16. 17. 18. 19. 20. 21. 22.
 */
public class Vehicle extends AbstractModel {
    /** 入口：车牌颜色 出口： 出口实际车牌颜色 门架：计费车牌颜色 */
    public int color;
    /** 入口：车牌号 出口：出口实际车牌号 门架：计费车牌号 */
    public String number;
    /** 入口：车种 出口：出口车种 门架：车种 */
    // private int vehicleClass;
    /** 入口：收费车型 出口：出口车型 门架：计费车型 */
    private int vehicleType;
    /** 门架：车辆座位数/载重 */
    private int vehicleSeat;
    /** 门架：车辆长 */
    private int vehicleLength;
    /** 门架：车辆宽 */
    private int vehicleWidth;
    /** 门架：车辆高 */
    private int vehicleHight;

    /** 入口、出口：轴组信息 */
    private String axisInfo;
    /** 入口：入口轴数，门架：车轴数，出口：轴数、入口轴数 */
    private int axleCount;

    /** 入口、出口：限载总重(kg) */
    private int limitWeight;
    private String passID;
    // 是否办理ETC
    // private boolean isETC;
    /** 入口、出口、门架：OBU单/双片标识 */
    // private int obuSign;
    /** 入口、出口：OBU编号 门架：OBU/CPC序号编码 */
    // private String obuId;
    /** 入口：ETC/CPC卡号? 出口：通行介质编码 (这里数据文件的字段与数据库字段不符) */
    // private String etcCardNumber;

    /**
     * 经过站点交易流水号 入口：交易流水号 门架：计费交易编号 出口：交易流水号
     */
    // private String transactionSerialNumber;
    /**
     * 经过站点类型 入口：1 门架：2 出口：3
     */
    // private int passSiteType;
    /**
     * 经过站点编号 入口：入口站号 门架：门架编号 出口：出口站号
     */
    // private String passSiteNumber;
    /**
     * 经过站点HEX编码 入口：入口站HEX编码 门架：门架HEX值 出口：出口站HEX编码
     */
    // private String passSiteHex;
    /**
     * 经过站点编号（国标） 入口：入口站号(国标) 门架： 出口：出口站号(国标)
     */
    // private String passSiteNumberNational;

    /** 入口：入口车道号 出口：出口车道号 */
    // private String passlaneNumber;
    /** 入口：入口车道号(国标) 出口：出口车道号(国标) */
    // private String passlaneNumberNational;
    /** 入口：入口车道HEX编码 出口：出口车道HEX编码 */
    // private String passlaneHex;

    /** 最近经过门架/站点的时间戳，单位：毫秒 */
    private long lastPassTimes;
    /** 入口、出口：操作员工号 */
    // private String passoperId;
    /** 入口、出口：操作员姓名 */
    // private String passoperName;

    /** 位置 */
    // @sinkIotDB
    // public String location;

    /**
     * 经过站点时被识别的车牌 入口：识别车牌号 门架： 出口：识别车牌号
     */
    // private String identifyVlp;
    /**
     * 经过站点时被识别的车牌颜色 入口：识别车牌颜色 门架： 出口：识别车牌颜色
     */
    // private int identifyVlpc;
    /**
     * 经过站点时通行介质类型 入口：通行介质类型 门架：+1 出口：+1
     * 
     */
    private int mediaType;

    /**
     * 出口：通行介质编码
     */
    private String mediaNo;

    /** 入口:入口重量 出口：出口重量 */
    // @sinkIotDB
    // private int weight;

    /**
     * 入口：超限率 出口：超限率
     * 
     */
    // private int overWeightRate;

    /**
     * 通行费用 入口： 门架：交易金额 出口：总交易金额
     */
    private int fee;
    /**
     * 通行里程 入口： 门架：计费里程数 出口：计费总里程数
     */
    private int feeMileAge;

    /** 车速 */
    // @sinkIotDB
    // public int speed;
    public String getPassID() {
        return passID;
    }
    @JSONField(alternateNames = { "PASSID"})
    public void setPassID(String passID) {
        this.passID = passID;
    }

    /** 最近经过的门架/站点 */
    @sinkIotDB
    public String lastPassStation;

    public long getLastPassTimes() {
        return lastPassTimes;
    }

    @JSONField(deserialize = false)
    public void setLastPassTimes(long lastPassTimes) {
        this.lastPassTimes = lastPassTimes;
    }

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return String.format("%s-%d", number, color);
    }

    public Vehicle() {
    }

    public int getColor() {
        return color;
    }

    // "车牌颜色", "出口实际车牌颜色", "计费车牌颜色"
    @JSONField(alternateNames = { "VLPC", "EXVLPC" }, deserializeUsing = IntDeserializer.class)
    public void setColor(int color) {
        this.color = color;
    }

    public String getNumber() {
        return number;
    }

    // "车牌号", "出口实际车牌号", "计费车牌号"
    @JSONField(alternateNames = { "VLP", "EXVLP" })
    public void setNumber(String number) {
        this.number = number;
    }

    // public int getVehicleClass() {
    // return vehicleClass;
    // }
    // "车种", "出口车种"
    // @JSONField(alternateNames = { "VEHICLECLASS", "EXVEHICLECLASS" },
    // deserializeUsing = IntDeserializer.class)
    // public void setVehicleClass(int vehicleClass) {
    // this.vehicleClass = vehicleClass;
    // }

    public int getVehicleType() {
        return vehicleType;
    }

    // "收费车型", "出口车型", "计费车型"
    @JSONField(alternateNames = { "VEHICLETYPE", "EXVEHICLETYPE" }, deserializeUsing = IntDeserializer.class)
    public void setVehicleType(int vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getVehicleSeat() {
        return vehicleSeat;
    }

    // "车辆座位数/载重"
    @JSONField(alternateNames = { "VEHICLESEAT" }, deserializeUsing = IntDeserializer.class)
    public void setVehicleSeat(int vehicleSeat) {
        this.vehicleSeat = vehicleSeat;
    }

    public int getVehicleLength() {
        return vehicleLength;
    }

    // 车辆长
    @JSONField(alternateNames = { "VEHICLELENGTH" }, deserializeUsing = IntDeserializer.class)
    public void setVehicleLength(int vehicleLength) {
        this.vehicleLength = vehicleLength;
    }

    public int getVehicleWidth() {
        return vehicleWidth;
    }

    // 车辆宽
    @JSONField(alternateNames = { "VEHICLEWIDTH" }, deserializeUsing = IntDeserializer.class)
    public void setVehicleWidth(int vehicleWidth) {
        this.vehicleWidth = vehicleWidth;
    }

    public int getVehicleHight() {
        return vehicleHight;
    }

    // 车辆高
    @JSONField(alternateNames = { "VEHICLEHIGHT" }, deserializeUsing = IntDeserializer.class)
    public void setVehicleHight(int vehicleHight) {
        this.vehicleHight = vehicleHight;
    }

    public String getAxisInfo() {
        return axisInfo;
    }

    // 轴组信息
    @JSONField(alternateNames = { "AXISINFO" })
    public void setAxisInfo(String axisInfo) {
        this.axisInfo = axisInfo;
    }

    public int getAxleCount() {
        return axleCount;
    }

    // "入口轴数", "车轴数", "轴数"
    @JSONField(alternateNames = { "AXLECOUNT", "ENAXLECOUNT" }, deserializeUsing = IntDeserializer.class)
    public void setAxleCount(int axleCount) {
        this.axleCount = axleCount;
    }

    public int getLimitWeight() {
        return limitWeight;
    }

    // 限载总重(kg)
    @JSONField(alternateNames = { "LIMITWEIGHT" }, deserializeUsing = IntDeserializer.class)
    public void setLimitWeight(int limitWeight) {
        this.limitWeight = limitWeight;
    }

    // public int getWeight() {
    // return weight;
    // }

    // @JSONField(alternateNames = { "入口重量", "出口重量" }, deserializeUsing =
    // IntDeserializer.class)
    // public void setWeight(int weight) {
    // this.weight = weight;
    // }

    // public boolean isETC() {
    // return isETC;
    // }

    // @JSONField(deserialize = false)
    // public void setETC(boolean isETC) {
    // this.isETC = isETC;
    // }

    // public int getObuSign() {
    // return obuSign;
    // }

    // @JSONField(alternateNames = { "OBU单/双片标识" }, deserializeUsing =
    // IntDeserializer.class)
    // public void setObuSign(int obuSign) {
    // this.obuSign = obuSign;
    // this.isETC = true;
    // }

    // public String getObuId() {
    // return obuId;
    // }

    // @JSONField(alternateNames = { "OBU编号", "OBU/CPC序号编码" })
    // public void setObuId(String obuId) {
    // this.obuId = obuId;
    // this.isETC = true;
    // }

    // public String getEtcCardNumber() {
    // return etcCardNumber;
    // }

    // @JSONField(alternateNames = { "ETC/CPC卡号?", "通行介质编码" })
    // public void setEtcCardNumber(String etcCardNumber) {
    // this.etcCardNumber = etcCardNumber;
    // this.isETC = true;
    // }

    // public String getTransactionSerialNumber() {
    // return transactionSerialNumber;
    // }

    // @JSONField(alternateNames = { "交易流水号", "计费交易编号" })
    // public void setTransactionSerialNumber(String transactionSerialNumber) {
    // this.transactionSerialNumber = transactionSerialNumber;
    // }

    // public int getPassSiteType() {
    // return passSiteType;
    // }

    // @JSONField(deserialize = false)
    // public void setPassSiteType(int passSiteType) {
    // this.passSiteType = passSiteType;
    // }

    // public String getPassSiteNumber() {
    // return passSiteNumber;
    // }

    // @JSONField(alternateNames = { "入口站号", "门架编号", "出口站号" })
    // public void setPassSiteNumber(String passSiteNumber) {
    // this.passSiteNumber = passSiteNumber;
    // this.lastPassStation = passSiteNumber;
    // }

    // public String getPassSiteHex() {
    // return passSiteHex;
    // }

    // @JSONField(alternateNames = { "入口站HEX编码", "门架HEX值", "出口站HEX编码" })
    // public void setPassSiteHex(String passSiteHex) {
    // this.passSiteHex = passSiteHex;
    // }

    // public String getPassSiteNumberNational() {
    // return passSiteNumberNational;
    // }

    // @JSONField(alternateNames = { "入口站号(国标)", "出口站号(国标)" })
    // public void setPassSiteNumberNational(String passSiteNumberNational) {
    // this.passSiteNumberNational = passSiteNumberNational;
    // }

    // public String getPasslaneNumber() {
    // return passlaneNumber;
    // }

    // @JSONField(alternateNames = { "入口车道号", "出口车道号" })
    // public void setPasslaneNumber(String passlaneNumber) {
    // this.passlaneNumber = passlaneNumber;
    // }

    // public String getPasslaneNumberNational() {
    // return passlaneNumberNational;
    // }

    // @JSONField(alternateNames = { "入口车道号(国标)", "出口车道号(国标)" })
    // public void setPasslaneNumberNational(String passlaneNumberNational) {
    // this.passlaneNumberNational = passlaneNumberNational;
    // }

    // public String getPasslaneHex() {
    // return passlaneHex;
    // }

    // @JSONField(alternateNames = { "入口车道HEX编码", "出口车道HEX编码" })
    // public void setPasslaneHex(String passlaneHex) {
    // this.passlaneHex = passlaneHex;
    // }

    // public String getPassoperId() {
    // return passoperId;
    // }

    // @JSONField(alternateNames = { "操作员工号" })
    // public void setPassoperId(String passoperId) {
    // this.passoperId = passoperId;
    // }

    // public String getPassoperName() {
    // return passoperName;
    // }

    // @JSONField(alternateNames = { "操作员姓名" })
    // public void setPassoperName(String passoperName) {
    // this.passoperName = passoperName;
    // }

    // public String getIdentifyVlp() {
    // return identifyVlp;
    // }

    // @JSONField(alternateNames = { "识别车牌号" })
    // public void setIdentifyVlp(String identifyVlp) {
    // this.identifyVlp = identifyVlp;
    // }

    // public int getIdentifyVlpc() {
    // return identifyVlpc;
    // }

    // @JSONField(alternateNames = { "识别车牌颜色" })
    // public void setIdentifyVlpc(int identifyVlpc) {
    // this.identifyVlpc = identifyVlpc;
    // }

    public int getMediaType() {
        return mediaType;
    }

    // 通行介质类型
    @JSONField(alternateNames = { "MEDIATYPE" })
    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaNo() {
        return mediaNo;
    }

    // 通行介质编码
    @JSONField(alternateNames = { "MEDIANO" })
    public void setMediaNo(String mediaNo) {
        this.mediaNo = mediaNo;
    }

    // public int getOverWeightRate() {
    // return overWeightRate;
    // }

    // @JSONField(alternateNames = { "超限率" })
    // public void setOverWeightRate(int overWeightRate) {
    // this.overWeightRate = overWeightRate;
    // }

    public int getFee() {
        return fee;
    }

    // "交易金额","总交易金额"
    @JSONField(alternateNames = { "FEE" })
    public void setFee(int fee) {
        this.fee = fee;
    }

    public int getFeeMileAge() {
        return feeMileAge;
    }

    // "计费里程数","计费总里程数"
    @JSONField(alternateNames = { "FEEMILEAGE" })
    public void setFeeMileAge(int feeMileAge) {
        this.feeMileAge = feeMileAge;
    }

}
