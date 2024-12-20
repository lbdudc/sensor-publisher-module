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
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

public class CategoryProviderCustomRepositoryImpl implements CategoryProviderCustomRepository {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<CategoryDTO> findAllByCategory(
      String repo_url,
      String sensor_name,
      String category,
      LocalDateTime date,
      TemporalAggregation aggregation,
      String sensorId) {

    String sqlQueryString =
        "select distinct "
            + category
            + " from t_category_"
            + camelToSnake(sensor_name)
            + " order by "
            + category;
    Query query = entityManager.createNativeQuery(sqlQueryString);
    List<Object[]> resultList = query.getResultList();
    List<CategoryDTO> categories = new ArrayList<>();
    for (Object alarm : resultList) {
      categories.add(new CategoryDTO((String) alarm, (String) alarm));
    }
    return categories;
  }

  private String camelToSnake(String camelCase) {
    String snakeCase = camelCase.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    return snakeCase;
  }
}
/*% } %*/
/*% } %*/
