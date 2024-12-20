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
  const entity = data.dataModel.entities[0] || null;
  var pk = normalize(getEntityProperty(entity, 'id').class.split(' ')[0], true);
%*/
package es.udc.lbd.gema.lps.model.repository.sensor;

import es.udc.lbd.gema.lps.model.domain./*%= normalize(entity.name, true) %*/;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenericRepository
    extends JpaRepository</*%= normalize(entity.name, true) %*/, Long>, InstantProviderCustomRepository
    /*% if (hasCategoricalDims) { %*/
      , CategoryProviderCustomRepository
    /*% } %*/
    {}
/*% } %*/