/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.model.service.dto;

import jakarta.persistence.Tuple;

public class MinMaxDTO {
  private Object min;
  private Object max;

  public MinMaxDTO() {}

  public MinMaxDTO(Tuple tupla) {
    this.min = tupla.get("min");
    this.max = tupla.get("max");
  }

  public Object getMin() {
    return min;
  }

  public void setMin(Object min) {
    this.min = min;
  }

  public Object getMax() {
    return max;
  }

  public void setMax(Object max) {
    this.max = max;
  }
}
/*% } %*/
