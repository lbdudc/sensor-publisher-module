/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Producers) { %*/
package es.udc.lbd.gema.lps.model.service.conditional;

import java.lang.annotation.*;
import org.springframework.context.annotation.Conditional;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(ConditionalOnKafkaTopic.class)
public @interface ConditionalOnKafkaTopicProperty {
  String value();
}
/*% } %*/
