#!/bin/bash


# 用于 url 中编码转换，主要为了处理汉字
. urlencode.sh


port="8080"

namespaces="ics.rodaki"

while true
do
    # 先统计是否还存在 "ics.rodaki" 项目的实体
    things_count=$(curl -u ditto:ditto -X GET 'http://localhost:'$port'/api/2/search/things/count?namespaces='$namespaces'')
    if [ $things_count -eq 0 ]
    then
    break
    fi

    # 一次 get 200 个事物进行删除
    response=$(echo $(curl -u ditto:ditto -X GET 'http://localhost:'$port'/api/2/search/things?namespaces='$namespaces'&option=size(200)') | jq .items | jq .[].thingId | sed 's/\"//g')
    echo $response

    for thingId in $response
    do
    encode_thingId=$(urlencode $thingId)
    curl -u ditto:ditto -X DELETE http://localhost:$port/api/2/things/$encode_thingId
    done
done

