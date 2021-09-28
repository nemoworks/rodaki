package com.nju.ics.Models;

import com.alibaba.fastjson.annotation.JSONField;

public class GantryRecord extends AbstractModel {
    /** class Gantry */
    public String gantryId;
    /** 门架：计费交易编号 */
    public String tradeId;
    /** 门架：计费交易时间 */
    public String transTime;
    /** class Media */
    public String mediaId;

    /** v3 */
    /** 门架拟合结果 */
    private int gantryFitResult;
    /** 应收金额 */
    private int payFee;
    /** 交易金额 */
    private int fee;

    /** 优惠金额 */
    private int discountFee;
    /** 识别的车型 */
    private int identifyVehicleType;

    /** TAC码 */
    private String tac;
    /** PSAM卡脱机交易序列号 */
    private String terminalTransNo;
    /** 交易的服务类型 */
    private int serviceType;
    /** 算法标识 */
    private int algorithmIdentifier;
    /** 消费密钥版本号 */
    private String keyVersion;
    /** 天线ID编号 */
    private int antennaID;
    /** 计费模块版本号 */
    private String tollModeVer;
    /** 计费参数版本号 */
    private String tollParaVer;
    /** 交易耗时（单位：ms） */
    private int consumeTime;
    /** 通行状态 */
    private int passState;
    /** 计费模块响应计费结果 */
    private int feeCalcResult;
    /** 节假日状态 */
    private int holidayState;
    /** 交易结果 */
    private int tradeResult;
    /** 特情类型 */
    private int specialType;
    /** 匹配状态 */
    private int matchStatus;
    /** 去重状态 */
    private int validStatus;
    /** 处理状态 */
    private int dealStatus;
    /** 计费接口特情值 */
    private int feeCalcSpecial;
    /** 收费特情类型 */
    private String chargesSpecialType;
    /** 是否修正过 */
    private int isFixData;
    /** 标签写入结果 */
    private int obuTradeResult;
    /** 交易类型 */
    private int tradeType;
    /** 计费里程数 */
    private int feeMileage;
    /** 本次计费拟合结果标识 */
    private int pathFitFlag;
    /** 计费特情值组合 */
    private int feeCalcSpecials;
    /** 更新过站信息结果 */
    private int updateResult;
    /** 收费单元处理标识 */
    private String tollIntervalSign;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return tradeId;
    }

    public GantryRecord() {
    }

    public GantryRecord(String tradeId, String transTime) {

        this.tradeId = tradeId == null ? null : tradeId.trim();
        this.transTime = transTime == null ? null : transTime.trim();

    }
    
    public String getTradeId() {
        return tradeId;
    }
    @JSONField(alternateNames = {"计费交易编号"})
    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getTransTime() {
        return transTime;
    }
    @JSONField(alternateNames = {"计费交易时间"})
    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public int getGantryFitResult() {
        return gantryFitResult;
    }
    @JSONField(alternateNames = {"门架拟合结果"})
    public void setGantryFitResult(int gantryFitResult) {
        this.gantryFitResult = gantryFitResult;
    }

    public int getPayFee() {
        return payFee;
    }
    @JSONField(alternateNames = {"应收金额"})
    public void setPayFee(int payFee) {
        this.payFee = payFee;
    }

    public int getFee() {
        return fee;
    }
    @JSONField(alternateNames = {"交易金额"})
    public void setFee(int fee) {
        this.fee = fee;
    }

    public int getDiscountFee() {
        return discountFee;
    }
    @JSONField(alternateNames = {"优惠金额"})
    public void setDiscountFee(int discountFee) {
        this.discountFee = discountFee;
    }

    public int getIdentifyVehicleType() {
        return identifyVehicleType;
    }
    @JSONField(alternateNames = {"识别的车型"})
    public void setIdentifyVehicleType(int identifyVehicleType) {
        this.identifyVehicleType = identifyVehicleType;
    }

    public String getTac() {
        return tac;
    }
    @JSONField(alternateNames = {"TAC码"})
    public void setTac(String tac) {
        this.tac = tac;
    }

    public String getTerminalTransNo() {
        return terminalTransNo;
    }
    @JSONField(alternateNames = {"PSAM卡脱机交易序列号"})
    public void setTerminalTransNo(String terminalTransNo) {
        this.terminalTransNo = terminalTransNo;
    }

    public int getServiceType() {
        return serviceType;
    }
    @JSONField(alternateNames = {"交易的服务类型"})
    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public int getAlgorithmIdentifier() {
        return algorithmIdentifier;
    }
    @JSONField(alternateNames = {"算法标识"})
    public void setAlgorithmIdentifier(int algorithmIdentifier) {
        this.algorithmIdentifier = algorithmIdentifier;
    }

    public String getKeyVersion() {
        return keyVersion;
    }
    @JSONField(alternateNames = {"消费密钥版本号"})
    public void setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
    }

    public int getAntennaID() {
        return antennaID;
    }
    @JSONField(alternateNames = {"天线ID编号"})
    public void setAntennaID(int antennaID) {
        this.antennaID = antennaID;
    }

    public String getTollModeVer() {
        return tollModeVer;
    }
    @JSONField(alternateNames = {"计费模块版本号"})
    public void setTollModeVer(String tollModeVer) {
        this.tollModeVer = tollModeVer;
    }

    public String getTollParaVer() {
        return tollParaVer;
    }
    @JSONField(alternateNames = {"计费参数版本号"})
    public void setTollParaVer(String tollParaVer) {
        this.tollParaVer = tollParaVer;
    }

    public int getConsumeTime() {
        return consumeTime;
    }
    @JSONField(alternateNames = {"交易耗时（单位：ms）"})
    public void setConsumeTime(int consumeTime) {
        this.consumeTime = consumeTime;
    }

    public int getPassState() {
        return passState;
    }
    @JSONField(alternateNames = {"通行状态"})
    public void setPassState(int passState) {
        this.passState = passState;
    }

    public int getFeeCalcResult() {
        return feeCalcResult;
    }
    @JSONField(alternateNames = {"计费模块响应计费结果"})
    public void setFeeCalcResult(int feeCalcResult) {
        this.feeCalcResult = feeCalcResult;
    }

    public int getHolidayState() {
        return holidayState;
    }
    @JSONField(alternateNames = {"节假日状态"})
    public void setHolidayState(int holidayState) {
        this.holidayState = holidayState;
    }

    public int getTradeResult() {
        return tradeResult;
    }
    @JSONField(alternateNames = {"交易结果"})
    public void setTradeResult(int tradeResult) {
        this.tradeResult = tradeResult;
    }

    public int getSpecialType() {
        return specialType;
    }
    @JSONField(alternateNames = {"特情类型"})
    public void setSpecialType(int specialType) {
        this.specialType = specialType;
    }

    public int getMatchStatus() {
        return matchStatus;
    }
    @JSONField(alternateNames = {"匹配状态"})
    public void setMatchStatus(int matchStatus) {
        this.matchStatus = matchStatus;
    }

    public int getValidStatus() {
        return validStatus;
    }
    @JSONField(alternateNames = {"去重状态"})
    public void setValidStatus(int validStatus) {
        this.validStatus = validStatus;
    }

    public int getDealStatus() {
        return dealStatus;
    }
    @JSONField(alternateNames = {"处理状态"})
    public void setDealStatus(int dealStatus) {
        this.dealStatus = dealStatus;
    }

    public int getFeeCalcSpecial() {
        return feeCalcSpecial;
    }
    @JSONField(alternateNames = {"计费接口特情值"})
    public void setFeeCalcSpecial(int feeCalcSpecial) {
        this.feeCalcSpecial = feeCalcSpecial;
    }

    public String getChargesSpecialType() {
        return chargesSpecialType;
    }
    @JSONField(alternateNames = {"收费特情类型"})
    public void setChargesSpecialType(String chargesSpecialType) {
        this.chargesSpecialType = chargesSpecialType;
    }

    public int getIsFixData() {
        return isFixData;
    }
    @JSONField(alternateNames = {"是否修正过"})
    public void setIsFixData(int isFixData) {
        this.isFixData = isFixData;
    }

    public int getObuTradeResult() {
        return obuTradeResult;
    }
    @JSONField(alternateNames = {"标签写入结果"})
    public void setObuTradeResult(int obuTradeResult) {
        this.obuTradeResult = obuTradeResult;
    }

    public int getTradeType() {
        return tradeType;
    }
    @JSONField(alternateNames = {"交易类型"})
    public void setTradeType(int tradeType) {
        this.tradeType = tradeType;
    }

    public int getFeeMileage() {
        return feeMileage;
    }
    @JSONField(alternateNames = {"计费里程数"})
    public void setFeeMileage(int feeMileage) {
        this.feeMileage = feeMileage;
    }

    public int getPathFitFlag() {
        return pathFitFlag;
    }
    @JSONField(alternateNames = {"本次计费拟合结果标识"})
    public void setPathFitFlag(int pathFitFlag) {
        this.pathFitFlag = pathFitFlag;
    }

    public int getFeeCalcSpecials() {
        return feeCalcSpecials;
    }
    @JSONField(alternateNames = {"计费特情值组合"})
    public void setFeeCalcSpecials(int feeCalcSpecials) {
        this.feeCalcSpecials = feeCalcSpecials;
    }

    public int getUpdateResult() {
        return updateResult;
    }
    @JSONField(alternateNames = {"更新过站信息结果"})
    public void setUpdateResult(int updateResult) {
        this.updateResult = updateResult;
    }

    public String getTollIntervalSign() {
        return tollIntervalSign;
    }
    @JSONField(alternateNames = {"收费单元处理标识"})
    public void setTollIntervalSign(String tollIntervalSign) {
        this.tollIntervalSign = tollIntervalSign;
    }
    
}
