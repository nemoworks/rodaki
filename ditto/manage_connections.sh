#!/bin/bash

option=$1
echo $option
if [ $option == "--open" ] || [ $option == "--close" ] || [ $option == "--delete" ] || [ $option == "--retrieve" ] 
then
  for connect in "CPCCard" "ETCCard" "Gantry" "GantryRecord" "InvoiceRecord" "Lane" "OBUCard" "Operator" "PaymentRecord" "Plate" "Shift" "StationRecord" "TollStation" "TrafficRecord" "TrafficTransaction" "Vehicle" 
  do
  curl -X POST -i -u devops:foobar -H 'Content-Type: application/vnd.eclipse.ditto+json' -d '{
    "targetActorSelection": "/system/sharding/connection",
    "headers": {
      "aggregate": false
    },
    "piggybackCommand": {
      "type": "connectivity.commands:'${option:2}'Connection",
      "connectionId": "'$connect'-mqtt-connection"
    }
  }' 'http://localhost:8080/devops/piggyback/connectivity'
  done
elif [ $option == "--create" ]
then

  for queue in "CPCCard" "ETCCard" "Gantry" "GantryRecord" "InvoiceRecord" "Lane" "OBUCard" "Operator" "PaymentRecord" "Plate" "Shift" "StationRecord" "TollStation" "TrafficRecord" "TrafficTransaction" "Vehicle"
  do

  connectionType="mqtt"
  file="mappers/"$queue"Mapper.js"
  id="$queue-$connectionType-connection"
  uri="tcp://admin:admin@rabbitmq:1883"

  mapper=$(echo $(cat $file))
  mapper=${mapper//\"/\\\"}

  curl -X POST -i -u devops:foobar -H 'Content-Type: application/vnd.eclipse.ditto+json' -d "{
      \"targetActorSelection\": \"/system/sharding/connection\",
      \"headers\": {
          \"aggregate\": false
      },
      \"piggybackCommand\": {
          \"type\": \"connectivity.commands:createConnection\",
          \"connection\": {
              \"id\": \"$id\",
              \"connectionType\": \"$connectionType\",
              \"connectionStatus\": \"closed\",
              \"uri\":\"$uri\",
              \"failoverEnabled\": true,
              \"sources\": [{
                  \"addresses\": [
                      \"$queue\"],
                  \"authorizationContext\": [\"nginx:ditto\"],
                  \"qos\":1
              }],
              \"mappingContext\": {
                  \"mappingEngine\": \"JavaScript\",
                  \"options\": {\"incomingScript\": \"$mapper\"}
              }
          }
      }
  }" http://localhost:8080/devops/piggyback/connectivity

  done


fi
