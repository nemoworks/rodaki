package com.nju.ics.models;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;

/**
 * 支付记录,只在出口记录产生
 */
@JSONType(parseFeatures = { Feature.IgnoreNotMatch })
public class PaymentRecord extends AbstractModel {
    public String invoiceRecordId;
    /** 出口:通行标识ID */
    private String id;

    /** 出口：支付类型 */
    private int payType;
    /** 卡面扣费金额 */
    private int transFee;
    /** 交易类型 */
    private String transType;
    /** PSAM卡脱机交易序号 */
    private String terminalTransNo;
    /** 终端机编号 */
    private String terminalNo;
    /** 交易服务类型 */
    private int serviceType;
    /** 算法标识 */
    private int algorithmIdentifier;
    /** 计费里程（米） */
    private int tollDistance;
    /** 实际里程(米) */
    private int realDistance;
    /** 免费区间类型 */
    // private int freeType;
    /** 免费方式 */
    private int freeMode;
    /** 免费区域信息 */
    private String freeInfo;
    /** 交易前余额（分） */
    private int balanceBefore;
    /** 交易后余额（分） */
    private int balanceAfter;
    /** 总交易金额 */
    private int fee;
    /** 优惠金额 */
    private int discountFee;
    /** 应收金额 */
    private int payFee;
    /** 计费总里程数 */
    private String feeMileag;
    /** 代收外省金额 */
    private int collectFee;
    /** 折扣优惠金额(分) */
    private int rebateMoney;
    /** 卡成本金额(分) */
    private int cardCostFee;
    /** 欠通行费金额(分） */
    private int unpayFee;
    /** 欠通行费标志 */
    private String unpayFlag;
    /** 欠卡成本金额(分） */
    private int unpayCardCost;
    /** 次票代收 */
    private int ticketFee;
    /** 统缴金额 */
    private int unifiedFee;
    /** 应收通行费金额(分) */
    private int enTollMoney;
    /** 通行费免收金额（分） */
    private int enFreeMoney;
    /** 本次最终支付通行费金额 */
    private int enLastMoney;
    /** 补费记录号 */
    private String returnMoneySn;
    /** 支付卡类型 */
    private int payCardType;
    /** 支付卡网络号 */
    private String payCardNet;
    /** 支付卡卡号? */
    private String payCardId;
    /** 支付卡交易号 */
    private String payCardTranSn;
    /** 第三方支付信息 */
    private String payOrderNum;
    /** 支付码 */
    private String payCode;
    /** 折扣率(1‰) */
    private int payRebate;
    /** 开票标识 */
    private int identification;
    /** 参数版本组合 */
    private String paraVer;
    /** 省中心编号组合 */
    private String provinceGroup;
    /** 卡内门架编号组合 */
    private String gantryIdGroup;
    /** 收费单元数量 */
    private int tollIntervalsCount;
    /** 收费单元组合 */
    private String tollIntervalsGroup;
    /** 收费单元时间组合 */
    private String transTimeGroup;
    /** 收费单元实际收费金额组合 */
    private String chargefeeGroup;
    /** 收费单元优惠金额组合 */
    private String chargeDiscountGroup;
    /** 收费单元计费模块版本号组合 */
    private String rateModeVersionGroup;
    /** 收费单元计费参数版本号组合 */
    private String rateParaVersionGroup;
    /** 实收金额组合 */
    private String tollfeeGroup;
    /** 校验码 */
    private String verifyCode;
    /** 收费路段编号组合 */
    private String sectionGroup;
    /** 公路类型 */
    private String roadType;
    /** CPC卡省内累加金额 */
    private int feeProvInfo;
    /** 最短路径交易金额 */
    private int shortFee;
    /** 最短计费里程 */
    private int shortFeeMileage;
    /** 交易金额占比 */
    private float feeRate;
    /** 实际计费方式 */
    private int actualFeeClass;
    /** 计费模式 */
    private int chargeMode;
    /** 费显显示信息 */
    private int feeBoardPlay;
    /** 最短路径计费参数版本号 */
    private String spcRateVersion;
    /** 查验标志 */
    private String checkSign;
    /** 校检标志 */
    private int verifyFlag;
    /** 合法性验证通过的时间 */
    private String verifyPassTime;
    /** 备注 */
    private String remarks;
    /** 省中心优惠类型 */
    private int discountType;
    /** 省中心优惠金额 */
    private int provinceDiscountFee;
    /** 省中心优惠前交易金额 */
    private int originFee;
    /** 二次计费类型 */
    private int secFreeType;
    /** 二次计费结果 */
    private int secProResult;
    /** 二次计费时间 */
    private String secProTime;
    /** 省中心二次计费前交易金额 */
    private int secBefFee;
    /** 省中心二次计费优惠金额 */
    private int secFreeFee;
    /** 次票代收处理信息 */
    private String ticketfeeinfo;

    /** 优免信息编号 */
    // private String appointId;

    @Override
    public String id() {
        // TODO Auto-generated method stub
        return id;
    }

    public PaymentRecord() {

    }

    public PaymentRecord(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getId() {
        return id;
    }

    @JSONField(alternateNames = { "通行标识ID" })
    public void setId(String id) {
        this.id = id;
    }

    public int getPayType() {
        return payType;
    }

    @JSONField(alternateNames = { "支付类型" }, parseFeatures = { Feature.IgnoreNotMatch })
    public void setPayType(int payType) {
        this.payType = payType;
    }

    public int getTransFee() {
        return transFee;
    }

    @JSONField(alternateNames = { "卡面扣费金额" })
    public void setTransFee(int transFee) {
        this.transFee = transFee;
    }

    public String getTransType() {
        return transType;
    }

    @JSONField(alternateNames = { "交易类型" })
    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getTerminalTransNo() {
        return terminalTransNo;
    }

    @JSONField(alternateNames = { "PSAM卡脱机交易序号" })
    public void setTerminalTransNo(String terminalTransNo) {
        this.terminalTransNo = terminalTransNo;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    @JSONField(alternateNames = { "终端机编号" })
    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public int getServiceType() {
        return serviceType;
    }

    @JSONField(alternateNames = { "交易服务类型" })
    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public int getAlgorithmIdentifier() {
        return algorithmIdentifier;
    }

    @JSONField(alternateNames = { "算法标识" })
    public void setAlgorithmIdentifier(int algorithmIdentifier) {
        this.algorithmIdentifier = algorithmIdentifier;
    }

    public int getTollDistance() {
        return tollDistance;
    }

    @JSONField(alternateNames = { "计费里程（米）" })
    public void setTollDistance(int tollDistance) {
        this.tollDistance = tollDistance;
    }

    public int getRealDistance() {
        return realDistance;
    }

    @JSONField(alternateNames = { "实际里程(米)" })
    public void setRealDistance(int realDistance) {
        this.realDistance = realDistance;
    }

    public int getFreeMode() {
        return freeMode;
    }

    @JSONField(alternateNames = { "免费方式" })
    public void setFreeMode(int freeMode) {
        this.freeMode = freeMode;
    }

    public String getFreeInfo() {
        return freeInfo;
    }

    @JSONField(alternateNames = { "免费区域信息" })
    public void setFreeInfo(String freeInfo) {
        this.freeInfo = freeInfo;
    }

    public int getBalanceBefore() {
        return balanceBefore;
    }

    @JSONField(alternateNames = { "交易前余额（分）" })
    public void setBalanceBefore(int balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public int getBalanceAfter() {
        return balanceAfter;
    }

    @JSONField(alternateNames = { "交易后余额（分）" })
    public void setBalanceAfter(int balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public int getFee() {
        return fee;
    }

    @JSONField(alternateNames = { "总交易金额" })
    public void setFee(int fee) {
        this.fee = fee;
    }

    public int getDiscountFee() {
        return discountFee;
    }

    @JSONField(alternateNames = { "优惠金额" })
    public void setDiscountFee(int discountFee) {
        this.discountFee = discountFee;
    }

    public int getPayFee() {
        return payFee;
    }

    @JSONField(alternateNames = { "应收金额" })
    public void setPayFee(int payFee) {
        this.payFee = payFee;
    }

    public String getFeeMileag() {
        return feeMileag;
    }

    @JSONField(alternateNames = { "计费总里程数" })
    public void setFeeMileag(String feeMileag) {
        this.feeMileag = feeMileag;
    }

    public int getCollectFee() {
        return collectFee;
    }

    @JSONField(alternateNames = { "代收外省金额" })
    public void setCollectFee(int collectFee) {
        this.collectFee = collectFee;
    }

    public int getRebateMoney() {
        return rebateMoney;
    }

    @JSONField(alternateNames = { "折扣优惠金额(分)" })
    public void setRebateMoney(int rebateMoney) {
        this.rebateMoney = rebateMoney;
    }

    public int getCardCostFee() {
        return cardCostFee;
    }

    @JSONField(alternateNames = { "卡成本金额(分)" })
    public void setCardCostFee(int cardCostFee) {
        this.cardCostFee = cardCostFee;
    }

    public int getUnpayFee() {
        return unpayFee;
    }

    @JSONField(alternateNames = { "欠通行费金额(分）" })
    public void setUnpayFee(int unpayFee) {
        this.unpayFee = unpayFee;
    }

    public String getUnpayFlag() {
        return unpayFlag;
    }

    @JSONField(alternateNames = { "欠通行费标志" })
    public void setUnpayFlag(String unpayFlag) {
        this.unpayFlag = unpayFlag;
    }

    public int getUnpayCardCost() {
        return unpayCardCost;
    }

    @JSONField(alternateNames = { "欠卡成本金额(分）" })
    public void setUnpayCardCost(int unpayCardCost) {
        this.unpayCardCost = unpayCardCost;
    }

    public int getTicketFee() {
        return ticketFee;
    }

    @JSONField(alternateNames = { "次票代收" })
    public void setTicketFee(int ticketFee) {
        this.ticketFee = ticketFee;
    }

    public int getUnifiedFee() {
        return unifiedFee;
    }

    @JSONField(alternateNames = { "统缴金额" })
    public void setUnifiedFee(int unifiedFee) {
        this.unifiedFee = unifiedFee;
    }

    public int getEnTollMoney() {
        return enTollMoney;
    }

    @JSONField(alternateNames = { "应收通行费金额(分)" })
    public void setEnTollMoney(int enTollMoney) {
        this.enTollMoney = enTollMoney;
    }

    public int getEnFreeMoney() {
        return enFreeMoney;
    }

    @JSONField(alternateNames = { "通行费免收金额（分）" })
    public void setEnFreeMoney(int enFreeMoney) {
        this.enFreeMoney = enFreeMoney;
    }

    public int getEnLastMoney() {
        return enLastMoney;
    }

    @JSONField(alternateNames = { "本次最终支付通行费金额" })
    public void setEnLastMoney(int enLastMoney) {
        this.enLastMoney = enLastMoney;
    }

    public String getReturnMoneySn() {
        return returnMoneySn;
    }

    @JSONField(alternateNames = { "补费记录号" })
    public void setReturnMoneySn(String returnMoneySn) {
        this.returnMoneySn = returnMoneySn;
    }

    public int getPayCardType() {
        return payCardType;
    }

    @JSONField(alternateNames = { "支付卡类型" })
    public void setPayCardType(int payCardType) {
        this.payCardType = payCardType;
    }

    public String getPayCardNet() {
        return payCardNet;
    }

    @JSONField(alternateNames = { "支付卡网络号" })
    public void setPayCardNet(String payCardNet) {
        this.payCardNet = payCardNet;
    }

    public String getPayCardId() {
        return payCardId;
    }

    @JSONField(alternateNames = { "支付卡卡号?" })
    public void setPayCardId(String payCardId) {
        this.payCardId = payCardId;
    }

    public String getPayCardTranSn() {
        return payCardTranSn;
    }

    @JSONField(alternateNames = { "支付卡交易号" })
    public void setPayCardTranSn(String payCardTranSn) {
        this.payCardTranSn = payCardTranSn;
    }

    public String getPayOrderNum() {
        return payOrderNum;
    }

    @JSONField(alternateNames = { "第三方支付信息" })
    public void setPayOrderNum(String payOrderNum) {
        this.payOrderNum = payOrderNum;
    }

    public String getPayCode() {
        return payCode;
    }

    @JSONField(alternateNames = { "支付码" })
    public void setPayCode(String payCode) {
        this.payCode = payCode;
    }

    public int getPayRebate() {
        return payRebate;
    }

    @JSONField(alternateNames = { "折扣率(1‰)" })
    public void setPayRebate(int payRebate) {
        this.payRebate = payRebate;
    }

    public int getIdentification() {
        return identification;
    }

    @JSONField(alternateNames = { "开票标识" })
    public void setIdentification(int identification) {
        this.identification = identification;
    }

    public String getParaVer() {
        return paraVer;
    }

    @JSONField(alternateNames = { "参数版本组合" })
    public void setParaVer(String paraVer) {
        this.paraVer = paraVer;
    }

    public String getProvinceGroup() {
        return provinceGroup;
    }

    @JSONField(alternateNames = { "省中心编号组合" })
    public void setProvinceGroup(String provinceGroup) {
        this.provinceGroup = provinceGroup;
    }

    public String getGantryIdGroup() {
        return gantryIdGroup;
    }

    @JSONField(alternateNames = { "卡内门架编号组合" })
    public void setGantryIdGroup(String gantryIdGroup) {
        this.gantryIdGroup = gantryIdGroup;
    }

    public int getTollIntervalsCount() {
        return tollIntervalsCount;
    }

    @JSONField(alternateNames = { "收费单元数量" })
    public void setTollIntervalsCount(int tollIntervalsCount) {
        this.tollIntervalsCount = tollIntervalsCount;
    }

    public String getTollIntervalsGroup() {
        return tollIntervalsGroup;
    }

    @JSONField(alternateNames = { "收费单元组合" })
    public void setTollIntervalsGroup(String tollIntervalsGroup) {
        this.tollIntervalsGroup = tollIntervalsGroup;
    }

    public String getTransTimeGroup() {
        return transTimeGroup;
    }

    @JSONField(alternateNames = { "收费单元时间组合" })
    public void setTransTimeGroup(String transTimeGroup) {
        this.transTimeGroup = transTimeGroup;
    }

    public String getChargefeeGroup() {
        return chargefeeGroup;
    }

    @JSONField(alternateNames = { "收费单元实际收费金额组合" })
    public void setChargefeeGroup(String chargefeeGroup) {
        this.chargefeeGroup = chargefeeGroup;
    }

    public String getChargeDiscountGroup() {
        return chargeDiscountGroup;
    }

    @JSONField(alternateNames = { "收费单元优惠金额组合" })
    public void setChargeDiscountGroup(String chargeDiscountGroup) {
        this.chargeDiscountGroup = chargeDiscountGroup;
    }

    public String getRateModeVersionGroup() {
        return rateModeVersionGroup;
    }

    @JSONField(alternateNames = { "收费单元计费模块版本号组合" })
    public void setRateModeVersionGroup(String rateModeVersionGroup) {
        this.rateModeVersionGroup = rateModeVersionGroup;
    }

    public String getRateParaVersionGroup() {
        return rateParaVersionGroup;
    }

    @JSONField(alternateNames = { "收费单元计费参数版本号组合" })
    public void setRateParaVersionGroup(String rateParaVersionGroup) {
        this.rateParaVersionGroup = rateParaVersionGroup;
    }

    public String getTollfeeGroup() {
        return tollfeeGroup;
    }

    @JSONField(alternateNames = { "实收金额组合" })
    public void setTollfeeGroup(String tollfeeGroup) {
        this.tollfeeGroup = tollfeeGroup;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    @JSONField(alternateNames = { "校验码" })
    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getSectionGroup() {
        return sectionGroup;
    }

    @JSONField(alternateNames = { "收费路段编号组合" })
    public void setSectionGroup(String sectionGroup) {
        this.sectionGroup = sectionGroup;
    }

    public String getRoadType() {
        return roadType;
    }

    @JSONField(alternateNames = { "公路类型" }, parseFeatures = { Feature.IgnoreNotMatch })
    public void setRoadType(String roadType) {
        this.roadType = roadType;
    }

    public int getFeeProvInfo() {
        return feeProvInfo;
    }

    @JSONField(alternateNames = { "CPC卡省内累加金额" })
    public void setFeeProvInfo(int feeProvInfo) {
        this.feeProvInfo = feeProvInfo;
    }

    public int getShortFee() {
        return shortFee;
    }

    @JSONField(alternateNames = { "最短路径交易金额" })
    public void setShortFee(int shortFee) {
        this.shortFee = shortFee;
    }

    public int getShortFeeMileage() {
        return shortFeeMileage;
    }

    @JSONField(alternateNames = { "最短计费里程" })
    public void setShortFeeMileage(int shortFeeMileage) {
        this.shortFeeMileage = shortFeeMileage;
    }

    public float getFeeRate() {
        return feeRate;
    }

    @JSONField(alternateNames = { "交易金额占比" })
    public void setFeeRate(float feeRate) {
        this.feeRate = feeRate;
    }

    public int getActualFeeClass() {
        return actualFeeClass;
    }

    @JSONField(alternateNames = { "实际计费方式" })
    public void setActualFeeClass(int actualFeeClass) {
        this.actualFeeClass = actualFeeClass;
    }

    public int getChargeMode() {
        return chargeMode;
    }

    @JSONField(alternateNames = { "计费模式" })
    public void setChargeMode(int chargeMode) {
        this.chargeMode = chargeMode;
    }

    public int getFeeBoardPlay() {
        return feeBoardPlay;
    }

    @JSONField(alternateNames = { "费显显示信息" })
    public void setFeeBoardPlay(int feeBoardPlay) {
        this.feeBoardPlay = feeBoardPlay;
    }

    public String getSpcRateVersion() {
        return spcRateVersion;
    }

    @JSONField(alternateNames = { "最短路径计费参数版本号" })
    public void setSpcRateVersion(String spcRateVersion) {
        this.spcRateVersion = spcRateVersion;
    }

    public String getCheckSign() {
        return checkSign;
    }

    @JSONField(alternateNames = { "查验标志" })
    public void setCheckSign(String checkSign) {
        this.checkSign = checkSign;
    }

    public int getVerifyFlag() {
        return verifyFlag;
    }

    @JSONField(alternateNames = { "校检标志" })
    public void setVerifyFlag(int verifyFlag) {
        this.verifyFlag = verifyFlag;
    }

    public String getVerifyPassTime() {
        return verifyPassTime;
    }

    @JSONField(alternateNames = { "合法性验证通过的时间" })
    public void setVerifyPassTime(String verifyPassTime) {
        this.verifyPassTime = verifyPassTime;
    }

    public String getRemarks() {
        return remarks;
    }

    @JSONField(alternateNames = { "备注" })
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getDiscountType() {
        return discountType;
    }

    @JSONField(alternateNames = { "省中心优惠类型" })
    public void setDiscountType(int discountType) {
        this.discountType = discountType;
    }

    public int getProvinceDiscountFee() {
        return provinceDiscountFee;
    }

    @JSONField(alternateNames = { "省中心优惠金额" })
    public void setProvinceDiscountFee(int provinceDiscountFee) {
        this.provinceDiscountFee = provinceDiscountFee;
    }

    public int getOriginFee() {
        return originFee;
    }

    @JSONField(alternateNames = { "省中心优惠前交易金额" })
    public void setOriginFee(int originFee) {
        this.originFee = originFee;
    }

    public int getSecFreeType() {
        return secFreeType;
    }

    @JSONField(alternateNames = { "二次计费类型" })
    public void setSecFreeType(int secFreeType) {
        this.secFreeType = secFreeType;
    }

    public int getSecProResult() {
        return secProResult;
    }

    @JSONField(alternateNames = { "二次计费结果" })
    public void setSecProResult(int secProResult) {
        this.secProResult = secProResult;
    }

    public String getSecProTime() {
        return secProTime;
    }

    @JSONField(alternateNames = { "二次计费时间" })
    public void setSecProTime(String secProTime) {
        this.secProTime = secProTime;
    }

    public int getSecBefFee() {
        return secBefFee;
    }

    @JSONField(alternateNames = { "省中心二次计费前交易金额" })
    public void setSecBefFee(int secBefFee) {
        this.secBefFee = secBefFee;
    }

    public int getSecFreeFee() {
        return secFreeFee;
    }

    @JSONField(alternateNames = { "省中心二次计费优惠金额" })
    public void setSecFreeFee(int secFreeFee) {
        this.secFreeFee = secFreeFee;
    }

    public String getTicketfeeinfo() {
        return ticketfeeinfo;
    }

    @JSONField(alternateNames = { "次票代收处理信息" })
    public void setTicketfeeinfo(String ticketfeeinfo) {
        this.ticketfeeinfo = ticketfeeinfo;
    }

}
