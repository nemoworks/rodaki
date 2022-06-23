# 方法一



1. 使用sendDataToMongodb.py提取原始入口、门架、出口流水数据中指定字段，插入mongodb中的1101_original中（147.07 sec + 1522.35 sec+ 184.47 sec）

   ```shell
   python sendDataToMongodb.py
   ```

2. 对1101_original中的数据按照TIME字段和FLOWTYPE字段升序排序，结果转存在1101_sort中（973 sec）

   ```shell
   # 在rodaki/deploy目录下，进入mongodb容器
   docker-compose exec mymongodb /bin/bash
   
   # 进入mongo shell
   mongo
   
   # 使用sortdata数据哭
   use sortdata
   
   # 进行排序并转存排序后的数据
   db.getCollection('1101_original').aggregate([{$sort:{"TIME":1, "FLOWTYPE":1}},{$merge:{into:"1101_sort"}}],{allowDiskUse:true})
   
   
   
   ```

   

3. 从数据库中导出1101_sort为csv文件

   ```shell
   # 退出mongo shell环境
   exit
   
   # 在容器中执行mongoexport导出数据
   mongoexport -d sortdata -c 1101_sort --type csv --fields FLOWTYPE,LANESPINFO,MEDIATYPE,ORIGINALFLAG,PASSID,PROVINCEBOUND,SPECIALTYPE,STATIONID,TIME,TIMESTRING,TRANSCODE,VEHICLETYPE,VLP,VLPC,ACTUALFEECLASS -o 1101_sort.csv
   
   # 退出mongodb容器
   exit
   
   # 导出容器中数据到当前路径
   docker cp mymongodb:/1101_sort.csv .
   ```

   

# 方法二(推荐)



1. 运行处理脚本，默认输入为 /hdd/data ，输出路径必须指定（379 sec）

   ```shell
   # 指定输出路径
   bash sort_data.sh -i /hdd/data -o ./sortdata
   ```

2. 指定处理的天数，默认从1101开始

   ```shell
   # 指定处理的天数
   bash sort_data.sh -o ./sortdata -d 2
   ```

   

