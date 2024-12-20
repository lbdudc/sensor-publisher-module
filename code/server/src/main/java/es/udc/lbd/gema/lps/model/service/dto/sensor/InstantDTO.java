/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.model.service.dto.sensor;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import es.udc.lbd.gema.lps.config.GenericValueSerializer;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.poi.ss.formula.functions.T;

public class InstantDTO<T> {
  @JsonSerialize(using = GenericValueSerializer.class)
  private T value;

  private String label;

  public InstantDTO(LocalDateTime value, String format) {
    this((T) value, value.format(DateTimeFormatter.ofPattern(format)));
  }

  public InstantDTO(Timestamp value, String format) {
    this(value.toLocalDateTime(), format);
  }

  public InstantDTO(String value) {
    this((T) value, value);
  }

  public InstantDTO(T value, String label) {
    this.value = value;
    this.label = label;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
/*% } %*/
