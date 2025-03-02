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
