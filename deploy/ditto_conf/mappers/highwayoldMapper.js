function mapToDittoProtocolMsg(headers,textPayload,bytePayload,contentType){
    const jsonData = JSON.parse(textPayload);

    const namespace = "ics.rodaki";
    const policy = "ics.rodaki:base";
    let path;
    let value;
    let name_part1;
    let name_part2;
    let full_name;
    let def;


    if(jsonData._model == "CPCCard"){
        name_part1 = "cpccard";
        name_part2 = jsonData.id;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                issueFlag:jsonData.issueFlag,
                cardVersion:jsonData.cardVersion,
                mediaType:jsonData.mediaType,
                vehicleId:jsonData.vehicleId
            },
            features:{
                batteryPercentage:{
                    properties:{
                        value:jsonData.batteryPercentage
                    }
                }
            }
        };    

    }

    if(jsonData._model == "ETCCard"){
        name_part1 = "etccard";
        name_part2 = jsonData.id;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                type:jsonData.type,
                cardVersion:jsonData.cardVersion,
                netID:jsonData.netID,
                startDate:jsonData.startDate,
                endDate:jsonData.endDate,
                Vlp:jsonData.Vlp,
                Vlpc:jsonData.Vlpc,
                VehicleType:jsonData.VehicleType,
                vehicleUserType:jsonData.vehicleUserType
            },
            features:{
            }
        };
    }

    if(jsonData._model == "Gantry"){
        name_part1 = "gantry";
        name_part2 = jsonData.id;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                computerOrder:jsonData.computerOrder,
                hex:jsonData.hex,
                hexOpposite:jsonData.hexOpposite,
                longtitude:jsonData.longtitude,
                latitude:jsonData.latitude
            },
            features:{
            }
        };
    }

    if(jsonData._model == "GantryRecord"){
        name_part1 = "gantryrecord";
        name_part2 = jsonData.tradeId;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                gantryId:jsonData.gantryId,
                transTime:jsonData.transTime,
                mediaId:jsonData.mediaId
            },
            features:{
            }
        };
    }

    if(jsonData._model == "InvoiceRecord"){
        name_part1 = "invoicerecord";
        name_part2 = jsonData.id;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                type:jsonData.type,
                code:jsonData.code,
                cnt:jsonData.cnt
            },
            features:{
            }
        };
    }

    if(jsonData._model == "Lane"){
        name_part1 = "lane";
        name_part2 = jsonData.laneNumberId;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                laneApp:jsonData.laneApp,
                laneType:jsonData.laneType,
                laneNumber:jsonData.laneNumber,
                laneHex:jsonData.laneHex,
                tollStationId:jsonData.tollStationId
            },
            features:{
            }
        };
    }

    if(jsonData._model == "OBUCard"){
        name_part1 = "obucard";
        name_part2 = jsonData.id;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                obuIssueFlag:jsonData.obuIssueFlag,
                obuSign:jsonData.obuSign,
                supplierId:jsonData.supplierId,
                obuVersion:jsonData.obuVersion,
                ETCcardId:jsonData.ETCcardId,
                vehicleId:jsonData.vehicleId,
                mediaType:jsonData.mediaType
            },
            features:{
                batteryPercentage:{
                    properties:{
                        value:jsonData.batteryPercentage
                    }
                }
            }
        };
    }

    if(jsonData._model == "Operator"){
        name_part1 = "operator";
        name_part2 = jsonData.operId;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                operName:jsonData.operName
            },
            features:{
                batchNum:{
                    properties:{
                        value:jsonData.batchNum
                    }
                }
            }
        };
    }

    if(jsonData._model == "PaymentRecord"){
        name_part1 = "paymentrecord";
        name_part2 = jsonData.id;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
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
    }

    if(jsonData._model == "Plate"){
        name_part1 = "plate";
        name_part2 = jsonData.number + "-" + jsonData.color;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(jsonData.number == null && jsonData.color == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
            },
            features:{
            }
        };
    }

    if(jsonData._model == "Shift"){
        name_part1 = "shift";
        name_part2 = jsonData.batchNum;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                tollCollectorId:jsonData.tollCollectorId,
                monitorId:jsonData.monitorId,
                loginTime:jsonData.loginTime,
                monitorTime:jsonData.monitorTime,
                lDate:jsonData.lDate
            },
            features:{
            }
        };
    }

    if(jsonData._model == "StationRecord"){
        name_part1 = "stationrecord";
        name_part2 = jsonData.id;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                transCode:jsonData.transCode,
                shiftId:jsonData.shiftId,
                vehicleId:jsonData.vehicleId,
                laneId:jsonData.laneId
            },
            features:{
            }
        };
    }

    if(jsonData._model == "TollStation"){
        name_part1 = "tollstation";
        name_part2 = jsonData.numberId;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                number:jsonData.number,
                hex:jsonData.hex,
                longtitude:jsonData.longtitude,
                latitude:jsonData.latitude
            },
            features:{
            }
        };
    }

    if(jsonData._model == "TrafficRecord"){
        name_part1 = "trafficrecord";
        name_part2 = jsonData.id;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                mediaId:jsonData.mediaId,
                gantryRecordIds:jsonData.gantryRecordIds,
                entryStationId:jsonData.entryStationId,
                exitStationId:jsonData.exitStationId
            },
            features:{
            }
        };
    }

    if(jsonData._model == "TrafficTransaction"){
        name_part1 = "traffictransaction";
        name_part2 = jsonData.id;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                description:jsonData.description,
                trafficRecordId:jsonData.trafficRecordId,
                paymentRecordId:jsonData.paymentRecordId,
                vehicleId:jsonData.vehicleId
            },
            features:{
            }
        };
    }

    if(jsonData._model == "Vehicle"){
        name_part1 = "vehicle";
        name_part2 = jsonData.plateId;
        full_name = name_part1 + "-" + name_part2;
        def = "ics.rodaki:" + name_part1 + ":1.0";
        if(name_part2 == null){return null;}
        path = "/";
        value = {
            thingId: namespace + ":" + full_name, 
            policyId: policy, 
            definition: def,
            attributes:{
                vehicleClass:jsonData.vehicleClass,
                vehicleType:jsonData.vehicleType,
                vehicleSeat:jsonData.vehicleSeat,
                vehicleLength:jsonData.vehicleLength,
                vehicleWidth:jsonData.vehicleWidth,
                vehicleHight:jsonData.vehicleHight,
                trafficTransactionid:jsonData.trafficTransactionid,
                axisInfo:jsonData.axisInfo,
                limitWeight:jsonData.limitWeight
            },
            features:{
                weight:{
                    properties:{
                        value:jsonData.weight
                    }
                },
                speed:{
                    properties:{
                        value:jsonData.speed
                    }
                },
                lastPassStation:{
                    properties:{
                        value:jsonData.lastPassStation
                    }
                },
                location:{
                    properties:{
                        value:jsonData.location
                    }
                }
            }
        };
    }
    return Ditto.buildDittoProtocolMsg(namespace, full_name, "things", "twin", "commands", "modify", path, headers, value);
}