## 环境准备
机器安装docker、docker-compose
<!-- ## 运行说明
整个系统主要分为两块内容 ，一个是kafka分布时消息队列系统与flink部署，一个是elasticsearch、flink部署

kafka分布时消息队列系统与flink部署负责抽取数字孪生对象，同时会把车流量预测需要的数据发送到elasticsearch

elasticsearch、flink部署主要是为了进行车流量预测使用 -->


<!-- ## kafka分布时消息队列系统与flink部署

1. 创建flink的savepoint与checkpoint目录
    ```
    mkdir ./flink-checkpoint
    mkdir ./flink-savepoint
    ```
    
    记得更改这两个文件夹的权限，不然无法写入文件
    `chown 9999:9999 flink-checkpoint flink-savepoint`

    创建kafka的数据存储文件夹
    
2. 更新docker-compose.yaml里面相应的数据卷挂载信息(默认即可)

3. 更改kafka中topic信息（默认即可）

    `"zcinput:2:1, zcoutput:2:1"`表示生成两个topic是，5表示5个partition，1个replicas
4. 创建docker网络

    `docker network create zc_net`

4. 部署起来
   
   在当前目录（flink-deploy） `docker-compose up -d`

5. 命令停止

    在当前目录（flink-deploy）`docker-compose down -v`

6. 测试kafka（也可不用执行）
    `docker-compose exec kafka kafkacat -b kafka:9092 -L`
    `docker-compose exec kafka kafka-console-producer.sh --broker-list localhost:9092 --topic zcinput`
    `docker-compose exec kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic zcinput`
## elasticsearch、flink部署

elasticsearch需要持久化存储数据

`mkdir ./es-data`

然后将这个文件夹的所有者的gid和uid改为1000（1000为elasticsearch在容器中的uid），不然es程序无法写入数据，造成容器启动失败

`sudo chown 1000:1000 es-data/`
 -->
1. 在当前目录创建flink的savepoint、checkpoint、datasource目录
    ```
    mkdir ./flink-checkpoint
    mkdir ./flink-savepoint
    mkdir ./flink-datasource
    ```
    
    记得更改这两个文件夹的权限，不然无法写入文件
    `chown 9999:9999 flink-checkpoint flink-savepoint`

    创建kafka的数据存储文件夹
    
2. 准备数据文件
   1. 把flink应用需要读取的数据文件（按照时间顺序排列好的门架、入口、出口流水数据）放到flink-datasource下面（该目录在flink对应docker中挂载在/datasource下，详见[docker-compose.yaml](docker-compose.yaml)）。
   2. 修改数据文件的权限
        `chmod 644 flink-datasource/*`
   3. 相关的数据文件的路径写成/datasource/xxxx.csv（记得参考flink-app目录下[readme](../flink-app/readme.md) 同时更新flink-app目录下的[com.nju.ics.Datastream.DataFlowBuilder.java](../flink-app/src/main/java/com/nju/ics/Datastream/DataFlowBuilder.java)

## 创建 consumer 镜像, 从rabbitmq消费数据、存储数据到 mongodb, 创建 service 镜像, 实时统计车流量 

进入 /rodaki/consumer-app 目录
`cd ../consumer-app`

maven 打jar包 
`mvn clean package`


build 镜像
`docker build -f ConsumerDockerfile -t consumer:v1 .`
`docker build -f ServiceDockerfile -t service:v1 .`

稽查 "大车小标"
`docker build -f VehicleTypeCheckerDockerfile -t vehicletypechecker:v1 .`

## 创建docker网络

`docker network create zc_net`


## 关于service中的车流量统计任务
service容器command中的后三个参数分别为：开始计算车流量的时间戳(默认为11月1日1时)、统计时间段(默认一小时内)、任务频率(默认1分钟一次)，第一次会统计0时-1时间每个门架的车流量，第二次统计任务在第一次任务开始1分钟后启动，以此往复，当所有门架在该时间段无数据时，暂停10分钟后再次统计该时段
`command: java -Xms10g -Xmx10g -XX:NewRatio=2 -jar service.jar 1635699600000 3600000 60000`

注意：
1. service容器是按照上述参数3规定任务频率的时间来周期性执行的，该时间与flink所采用的的（event-time)并不一致，并非模拟的事件时间。考虑到服务器消费RabbitMQ消息队列中的速度依赖于具体机子的配置，可能存在相应通信记录消息还没有被consumer服务消费，但是service已经统计下一时段的情况，导致统计数据不准确。在生产环境下，由于时间跨度较大，这种情况应该不会发生。但在当前demo版本，可能会出现这种不一致现象。可以考虑一开始先不部署service，等等consumer执行一段时间后，再部署service。
2. 此外，为了方便调试，如果在某个统计的时间段没有任何通行记录，我们会一直等待而不会统计下一个时段。（这个是由于我们目前的数据是按天划分的，方便我们发布第二天的数据）

## 启动容器
`docker-compose up -d`






## mongodb 初始化副本集配置


进入容器
`docker-compose exec mymongodb /bin/bash`

命令行执行
`mongo`

初始化副本集
`rs.initiate({"_id": "testSet", "members": [{"_id":0, "host":  "mymongodb:27017"}]})`

