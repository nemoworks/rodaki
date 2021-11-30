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



    name_part1 = "gantry";
    name_part2 = jsonData._id;
    full_name = name_part1 + "-" + name_part2;
    def = "ics.rodaki:" + name_part1 + ":1.0";


    path = "/";
    value = {
        thingId: namespace + ":" + full_name, 
        policyId: policy, 
        definition: def,
        attributes:{
            门架编号: jsonData._id,
            门架经度: jsonData.LONGITUDE,
            门架纬度: jsonData.LATITUDE,
            门架名称: jsonData.GANTRYNAME,
            门架类型: jsonData.GANTRYTYPE
        },

        features:{
            车流量更新时间:{
                properties:{
                    value:jsonData.TIME
                }
            },
            最近5分钟车流量:{
                properties:{
                    value:jsonData.TRAFFICFLOW
                }
            },
            门架历史车流量:{
                properties:{
                    value:jsonData.TRAFFICFLOWHISTORY
                }
            }
        }
    };    


return Ditto.buildDittoProtocolMsg(namespace, full_name, "things", "twin", "commands", "modify", path, headers, value);
}
