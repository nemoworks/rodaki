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



## 创建 consumer 镜像, 从rabbitmq消费数据、存储数据到 mongodb

进入 consumer-app 目录
`cd consumer-app`

build 镜像
`docker build -t consumer:v1 .`


## 创建docker网络

`docker network create zc_net`



## 启动容器
`docker-compose up -d`






## mongodb 初始化副本集配置


进入容器
`docker-compose exec mymongodb /bin/bash`

命令行执行
`mongo`

初始化副本集
`rs.initiate({"_id": "testSet", "members": [{"_id":0, "host":  "mymongodb:27017"}]})`

