function mapToDittoProtocolMsg(headers,textPayload,bytePayload,contentType){
    const jsonData = JSON.parse(textPayload);
    const namespace = "ics.rodaki";
    const full_name = "test";
    const policy = "ics.rodaki:base";
    const def = "ics.rodaki:test:1.0";
    let path;
    let value;

    if(jsonData.id == null){return null;}

    path = "/";
    value = {
        thingId: namespace + ":" + full_name, 
        policyId: policy, 
        definition: def,
        attributes:{
            a1:jsonData.a1,
            a2:jsonData.a2,
            a3:jsonData.a3,
            a4:jsonData.a4
        },
        features:{
            f1:{
                properties:{
                    value:jsonData.f1
                }
            },
            f2:{
                properties:{
                    value:jsonData.f2
                }
            },
            f3:{
                properties:{
                    value:jsonData.f3
                }
            },
            f4:{
                properties:{
                    value:jsonData.f4
                }
            }
        }
    };
    return Ditto.buildDittoProtocolMsg(namespace, full_name, "things", "twin", "commands", "modify", path, headers, value);
}