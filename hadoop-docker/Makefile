down:
	docker-compose down

master:
	docker-compose up --remove-orphans -d --build spark-master spark-worker-0 hadoop-datanode-0 hadoop-namenode hadoop-nodemanager-0

worker_1:
	docker-compose up --remove-orphans -d --build spark-worker-1 hadoop-datanode-1 hadoop-nodemanager-1 hadoop-resourcemanager

worker_2:
	docker-compose up --remove-orphans -d --build spark-worker-2 hadoop-datanode-2 hadoop-nodemanager-2 hadoop-historyserver

swarm-deploy:
	docker stack deploy --compose-file docker-compose.yml alex
	docker stack services alex
