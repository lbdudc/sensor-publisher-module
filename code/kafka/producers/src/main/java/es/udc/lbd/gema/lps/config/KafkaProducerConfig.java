/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Producers) { %*/
package es.udc.lbd.gema.lps.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

  @Value(value = "${kafka.servers}")
  private String bootstrapAddress;

  @Bean
  public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    // Producer configs
    configProps.put(ProducerConfig.ACKS_CONFIG, "all");
    configProps.put(ProducerConfig.RETRIES_CONFIG, 0);
    configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
    configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));
    configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, Integer.toString(32 * 1024));
    configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
/*% } %*/
