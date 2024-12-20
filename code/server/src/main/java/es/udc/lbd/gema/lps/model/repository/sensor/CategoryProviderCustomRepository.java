/*% if (feature.SensorViewer) { %*/
/*%
  const dimensions = [];
  data.dataWarehouse.sensors.forEach(function(sensor) {
    const dims = sensor.dimensions;
    dims
      .filter(dim => dim.type === "CATEGORICAL")
      .forEach(dim => {
        dimensions.push(dim);
      });
  });
  var hasCategoricalDims = dimensions.length > 0;
%*/
/*% if (hasCategoricalDims) { %*/
  package es.udc.lbd.gema.lps.model.repository.sensor;

  import es.udc.lbd.gema.lps.model.domain.sensor.TemporalAggregation;
  import es.udc.lbd.gema.lps.model.service.dto.sensor.CategoryDTO;
  import java.time.LocalDateTime;
  import java.util.List;

  public interface CategoryProviderCustomRepository {

    List<CategoryDTO> findAllByCategory(
        String repo_url,
        String sensor_name,
        String category,
        LocalDateTime date,
        TemporalAggregation aggregation,
        String sensorId);
  }
/*% } %*/
/*% } %*/
