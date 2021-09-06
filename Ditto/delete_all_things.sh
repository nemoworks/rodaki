#!/bin/bash

. urlencode.sh



namespaces="ics.rodaki"
while true
do
    things_count=$(curl -u ditto:ditto -X GET 'http://localhost:8080/api/2/search/things/count?namespaces='$namespaces'')
    if [ $things_count -eq 0 ]
    then
    break
    fi

    response=$(echo $(curl -u ditto:ditto -X GET 'http://localhost:8080/api/2/search/things?namespaces='$namespaces'&option=size(200)') | jq .items | jq .[].thingId | sed 's/\"//g')
    echo $response

    for thingId in $response
    do
    encode_thingId=$(urlencode $thingId)
    curl -u ditto:ditto -X DELETE http://localhost:8080/api/2/things/$encode_thingId
    done
done

