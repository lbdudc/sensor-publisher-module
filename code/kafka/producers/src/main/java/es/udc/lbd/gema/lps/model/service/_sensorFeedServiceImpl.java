/*%@
  if (!feature.SV_D_DataInsertion || !feature.SV_D_DI_Producers) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + 'FeedServiceImpl.java',
          context: sen
      };
    });
%*/
package es.udc.lbd.gema.lps.model.service;

import es.udc.lbd.gema.lps.model.domain./*%= normalize(context.id, true) %*/;
import es.udc.lbd.gema.lps.model.service.conditional.ConditionalOnKafkaTopicProperty;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnKafkaTopicProperty("/*%= normalize(context.id).toLowerCase() %*/")
public class /*%= normalize(context.id, true) %*/FeedServiceImpl implements /*%= normalize(context.id, true) %*/FeedService {

  @Inject private KafkaService kafkaService;

  private static final Logger logger = LoggerFactory.getLogger(/*%= normalize(context.id, true) %*/FeedServiceImpl.class);

  @Override
  // Every /*%= (context.time) %*/ seconds
  @Scheduled(fixedDelay = /*%= (context.time * 1000) %*/)
  public void fetch/*%= normalize(context.id, true) %*/InfoRealTime() throws Exception {
    logger.debug("Starting /*%= normalize(context.id, true) %*/ data import...");
    try {

      // TODO: Add your code here
      // Send data to Kafka
      List</*%= normalize(context.id, true) %*/> /*%= normalize(context.id, false) + "s" %*/ = new ArrayList<>();

      kafkaService.sendBatchMessages(
          "/*%= normalize(context.id).toLowerCase() %*/",
          /*%= normalize(context.id, false) + "s" %*/.stream().map(/*%= normalize(context.id, false) %*/ -> /*%= normalize(context.id, false) %*/.toJSON()).collect(Collectors.toList()));
    } catch (Exception ex) {
      logger.error(ex.getMessage());
      throw ex;
    }
    logger.debug("/*%= normalize(context.id, true) %*/ import completed");
  }
}
