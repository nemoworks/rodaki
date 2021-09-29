# 部署架构

![部署架构](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/nemoworks/rodaki/master/docs/uml/deployment.pu)

## 系统说明

一个真实的场景是：高速路网上的门架、收费站等相关节点，会源源不断地将数据上传至数据中心，
即后台。因此，后台需要有相对应的接收程序，然后接收程序会将数据按类别存储至不同的数据库，
如果需要对数据进行进一步的分析，还可以使用flink、elasticsearch等程序进行数据分析，得
到想要的目标数据。

## 组件说明
这一部分将按照逻辑顺序介绍
### source && jmeter
首先就是数据源。

数据源可以是真实场景下那些站点上传的数据，对应**source**部分；也可以是模拟上传的数据，对应**jmeter**部分

这些数据会发送至后台接收程序，即**webflux**部分

### webflux
webflux负责接收数据，并将数据存储至数据库，这里只需要发送至kafka消息队列系统

### kafka

kafka消息队列系统会使用一个队列，将webflux传来的数据按顺序写入队列，供消费者消费，即flink

### flink & DomainModel
flink流处理系统主要负责按照DomainModel的数据结构，从kafka获得的数据里抽取出DomainModel，将实体的状态信息
写入couchdb数据库、iotdb数据库；同时会将门架数据传入elasticsearch数据库中

### couchdb
couchdb 接收并存储来至flink处理的实体状态信息；与此同时，依据用户提供的额外的算子，计算实体的额外属性的状态信息；当实体的状态信息（直接接收到/通过算子得到）发生变更时，构建消息写入rabbitmq队列通知ditto
### rabbitmq &&  iotdb && elasticsearch

这三个数据库有各自的职能：
1. couchdb 负责存放实体的历史状态信息，并基于他来实现实体中依赖于其他属性（历史/状态变化）的状态的计算和更新
2. rabbitmq 负责放入实体的状态消息，供ditto消费
3. iotdb 负责存放实体在历史上的状态信息，因为ditto只存储实体当前的状态
4. elasticsearch 负责利用门架记录来进行车流量的聚合计算，提供真实车流量与预测车流量的数据存储

### Ditto

ditto负责存储每个实体的当前状态，提供相应的查询api

### trafficFlowPredicton

trafficFlowPredicton负责定期从elasticsearch里获取门架路网的真实车流量，训练模型、预测数据
