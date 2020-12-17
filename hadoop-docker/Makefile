down:
	docker-compose down

master:
	docker-compose up --remove-orphans -d --build spark-master spark-worker-0 hadoop-namenode hadoop-datanode-0 hadoop-resourcemanager hadoop-historyserver hadoop-nodemanager

worker_1:
	docker-compose up --remove-orphans -d --build spark-worker-1 hadoop-datanode-1

worker_2:
	docker-compose up --remove-orphans -d --build spark-worker-2 hadoop-datanode-2
