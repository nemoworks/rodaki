#!/bin/bash

port="8080"

option=$1


# 执行 打开/关闭/删除/获取 连接操作
if [ $option == "--open" ] || [ $option == "--close" ] || [ $option == "--delete" ] || [ $option == "--retrieve" ] 
then
  for connect in "highway" 
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
  }' 'http://localhost:'$port'/devops/piggyback/connectivity'
  done

# 执行 创建 连接操作
elif [ $option == "--create" ]
then

  for queue in "highway"
  do

  connectionType="mqtt"
  file="ditto_conf/mappers/"$queue"Mapper.js"
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
  }" http://localhost:$port/devops/piggyback/connectivity

  done

# 打印帮助信息
elif [ $option == "-h" ] || [ $option == "--help" ]
then
  echo "所有连接操作:"
  echo "  --create    创建"
  echo "  --delete    删除"
  echo "  --open      打开"
  echo "  --close     关闭"
  echo "  --retrieve  获取信息"
fi
