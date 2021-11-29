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



    name_part1 = "vehicle";
    name_part2 = jsonData._id;
    full_name = name_part1 + "-" + name_part2;
    def = "ics.rodaki:" + name_part1 + ":1.0";


    path = "/";
    value = {
        thingId: namespace + ":" + full_name, 
        policyId: policy, 
        definition: def,
        attributes:{
            车牌号和颜色: jsonData._id,
            车辆座位数: jsonData.VEHICLESEAT,
            车辆长: jsonData.VEHICLELENGTH,
            车辆宽: jsonData.VEHICLEWIDTH,
            车辆高: jsonData.VEHICLEHIGHT,
            车轴数: jsonData.AXLECOUNT,
            车型 : jsonData.VEHICLETYPE,
            轴组信息: jsonData.AXISINFO,
            限载重量: jsonData.LIMITWEIGHT,
        },

        features:{
            是否在高速上:{
                properties:{
                    value:jsonData.ISINHIGHWAY
                }
            },
            通行介质类型:{
                properties:{
                    value:jsonData.MEDIATYPE
                }
            },
            通行介质编号:{
                properties:{
                    value:jsonData.MEDIAID
                }
            },
            通过站点信息:{
                properties:{
                    value:jsonData.PASSSTATION
                }
            },
            最近更新时间:{
                properties:{
                    value:jsonData.TIME
                }
            },
            当前通行交易编号:{
                properties:{
                    value:jsonData.PASSID
                }
            },
            最近一段时间车速:{
                properties:{
                    value:jsonData.CURRENTSPEED
                }
            },
            此次通行平均车速:{
                properties:{
                    value:jsonData.CURRENTAVGSPEED
                }
            },
            此次通行总费用:{
                properties:{
                    value:jsonData.TOTALFEE
                }
            },
            此次通行总里程:{
                properties:{
                    value:jsonData.TOTALFEEMILEAGE
                }
            },
            此次通行总时间:{
                properties:{
                    value:jsonData.TOTALPASSTIME
                }
            },
            车辆当前通行速度列表:{
                properties:{
                    value:jsonData.CURRENTSPEEDLIST
                }
            },
            车辆历史通行速度列表:{
                properties:{
                    value:jsonData.CURRENTSPEEDHISTORY
                }
            }
        }
    };    


return Ditto.buildDittoProtocolMsg(namespace, full_name, "things", "twin", "commands", "modify", path, headers, value);
}
