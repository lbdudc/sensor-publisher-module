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
package es.udc.lbd.gema.lps.model.service.sensor;
import es.udc.lbd.gema.lps.model.service.dto.sensor.DataDTO;
import es.udc.lbd.gema.lps.model.service.dto.sensor./*%= normalize(context.id, true) %*/StateRequestDto;
import java.util.List;
/*%if (feature.SV_P_SensorInfo){ %*/
import es.udc.lbd.gema.lps.model.service.exceptions.NotFoundException;
/*% } %*/

public interface /*%= normalize(context.id, true) %*/Service {

  List<DataDTO> getData(/*%= normalize(context.id, true) %*/StateRequestDto params);
  DataDTO getData(Long id, /*%= normalize(context.id, true) %*/StateRequestDto params)/*% if (feature.SV_P_SensorInfo) { %*/ throws NotFoundException; /*% } else { %*/; /*% } %*/
  List<DataDTO> getDataHistogram(Long id, /*%= normalize(context.id, true) %*/StateRequestDto params);
}
