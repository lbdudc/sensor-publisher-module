/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Consumers) { %*/
package es.udc.lbd.gema.lps.model.service.conditional;

import java.lang.annotation.*;
import org.springframework.context.annotation.Conditional;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(ConditionalOnKafka.class)
public @interface ConditionalOnKafkaProperty {
  String topic();

  String datasource();
}
/*% } %*/
