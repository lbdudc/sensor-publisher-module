/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Producers) { %*/
package es.udc.lbd.gema.lps.model.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaServiceImpl implements KafkaService {

  @Autowired private KafkaTemplate<String, String> kafkaTemplate;

  public void sendBatchMessages(String topic, List<String> message) {
    for (String msg : message) {
      kafkaTemplate.send(topic, msg);
    }
  }

  @Override
  public void sendMessage(String topic, String message) {
    kafkaTemplate.send(topic, message);
  }
}
/*% } %*/
