/*%@ 
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors.filter(function(en){return en.factTableEntity ? true : false }).map(function(en) {
    return {
        fileName: normalize(en.factTableEntity, true) + 'Id.java',
        context: en
    };
  })
%*/
 package es.udc.lbd.gema.lps.model.domain;

 import java.io.Serializable;
 import java.time.LocalDateTime;
 import java.util.Objects;

 public class /*%= normalize(context.factTableEntity, true) %*/Id implements Serializable {

  private Long id;

  private LocalDateTime date;

  public /*%= normalize(context.factTableEntity, true) %*/Id() {}

  public /*%= normalize(context.factTableEntity, true) %*/Id(Long id, LocalDateTime date) {
    this.id = id;
    this.date = date;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    /*%= normalize(context.factTableEntity, true) %*/Id that = (/*%= normalize(context.factTableEntity, true) %*/Id) o;
    return Objects.equals(id, that.id) && Objects.equals(date, that.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, date);
  }
}
