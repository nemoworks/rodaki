# Ditto 模块部署
------------------------------

1. [ditto的安装与运行](#jump1)
2. [ditto与rabbitmq连接的建立](#jump2)
3. [things 的统计与删除](#jump3)


### <span id="jump1"> Ditto的安装与运行 </span>  


通过 Docker Compose 运行 ditto，需要:  

* Docker daemon 正在运行  
* Docker Compose 已经安装


在 Ditto/ 目录下获取 ditto  

	git clone https://github.com/eclipse/ditto.git
	
	
修改配置文件 docker-compose.yml

	cp docker-compose.yml ditto/deployment/docker/


运行 ditto  

	cd ditto/deployment/docker/
	docker-compose up -d


检查 logs 或访问 [http://localhost:8080](http://localhost:8080) (或 docker-compose.yml 中手动配置的端口)验证 ditto 已启动  

	docker-compose logs -f

查看组建运行状态/使用资源情况/重启服务

	docker-compose ps
	docker stats
	docker restart mongodb/policies/things/things-search/concierge/connectivity

停止 ditto  

	docker-compose stop


### <span id="jump2"> Ditto与RabbitMQ连接的建立 </span>


ditto 服务启动后，回到 Ditto/ 目录，使用 manage_connection.sh 脚本创建所有连接

	./manage_connection.sh --create
	
使用 -h 查看可对连接进行的管理操作，使用 open/close/delete/retrieve 选项执行 打开/关闭/删除/获取信息 操作，连接创建好后默认状态为 closed，使用 --open 选项打开所有连接进行消费

	./manage_connection.sh -h
	./manage_connection.sh --open


### <span id="jump3"> Things 的统计与删除 </span>

统计 ditto 中各类 thing 的数量

	./count_all_things.sh
	
	# cpccard:64
	# etccard:400
	# gantry:1918
	# gantryrecord:174446
	# invoicerecord:29268
	# lane:3088
	# obucard:3868
	# operator:2086
	# paymentrecord:4919
	# plate:146700
	# shift:3341
	# stationrecord:148262
	# tollstation:440
	# trafficrecord:488
	# traffictransaction:5794
	# vehicle:146774

删除 ditto 中所有 thing

	./delete_all_things.sh