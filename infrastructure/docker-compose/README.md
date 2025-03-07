# docker compose command

``` bash
docker compose -f common.yml -f postgres.yml up
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

# debezium jar 파일
`./volums/debezium/schemaregistry-7.2.5/` 에 다음 jar 추가

- [common-utils](https://packages.confluent.io/maven/io/confluent/common-utils/7.2.5/common-utils-7.2.5.jar)
- [common-config](https://packages.confluent.io/maven/io/confluent/common-config/7.2.5/common-config-7.2.5.jar)

- [kafka-avro-serializer](https://packages.confluent.io/maven/io/confluent/kafka-avro-serializer/7.2.5/kafka-avro-serializer-7.2.5.jar)
- [kafka-connect-avro-converter](https://packages.confluent.io/maven/io/confluent/kafka-connect-avro-converter/7.2.5/kafka-connect-avro-converter-7.2.5.jar)
- [kafka-connect-avro-data](https://packages.confluent.io/maven/io/confluent/kafka-connect-avro-data/7.2.5/kafka-connect-avro-data-7.2.5.jar)

- [kafka-schema-converter](https://packages.confluent.io/maven/io/confluent/kafka-schema-converter/7.2.5/kafka-schema-converter-7.2.5.jar)
- [kafka-schema-registry](https://packages.confluent.io/maven/io/confluent/kafka-schema-registry/7.2.5/kafka-schema-registry-7.2.5.jar)
- [kafka-schema-registry-client](https://packages.confluent.io/maven/io/confluent/kafka-schema-registry-client/7.2.5/kafka-schema-registry-client-7.2.5.jar)
- [kafka-schema-serializer](https://packages.confluent.io/maven/io/confluent/kafka-schema-serializer/7.2.5/kafka-schema-serializer-7.2.5.jar)

- guava [MavenRepo](https://mvnrepository.com/artifact/com.google.guava/guava/12.0)
  - [guava-jar](https://repo1.maven.org/maven2/com/google/guava/guava/12.0/guava-12.0.jar)

- avro [MavenRepo](https://mvnrepository.com/artifact/org.apache.avro/avro/1.11.1)
  - [avro-jar](https://repo1.maven.org/maven2/org/apache/avro/avro/1.11.1/avro-1.11.1.jar)


# Postgres 커스터마이징
## `postgresql.conf` 수정
### connection 수정
`max_connections` = 100 -> 200

### CUSTOMIZED OPTIONS 추가
```
# Add settings for extensions here
log_min_error_statement = fatal
listen_addresses = '*'
# MODULES
#shared_preload_libraries = 'wal2json'
#REPLICATION
wal_level = logical
max_wal_senders = 4
#wal_keep_segments = 4
#wal_sender_timeout = 60s
max_replication_slots = 4
```
## `pg_hba.conf` 수정

### Replication 설정 추가
```
### REPLICATION ###
local   replication     postgres                                trust
host    replication     postgres    127.0.0.1/32                trust
host    replication     postgres    ::1/128                     trust
```

## postgres 재시작
도커의 경우 restart

pg_ctl 사용시
`sudo -u postgres {pg_ctl_path} -D {postgres_data_path} restart`