package com.cvicse.highway.domain;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;
    @Value(value = "${kafka.topicInput}")
    public String topicInput;
    @Value(value = "${kafka.topicOutput}")
    public String topicOutput;
    // @Bean
    // public ProducerFactory<String, String> producerFactory() {
    // Map<String, Object> configProps = new HashMap<>();
    // configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    // configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
    // StringSerializer.class);
    // configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
    // StringSerializer.class);
    // configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "20971520");

    // return new DefaultKafkaProducerFactory<>(configProps);
    // }
    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProps());
    }

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // ...
        return props;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory(
            ConsumerFactory<Integer, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ProducerFactory<Integer, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(senderProps());
    }

    private Map<String, Object> senderProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapAddress);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // ...
        return props;
    }

    public class Listener {
        @KafkaListener(id = "OUTSIDE", topics = "${kafka.topicInput}")
        public void listen1(String in) {
            //System.out.println(in);
        }

    }

    @Bean
    public Listener listener() {
        return new Listener();
    }

    @Bean
    public KafkaTemplate<Integer, String> kafkaTemplate(ProducerFactory<Integer, String> producerFactory) {
        return new KafkaTemplate<Integer, String>(producerFactory);
    }

}
