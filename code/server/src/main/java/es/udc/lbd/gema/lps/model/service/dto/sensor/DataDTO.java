/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.model.service.dto.sensor;

import java.util.Map;

public class DataDTO {

  private String id;

  private Map<String, Object> data;

  public DataDTO() {}

  public DataDTO(String id, Map<String, Object> data) {
    this.id = id;
    this.data = data;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
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
