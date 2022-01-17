

排序数据：

1. 写入流水数据到mongodb
2. 在mongodb中排序数据
3. 导出mongodb中的数据





1. 写入流水数据到mongodb

	- 启动 mongodb 服务(可以是 rodaki 中的 mongodb)

	- 安装 python package
		pip install pymongo

	- 运行 python 脚本发送数据到mongodb, 注意脚本中以下参数配置, Tutorial: https://pymongo.readthedocs.io/en/stable/tutorial.html

		- connect to mongodb
  
			client = MongoClient('mongodb://localhost:27017/')

		- db in mongodb
  
			db = client['sortdata']

		- collection in mongodb
			collection = db["1101_original"]


		- paths of three files
  
			entryFilePath = "./enwaste.csv"

			gntryFilePath = "./gantrywaste_fix.csv"

			exitFilePath = "./exitwaste.csv"


2. 在mongodb中排序数据

	- 在 deploy 目录中执行 "docker-compose exec mymongodb /bin/bash" 进入 mymongodb 容器

	- 执行 "mongo" 进入 mongo shell

	- 在mongo shell 中执行以下聚合命令，按照 "TIME" 字段对数据进行排序，并输出到 "1101_sort" collectio 中

		db.getCollection("1101_original").aggregate([{$sort:{"TIME":1}},{$merge:{into:"1101_sort"}}],{allowDiskUse:true})



3. 导出mongodb中的数据

	- 执行 "exit" 退出 mongo shell

	- 在 mymongodb 容器内使用 mongoexport 导出 1101_sort collection 中的数据为制定的 csv 格式

		mongoexport -d sortdata -c 1101_sort --csv -f FLOWTYPE,TIME,STATIONID,VLP,VLPC,VEHICLETYPE,PASSID,TIMESTRING,ORIGINALFLAG,PROVINCEBOUND -o 1101_sort.csv

	- 执行 "exit" 退出容器

	- 执行 "docker cp mymongodb:/1101_sort.csv ." 从容器中copy出排序好的 csv 文件到当前目录





