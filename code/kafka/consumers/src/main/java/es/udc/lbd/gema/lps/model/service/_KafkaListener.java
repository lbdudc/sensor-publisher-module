/*%@
  if (!feature.SV_D_DataInsertion || !feature.SV_D_DI_Consumers) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, false) + '/' + normalize(sen.id, true) + 'KafkaReader.java',
          context: sen
      };
    });
%*/
package es.udc.lbd.gema.lps.model.service./*%= normalize(context.id, false) %*/;

import es.udc.lbd.gema.lps.model.domain./*%= normalize(context.id, true) %*/;
import es.udc.lbd.gema.lps.model.domain.ModelMapper;
import es.udc.lbd.gema.lps.model.service.conditional.ConditionalOnKafkaProperty;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnKafkaProperty(topic = "/*%= normalize(context.id).toLowerCase() %*/", datasource = "'postgres'")
public class /*%= normalize(context.id, true) %*/KafkaReader {


  private static final Logger logger =
    LoggerFactory.getLogger(/*%= normalize(context.id, true) %*/KafkaReader.class);

  private static final String ENTITY_NAME = "t_/*%= normalize(context.id, false) %*/";

  @Autowired private /*%= normalize(context.id, true) %*/Writer writer;

  @KafkaListener(
      id = "/*%= normalize(context.id, false).toLowerCase() %*/-postgres-consumer",
      topics = "/*%= normalize(context.id, false).toLowerCase() %*/",
      groupId = "/*%= normalize(context.id, false).toLowerCase() %*/-postgres-consumer",
      autoStartup = "true")
  public void consume(
      @Payload List<String> messages,
      @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
      @Header(KafkaHeaders.OFFSET) List<Long> offsets,
      Acknowledgment ack) {

    try {
      List</*%= normalize(context.id, true) %*/> events =
          messages.stream()
              .map(message -> ModelMapper.to/*%= normalize(context.id, true) %*/(message))
              .collect(Collectors.toList());

      writer.insertMedidas(ENTITY_NAME, events);
      logger.info("Consumed Messages(/*%= normalize(context.id, true) %*/): " + events.size());
      ack.acknowledge();
    } catch (Exception e) {
      logger.error("Error consuming messages (/*%= normalize(context.id, true) %*/):", e);
    }
  }


}
