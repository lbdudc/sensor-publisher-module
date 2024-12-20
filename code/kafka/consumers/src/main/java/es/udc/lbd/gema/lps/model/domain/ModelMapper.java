/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Consumers) { %*/
package es.udc.lbd.gema.lps.model.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelMapper {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final Logger logger = LoggerFactory.getLogger(ModelMapper.class);

  /*% data.dataWarehouse.sensors.forEach(function(sensor) { %*/
  public static /*%= normalize(sensor.id, true) %*/ to/*%= normalize(sensor.id, true) %*/(String json) {
    try {
      /*%= normalize(sensor.id, true) %*/ /*%= normalize(sensor.id, false) %*/ = mapper.readValue(json, /*%= normalize(sensor.id, true) %*/.class);

      // Apply triggers for error values
      return ModelFormatter.apply/*%= normalize(sensor.id, true) %*/Triggers(/*%= normalize(sensor.id, false) %*/);
    } catch (IOException e) {
      logger.error("Error on mapper", e);
    }

    return null;
  }
  /*% }); %*/
}
/*% } %*/
