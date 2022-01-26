
## maven 打jar包 
`mvn clean package`


## build 镜像

### 消费者,消费数据并写入数据库
`docker build -f ConsumerDockerfile -t consumer:v1 .`

### 车流统计任务，定时统计门架车流量
`docker build -f ServiceDockerfile -t service:v1 .`

### 稽查"大车小标"任务，发送出口流水指定字段到队列

* *判断逻辑
    - 判断 TRANSPAYTYPE, 如果不是 1，则为人工通道，将该记录中的车型作为该 VEHICLEID 的真实车型，存入数据库
    - 判断 TRANSPAYTYPE, 如果是 1， 则认为不是人工通道，对比当前车型与数据库中真实车型，若当前车型小于真实车型，则发送消息到 rabbitmq 队列

`docker build -f VehicleTypeCheckerDockerfile -t vehicletypechecker:v1 .`


发送稽查任务数据到rabbitmq队列中

`python SendData2Rabbitmq.py`

其中 exitFilePath 指定出口流水路径
`exitFilePath = "/hdd/data/1101/exitwaste.csv"`


## 在/rodaki/deploy 目录下启动任务

`docker-compose up -d`