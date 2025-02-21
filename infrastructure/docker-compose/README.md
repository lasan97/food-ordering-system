# docker compose command

``` bash
docker compose -f postgres.yml up
```
``` bash
docker compose -f common.yml -f zookeeper.yml up
```
``` bash
docker compose -f common.yml -f kafka_cluster.yml up
```
``` bash
docker compose -f common.yml -f init_kafka.yml up
```