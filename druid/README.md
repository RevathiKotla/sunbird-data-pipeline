## Set up Druid

#### Download druid using the following commands

```
curl -O http://static.druid.io/artifacts/releases/druid-0.12.3-bin.tar.gz
tar -xzf druid-0.12.3-bin.tar.gz
cd druid-0.12.3
```
#### Start up zookeeper

```curl http://www.gtlib.gatech.edu/pub/apache/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz -o zookeeper-3.4.10.tar.gz
tar -xzf zookeeper-3.4.10.tar.gz
cd zookeeper-3.4.10
cp conf/zoo_sample.cfg conf/zoo.cfg
./bin/zkServer.sh start
```

### Start druid services

```
nohup java `cat conf/druid/coordinator/jvm.config | xargs` -cp "conf/druid/_common:conf/druid/coordinator:lib/*" io.druid.cli.Main server coordinator >> /data/druid/logs/coordinator.log &

nohup java `cat conf/druid/overlord/jvm.config | xargs` -cp "conf/druid/_common:conf/druid/overlord:lib/*" io.druid.cli.Main server overlord >> /data/druid/logs/overlord.log &

nohup java `cat conf/druid/historical/jvm.config | xargs` -cp "conf/druid/_common:conf/druid/historical:lib/*" io.druid.cli.Main server historical >> /data/druid/logs/historical.log &

nohup java `cat conf/druid/middleManager/jvm.config | xargs` -cp "conf/druid/_common:conf/druid/middleManager:lib/*" io.druid.cli.Main server middleManager >> /data/druid/logs/middleManager.log &

nohup java `cat conf/druid/broker/jvm.config | xargs` -cp "conf/druid/_common:conf/druid/broker:lib/*" io.druid.cli.Main server broker >> /data/druid/logs/broker.log &
```

### Indexing APIs

The /indexer/v1/task api can be used to index data. The telemetry-index.json is the ingestion spec file that will define what dimensions and metrics that need to be indexed.

```
curl -X POST -H 'Content-Type: application/json' -d@telemetry-index.json  http://localhost:8081/druid/indexer/v1/task
```

### Deleting a datasource

In order to delete a datasource (which will delete all segments), the following DELETE api can be used. This will only mark the data source for delete. 

```curl -XDELETE http://localhost:8081/druid/coordinator/v1/datasources/telemetry```

We need to run the following kill task api to delete the data source after it is marked for delete. 

```
curl -X 'POST' -H 'Content-Type:application/json' http://localhost:8081/druid/indexer/v1/task -d '{
    "type": "kill",
    "id": "telemetry-delete",
    "dataSource": "telemetry",
    "interval" : "2018-11-01/2018-11-30"
}'
```

