/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.model.service.dto;

import java.util.Map;

public class DataDTO {

  private Long id;

  private Map<String, Object> data;

  public DataDTO() {}

  public DataDTO(Long id, Map<String, Object> data) {
    this.id = id;
    this.data = data;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }
}
/*% } %*/
