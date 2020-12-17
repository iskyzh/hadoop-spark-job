deploy:
	scp -r hadoop-docker root@124.70.146.167:~
	scp -o 'ProxyJump root@124.70.146.167' -r hadoop-docker root@192.168.0.200:~
	scp -o 'ProxyJump root@124.70.146.167' -r hadoop-docker root@192.168.0.249:~

up: deploy
	ssh root@124.70.146.167 "cd ~/hadoop-docker && ls -al && make master"
	ssh -J root@124.70.146.167 root@192.168.0.200 "cd ~/hadoop-docker && ls && make worker_1"
	ssh -J root@124.70.146.167 root@192.168.0.249 "cd ~/hadoop-docker && ls && make worker_2"

hadoop-build:
	cd map-reduce-temperature && mvn clean install

hadoop-deploy: hadoop-build
	scp map-reduce-temperature/target/map-reduce-temperature-1.0-SNAPSHOT.jar root@124.70.146.167:~/hadoop-docker/hadoop_namenode


hadoop-run:
	ssh root@124.70.146.167 "cd ~/hadoop-docker && docker-compose exec -T hadoop-namenode hadoop fs -rm -r hdfs://node-master.lan:9000/user/hadoop/hadoop.output || true"
	ssh root@124.70.146.167 "cd ~/hadoop-docker && docker-compose exec -T hadoop-namenode hadoop jar /hadoop/dfs/name/map-reduce-temperature-1.0-SNAPSHOT.jar Temperature hdfs://node-master.lan:9000/user/hadoop/input hdfs://node-master.lan:9000/user/hadoop/hadoop.output"

spark-build:
	cd spark-temperature && sbt package

spark-deploy: spark-build
	scp spark-temperature/target/scala-2.11/spark-temperature_2.11-0.1.jar root@124.70.146.167:~/hadoop-docker/hadoop_namenode

spark-run:
	ssh root@124.70.146.167 "cd ~/hadoop-docker && docker-compose exec -T hadoop-namenode hadoop fs -rm -r hdfs://node-master.lan:9000/user/hadoop/spark.output || true"
	ssh root@124.70.146.167 "/opt/spark/bin/spark-submit --master spark://node-master.lan:7077 ~/hadoop-docker/hadoop_namenode/spark-temperature_2.11-0.1.jar hdfs://node-master.lan:9000/user/hadoop/input/London2013.csv hdfs://node-master.lan:9000/user/hadoop/spark.output"

spark-run-local: spark-build
	spark-submit spark-temperature/target/scala-2.11/spark-temperature_2.11-0.1.jar ~/Work/data/London2013.csv spark.output
	cat spark.output/part-00000
	rm -rf spark.output

hadoop-cat:
	ssh root@124.70.146.167 "cd ~/hadoop-docker && docker-compose exec -T hadoop-namenode hadoop fs -cat /user/hadoop/hadoop.output/part-r-00000"

spark-cat:
	ssh root@124.70.146.167 "cd ~/hadoop-docker && docker-compose exec -T hadoop-namenode hadoop fs -cat /user/hadoop/spark.output/part-00000"
