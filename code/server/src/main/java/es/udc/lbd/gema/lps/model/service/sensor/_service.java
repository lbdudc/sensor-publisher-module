/*%@
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + 'Service.java',
          context: sen
      };
    });
%*/
/*%
const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
    return sensor.isMoving === true; });
%*/
package es.udc.lbd.gema.lps.model.service.sensor;
import es.udc.lbd.gema.lps.model.service.dto.sensor.DataDTO;
import es.udc.lbd.gema.lps.model.service.dto.sensor./*%= normalize(context.id, true) %*/StateRequestDto;
import java.util.List;
/*%if (feature.SV_P_SensorInfo){ %*/
import es.udc.lbd.gema.lps.model.service.exceptions.NotFoundException;
/*% } %*/
/*% if (hasMovingSensors) { %*/
import es.udc.lbd.gema.lps.web.rest.custom.FeatureCollectionJSON;
/*% } %*/

public interface /*%= normalize(context.id, true) %*/Service {

  /*% if (hasMovingSensors) { %*/ FeatureCollectionJSON /*% } else { %*/ List<DataDTO> /*% } %*/ getData(/*%= normalize(context.id, true) %*/StateRequestDto params);

  /*% if (!hasMovingSensors) { %*/
  DataDTO getData(Long id, /*%= normalize(context.id, true) %*/StateRequestDto params)/*% if (feature.SV_P_SensorInfo) { %*/ throws NotFoundException /*% } %*/;

  /*% } %*/
  /*% if (feature.SV_P_SensorInfo && hasMovingSensors) { %*/
  Object getInfo(Long id, ShipObservationStateRequestDto params) throws NotFoundException;

  /*% } %*/
  List<DataDTO> getDataHistogram(Long id, /*%= normalize(context.id, true) %*/StateRequestDto params);
}
