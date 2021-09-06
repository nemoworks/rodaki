#!/bin/bash


for thing in "cpccard" "etccard" "gantry" "gantryrecord" "invoicerecord" "lane" "obucard" "operator" "paymentrecord" "plate" "shift" "stationrecord" "tollstation" "trafficrecord" "traffictransaction" "vehicle"
do

echo -n $thing:
# 根据 definition 查找并统计各类实体的数量
curl -u ditto:ditto -X GET 'http://localhost:8080/api/2/search/things/count?filter=eq(definition,"ics.rodaki:'$thing':1.0")'
echo
done
