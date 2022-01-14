# 门架车流量预测
该部分代码用来对高速路网上的门架做车流量预测
依赖elasticsearch,目前暂停使用
## 处理流程

1. 高速路网上的门架设备、站点设备将数据传送至webflux接收程序
2. webflux接收程序会将数据存储至kafka消息队列
3. flink程序会从kafka系统读入消息，并做实时流处理，若记录属于门架上传的数据，则会发送至elasticsearch数据库
4. elasticsearch数据库负责在接收到门架收费计费数据后，对每一个门架Hex值做聚合计算，得到每个hex节点的每小时车流量
> 为什么是Hex值？
>
>因为可能存在两个门架紧挨在一处，他们的门架id不同，但hex编码相同，因此会按照hex编码为粒度进行计算

5. 门架车流量预测部分会定期训练、校验模型；同时也会每天0点预测出当天的各个hex节点的24小时车流量（粒度为小时）


## 环境配置

1.  python3.7 环境安装，建议使用Minicoda3
    1.  下载   `wget https://mirrors.tuna.tsinghua.edu.cn/anaconda/miniconda/Miniconda3-py37_4.9.2-Linux-x86_64.sh`
    2.  添加执行权限
    3.  执行下载的脚本完成安装，安装过程选择默认设置即可
2.  
## 代码执行

进入conda环境执行下列代码:

`pip install -r requirements.txt`安装依赖

`python myscheduler.py `启动程序,会向elasticsearch注册index、聚合计算等服务，也会向kibana注册地图服务

## 配置修改

在`myflask/config/config.yaml`下可以设置kibana、elasticsearch的host、port，具体的说明在文件里有注释，一般情况下不用更改
