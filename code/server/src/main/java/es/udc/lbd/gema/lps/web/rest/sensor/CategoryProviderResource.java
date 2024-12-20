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
package es.udc.lbd.gema.lps.web.rest.sensor;

import es.udc.lbd.gema.lps.model.domain.sensor.TemporalAggregation;
import es.udc.lbd.gema.lps.model.service.sensor.CategoryProviderServiceImpl;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CategoryProviderResource.CATEGORY_PROVIDER_RESOURCE_URL)
public class CategoryProviderResource {

  public static final String CATEGORY_PROVIDER_RESOURCE_URL = "/api/sensors/categories";

  @Inject private CategoryProviderServiceImpl categoryProviderService;

  @GetMapping()
  public @ResponseBody ResponseEntity<?> findAllByCategory(
      @RequestParam("repoUrl") String repo_url,
      @RequestParam("sensorName") String sensor_name,
      @RequestParam("category") String category,
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
      @RequestParam(value = "aggregation", required = false) TemporalAggregation aggregation,
      @RequestParam(value = "sensorId", required = false) String sensorId) {
    return new ResponseEntity<>(
        categoryProviderService.findAllByCategory(
            repo_url, sensor_name, category, date, aggregation, sensorId),
        HttpStatus.OK);
  }
}
/*% } %*/
/*% } %*/
