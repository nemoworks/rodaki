function mapToDittoProtocolMsg(headers,textPayload,bytePayload,contentType){
    const jsonData = JSON.parse(textPayload);

    const namespace = "ics.rodaki";

    const name_part1 = "paymentrecord";
    const name_part2 = jsonData.id;

    const full_name = name_part1 + "-" + name_part2;
    const policy = "ics.rodaki:base";
    const def = "ics.rodaki:" + name_part1 + ":1.0";
    let path;
    let value;

    if(name_part2 == null){return null;}

    path = "/";
    value = {
        thingId: namespace + ":" + full_name, 
        policyId: policy, 
        definition: def,
        attributes:{
            invoiceRecordId:jsonData.invoiceRecordId,
            payType:jsonData.payType,
            transFee:jsonData.transFee,
            transType:jsonData.transType,

            terminalTransNo:jsonData.terminalTransNo,
            terminalNo:jsonData.terminalNo,
            serviceType:jsonData.serviceType,
            algorithmIdentifier:jsonData.algorithmIdentifier,

            tollDistance:jsonData.tollDistance,
            realDistance:jsonData.realDistance,
            freeMode:jsonData.freeMode,
            freeInfo:jsonData.freeInfo,

            balanceBefore:jsonData.balanceBefore,
            balanceAfter:jsonData.balanceAfter,
            fee:jsonData.fee,
            discountFee:jsonData.discountFee,

            payFee:jsonData.payFee,
            feeMileag:jsonData.feeMileag,
            collectFee:jsonData.collectFee,
            rebateMoney:jsonData.rebateMoney,

            cardCostFee:jsonData.cardCostFee,
            unpayFee:jsonData.unpayFee,
            unpayFlag:jsonData.unpayFlag,
            unpayCardCost:jsonData.unpayCardCost,

            ticketFee:jsonData.ticketFee,
            unifiedFee:jsonData.unifiedFee,
            enTollMoney:jsonData.enTollMoney,
            enFreeMoney:jsonData.enFreeMoney,

            enLastMoney:jsonData.enLastMoney,
            returnMoneySn:jsonData.returnMoneySn,
            payCardType:jsonData.payCardType,
            payCardNet:jsonData.payCardNet,

            payCardId:jsonData.payCardId,
            payCardTranSn:jsonData.payCardTranSn,
            payOrderNum:jsonData.payOrderNum,
            payCode:jsonData.payCode,

            payRebate:jsonData.payRebate,
            identification:jsonData.identification,
            paraVer:jsonData.paraVer,
            provinceGroup:jsonData.provinceGroup,
    
            gantryIdGroup:jsonData.gantryIdGroup,
            tollIntervalsCount:jsonData.tollIntervalsCount,
            tollIntervalsGroup:jsonData.tollIntervalsGroup,
            transTimeGroup:jsonData.transTimeGroup,

            chargefeeGroup:jsonData.chargefeeGroup,
            chargeDiscountGroup:jsonData.chargeDiscountGroup,
            rateModeVersionGroup:jsonData.rateModeVersionGroup,
            rateParaVersionGroup:jsonData.rateParaVersionGroup,

            tollfeeGroup:jsonData.tollfeeGroup,
            verifyCode:jsonData.verifyCode,
            sectionGroup:jsonData.sectionGroup,
            roadType:jsonData.roadType,

            shortFee:jsonData.shortFee,
            shortFeeMileage:jsonData.shortFeeMileage,
            feeRate:jsonData.feeRate,
            actualFeeClass:jsonData.actualFeeClass,

            chargeMode:jsonData.chargeMode,
            feeBoardPlay:jsonData.feeBoardPlay,
            spcRateVersion:jsonData.spcRateVersion,
            checkSign:jsonData.checkSign,

            verifyFlag:jsonData.verifyFlag,
            verifyPassTime:jsonData.verifyPassTime,
            remarks:jsonData.remarks,
            discountType:jsonData.discountType,

            provinceDiscountFee:jsonData.provinceDiscountFee,
            originFee:jsonData.originFee,
            secFreeType:jsonData.secFreeType,
            secProResult:jsonData.secProResult,

            secProTime:jsonData.secProTime,
            secBefFee:jsonData.secBefFee,
            secFreeFee:jsonData.secFreeFee,
            ticketfeeinfo:jsonData.ticketfeeinfo
        },
        features:{
        }
    };
    return Ditto.buildDittoProtocolMsg(namespace, full_name, "things", "twin", "commands", "modify", path, headers, value);
}