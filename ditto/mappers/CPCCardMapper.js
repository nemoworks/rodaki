function mapToDittoProtocolMsg(headers,textPayload,bytePayload,contentType){
    const jsonData = JSON.parse(textPayload);

    const namespace = "ics.rodaki";

    const name_part1 = "cpccard";
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
    return Ditto.buildDittoProtocolMsg(namespace, full_name, "things", "twin", "commands", "modify", path, headers, value);
}