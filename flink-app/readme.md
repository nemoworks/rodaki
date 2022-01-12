
### 先打包，再发送给远程flink集群

首先完成webflux接受程序的运行、以及flink-deploy文件夹里的文档部署

1. 项目打包

    `mvn package`
2. 项目上传到flink

    `../flink-1.13.1/bin/flink run -m 127.0.0.1:8081 -d target/analyse-0.1.jar --bootstrap.servers kafka:9092 --input-topic zcinput --checkpointing --event-time`

    -m指定flink集群的地址，也就是docker compose里面jobmanager ports暴露出来的8081端口 ，这个参数无需更改，只要在同一台机器上就行。--bootstrap.servers指定kafka服务，这个无需更改

3. web ui查看任务

    访问机器的8081端口即可，一般是localhost

/usr/bin/env /home/lzm/jdks/jdk8/bin/java -Dfile.encoding=UTF-8 -cp ./target/analyse-0.1.jar com.nju.ics.StreamingJobLocal --checkpointing


### 修改读取的csv文件目录

在`src/main/java/com/nju/ics/Datastream/DataFlowBuilder.java`里面修改三个流水的csv文件