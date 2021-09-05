## 环境准备
机器安装docker、docker-compose
## 运行说明

## kafka分布时消息队列系统与flink部署

1. 创建flink的savepoint与checkpoint目录
    ```
    mkdir ./flink-checkpoint
    mkdir ./flink-savepoint
    ```
    
    记得更改这两个文件夹的权限，不然无法写入文件

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
    ` docker-compose exec kafka kafka-console-producer.sh --broker-list localhost:9092 --topic zcinput`
    ` docker-compose exec kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic zcinput`
## 
