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
package es.udc.lbd.gema.lps.model.service.sensor;

import es.udc.lbd.gema.lps.model.domain.sensor.TemporalAggregation;
import es.udc.lbd.gema.lps.model.repository.sensor.GenericRepository;
import es.udc.lbd.gema.lps.model.service.dto.sensor.CategoryDTO;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CategoryProviderServiceImpl implements CategoryProviderService {

  @Inject private GenericRepository categoryProviderRepository;

  @Override
  public List<CategoryDTO> findAllByCategory(
      String repo_url,
      String sensor_name,
      String category,
      LocalDateTime date,
      TemporalAggregation aggregation,
      String sensorId) {

    return categoryProviderRepository
        .findAllByCategory(repo_url, sensor_name, category, date, aggregation, sensorId)
        .stream()
        .map(e -> new CategoryDTO(e.getLabel(), e.getValue()))
        .collect(Collectors.toList());
  }
}
/*% } %*/
/*% } %*/
