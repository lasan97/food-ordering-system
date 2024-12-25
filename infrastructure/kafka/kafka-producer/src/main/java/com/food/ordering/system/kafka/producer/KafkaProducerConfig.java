package com.food.ordering.system.kafka.producer;

import com.food.ordering.system.kafka.config.data.KafkaConfigData;
import com.food.ordering.system.kafka.config.data.KafkaProducerConfigData;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig<K extends Serializable, V extends SpecificRecordBase> {

	private final KafkaConfigData kafkaConfigData;
	private final KafkaProducerConfigData kafkaProducerConfigData;

	@Bean
	public Map<String, Object> produceConfig() {
		Map<String, Object> config = new HashMap<>();

		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
		config.put(kafkaConfigData.getSchemaRegistryUrlKey(), kafkaConfigData.getSchemaRegistryUrl());
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducerConfigData.getKeySerializerClass());
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducerConfigData.getValueSerializerClass());
		config.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaProducerConfigData.getBatchSize() * kafkaProducerConfigData.getBatchSizeBoostFactor());
		config.put(ProducerConfig.LINGER_MS_CONFIG, kafkaProducerConfigData.getLingerMs());
		config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProducerConfigData.getCompressionType());
		config.put(ProducerConfig.ACKS_CONFIG, kafkaProducerConfigData.getAcks());
		config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaProducerConfigData.getRequestTimeoutMs());
		config.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerConfigData.getRetryCount());

		return config;
	}

	@Bean
	public ProducerFactory<K, V> producerFactory() {
		return new DefaultKafkaProducerFactory<>(produceConfig());
	}

	@Bean
	public KafkaTemplate<K, V> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}
