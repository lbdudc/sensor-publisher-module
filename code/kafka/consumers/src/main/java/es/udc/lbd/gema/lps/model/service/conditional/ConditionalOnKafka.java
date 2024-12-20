/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Consumers) { %*/
package es.udc.lbd.gema.lps.model.service.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ConditionalOnKafka implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Environment environment = context.getEnvironment();
    String expectedKafkaTopic =
        metadata
            .getAnnotationAttributes(ConditionalOnKafkaProperty.class.getName())
            .get("topic")
            .toString();
    String expectedKafkaDataSource =
        metadata
            .getAnnotationAttributes(ConditionalOnKafkaProperty.class.getName())
            .get("datasource")
            .toString();

    String kafkaTopic = environment.getProperty("KAFKA_TOPIC");
    String kafkaDataSource = environment.getProperty("KAFKA_DATASOURCE");

    return expectedKafkaTopic.equalsIgnoreCase(kafkaTopic)
        && expectedKafkaDataSource.equalsIgnoreCase(kafkaDataSource);
  }
}
/*% } %*/
