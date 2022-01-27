
### 先打包，再发送给远程flink集群

首先完成webflux接受程序的运行、以及flink-deploy文件夹里的文档部署

1. 项目打包

    `mvn package`
2. 项目上传到flink

    `../flink-1.13.1/bin/flink run -m 127.0.0.1:8081 -d target/analyse-0.1.jar --bootstrap.servers kafka:9092 --input-topic zcinput --checkpointing --event-time`

    -m指定flink集群的地址，也就是docker compose里面jobmanager ports暴露出来的8081端口 ，这个参数无需更改，只要在同一台机器上就行。--bootstrap.servers指定kafka服务，这个无需更改

3. web ui查看任务

    访问机器的8081端口即可，一般是localhost



### 修改读取的csv文件目录

在`src/main/java/com/nju/ics/Datastream/DataFlowBuilder.java`里面修改三个流水的csv文件

### 同时在途与有入无出（超时）的稽查
跟上面部署app命令类似，但是要指定main class，因为要用到排序好的数据文件,排序文件的数据结构不一样，使用的datastream代码不一样
1. 首先修改下pom.xml里面的main class，将246行的mainClass改为`com.nju.ics.Funcs.GantryTimerRemoteTest`
同时也要修改排序好的csv的文件路径，在`src/main/java/com/nju/ics/Funcs/GantryTimerRemoteTest.java`里面更改相应路径
2. 再执行上面的项目打包与项目上传操作：

    `mvn package`

    `../flink-1.13.1/bin/flink run -m 127.0.0.1:8081 -d target/analyse-0.1.jar  --bootstrap.servers kafka:9092 --input-topic zcinput --checkpointing --event-time`

