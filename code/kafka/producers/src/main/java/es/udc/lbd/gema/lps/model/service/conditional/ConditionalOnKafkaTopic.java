/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Producers) { %*/
package es.udc.lbd.gema.lps.model.service.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ConditionalOnKafkaTopic implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    Environment environment = context.getEnvironment();
    String expectedKafkaTopic =
        metadata
            .getAnnotationAttributes(ConditionalOnKafkaTopicProperty.class.getName())
            .get("value")
            .toString();
    String kafkaTopic = environment.getProperty("KAFKA_TOPIC");
    return expectedKafkaTopic.equalsIgnoreCase(kafkaTopic);
  }
}
/*% } %*/
