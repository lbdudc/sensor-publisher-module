/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDateTime;

public class GenericValueSerializer extends JsonSerializer<Object> {

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    if (value instanceof LocalDateTime) {
      LocalDateTime dateTime = (LocalDateTime) value;
      gen.writeStartArray();
      gen.writeNumber(dateTime.getYear());
      gen.writeNumber(dateTime.getMonthValue());
      gen.writeNumber(dateTime.getDayOfMonth());
      gen.writeNumber(dateTime.getHour());
      gen.writeNumber(dateTime.getMinute());
      gen.writeNumber(dateTime.getSecond());
      gen.writeNumber(dateTime.getNano());
      gen.writeEndArray();
    } else {
      gen.writeObject(value);
    }
  }
}

/*% } %*/