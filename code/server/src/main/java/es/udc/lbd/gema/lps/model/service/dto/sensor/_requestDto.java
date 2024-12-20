/*%@
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors
  .map(function(sen) {
    return {
      fileName: normalize(sen.id, true) + 'StateRequestDto.java',
      context: sen
    };
  });
%*/
/*%
  const dimensions = [];
  data.dataWarehouse.sensors
    .filter(sen => sen.id === context.id)
    .forEach(function(sensor) {
      const dims = sensor.dimensions;
      dims
        .filter(dim => dim.type === "CATEGORICAL")
        .forEach(dim => {
          dimensions.push(dim);
        });
  });
  var hasCategoricalDims = dimensions.length > 0;
%*/

package es.udc.lbd.gema.lps.model.service.dto.sensor;

import es.udc.lbd.gema.lps.model.domain.sensor.CalcAggregation;
import es.udc.lbd.gema.lps.model.domain.sensor.TemporalAggregation;
import es.udc.lbd.gema.lps.model.domain.sensor./*%= normalize(context.id, true) %*/SpatialAggregation;
import es.udc.lbd.gema.lps.model.domain.sensor./*%= normalize(context.id, true) %*/SpatialFilter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class /*%= normalize(context.id, true) %*/StateRequestDto {

  private LocalDateTime start;
  private LocalDateTime end;
  private /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation;
  private TemporalAggregation temporalAggregation;
  private CalcAggregation calcAggregation;
  private String property;
  private Map<String, Object> filters;
  private /*%= normalize(context.id, true) %*/SpatialFilter spatialFilter;
  private Integer spatialFilterId;
  private String propertyAggregation;
  /*% if (hasCategoricalDims) { %*/
  private String categoryAggregation;
  private String categoryFilter;
  private String categoryFrom;
  private String categoryTo;
  /*% } %*/
  private List<String> properties;

  public /*%= normalize(context.id, true) %*/StateRequestDto() {}

  public LocalDateTime getStart() {
    return start;
  }

  public void setStart(LocalDateTime start) {
    this.start = start;
  }

  public LocalDateTime getEnd() {
    return end;
  }

  public void setEnd(LocalDateTime end) {
    this.end = end;
  }

  public /*%= normalize(context.id, true) %*/SpatialAggregation getSpatialAggregation() {
    return spatialAggregation;
  }

  public void setSpatialAggregation(/*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation) {
    this.spatialAggregation = spatialAggregation;
  }

  public TemporalAggregation getTemporalAggregation() {
    return temporalAggregation;
  }

  public void setTemporalAggregation(TemporalAggregation temporalAggregation) {
    this.temporalAggregation = temporalAggregation;
  }

  public CalcAggregation getCalcAggregation() {
    return calcAggregation;
  }

  public void setCalcAggregation(CalcAggregation calcAggregation) {
    this.calcAggregation = calcAggregation;
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public Map<String, Object> getFilters() {
    return filters;
  }

  public void setFilters(Map<String, Object> filters) {
    this.filters = filters;
  }

  public /*%= normalize(context.id, true) %*/SpatialFilter getSpatialFilter() {
    return spatialFilter;
  }

  public void setSpatialFilter(/*%= normalize(context.id, true) %*/SpatialFilter spatialFilter) {
    this.spatialFilter = spatialFilter;
  }

  public Integer getSpatialFilterId() {
    return spatialFilterId;
  }

  public void setSpatialFilterId(Integer spatialFilterId) {
    this.spatialFilterId = spatialFilterId;
  }

  public String getPropertyAggregation() {
    return propertyAggregation;
  }

  public void setPropertyAggregation(String propertyAggregation) {
    this.propertyAggregation = propertyAggregation;
  }

  public List<String> getProperties() {
    return properties;
  }

  public void setProperties(List<String> properties) {
    this.properties = properties;
  }

  /*% if (hasCategoricalDims) { %*/
  public String getCategoryAggregation() {
    return categoryAggregation;
  }

  public void setCategoryAggregation(String categoryAggregation) {
    this.categoryAggregation = categoryAggregation;
  }

  public String getCategoryFilter() {
    return categoryFilter;
  }

  public void setCategoryFilter(String categoryFilter) {
    this.categoryFilter = categoryFilter;
  }

  public String getCategoryFrom() {
    return categoryFrom;
  }

  public void setCategoryFrom(String categoryFrom) {
    this.categoryFrom = categoryFrom;
  }

  public String getCategoryTo() {
    return categoryTo;
  }

  public void setCategoryTo(String categoryTo) {
    this.categoryTo = categoryTo;
  }
  /*% } %*/
}
