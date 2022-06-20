# 目录说明
目前，项目工程的结构如下所示：
```bash
./src/main/java/com/nju/ics/
├── abnormalsituation   //各种异常情况监测的datastream
├── annotation          //注解
├── configs             //静态数据文件解析，例如站点数据、门架数据
├── connectors          //各种外部系统的sink算子
├── dbs                 //一些数据库的连接参数
├── fastjsonutils       //fastjson的序列化反序列化
├── rawtype             //一些不重要的数据类型              
├── funcs               //各种算子以及算子使用的process处理函数
├── mappers             //mapstruct工具的mapper
├── modelextractors     //model的提取器
├── models              //建模的model         
├── snapshots           //snapshot模型
├── streamingjob.java   //remote模式的任务
├── streamingjoblocal.java  //local模式的任务
├── streamjobslocal          // 一些本地执行下的任务，包括使用CEP监测异常、model提取等各种任务
├── streamjobsremote          // 一些远程提交的任务
├── utils               //各种工具类
└── watermark           //生成watermark的操作
```
# 运行
## 本地测试运行
在本地IDE下，直接运行StreamJobsLocal文件夹下的各个任务即可

### 1. 修改读取的文件
当前StreamJobsLocal文件夹下的很多任务是直接将csv文件作为data source读取的，只要在文件的开头修改csv文件路径即可，如果csv的列有改动，还需要修改解析规则
### 2. 运行main函数
### 3. web ui查看任务
    绝大数的local任务访问机器的9000端口即可，少数是9001，具体的可以查看文件里的编码
## 远程集群运行
这个一般是用于正式环境，StreamJobsRemote目录下才是可以远程提交的任务。
### 1. 修改数据源
1. 如果任务是以csv文件作为数据源，则需要在相应的文件里修改（目前没有使用命令行传参的方式，且以csv文件作为数据源的代码只有local模式，下面的项目提交部分只有kafka数据源的任务）
2. 如果是以kafka作为数据源，则直接在下面的项目上传集群这一步骤设置即可
### 2. 指定main class
修改下pom.xml里面的main class，将246行的mainClass改为想要执行的任务的main class，例如`com.nju.ics.Funcs.GantryTimerRemoteTest`
### 3. 项目打包
`mvn package`
### 4. 项目提交
    
    `../flink-1.13.1/bin/flink run -m 127.0.0.1:8081 -d target/analyse-0.1.jar --bootstrap.servers kafka:9092 --input-topic zcinput --checkpointing --event-time`

    -m指定flink集群的地址，也就是docker compose里面jobmanager ports暴露出来的8081端口 ，这个参数无需更改，只要在同一台机器上就行。--bootstrap.servers指定kafka服务，这个无需更改
### web ui查看任务
    访问集群的jobmanager的端口