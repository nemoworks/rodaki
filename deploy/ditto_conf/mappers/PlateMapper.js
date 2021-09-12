function mapToDittoProtocolMsg(headers,textPayload,bytePayload,contentType){
    const jsonData = JSON.parse(textPayload);

    const namespace = "ics.rodaki";

    const name_part1 = "plate";
    const name_part2 = jsonData.number + "-" + jsonData.color;

    const full_name = name_part1 + "-" + name_part2;
    const policy = "ics.rodaki:base";
    const def = "ics.rodaki:" + name_part1 + ":1.0";
    let path;
    let value;

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
    return Ditto.buildDittoProtocolMsg(namespace, full_name, "things", "twin", "commands", "modify", path, headers, value);
}