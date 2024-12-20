/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Producers) { %*/
package es.udc.lbd.gema.lps.model.service;

import java.util.List;

public interface KafkaService {

  // Send a batch of messages to a topic
  public void sendBatchMessages(String topic, List<String> messages);

  // Send a message to a topic
  public void sendMessage(String topic, String message);
}
/*% } %*/
