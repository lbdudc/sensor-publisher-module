/*%@
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + 'Resource.java',
          context: sen
      };
    });
%*/
/*%
const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
    return sensor.isMoving === true; });
%*/

package es.udc.lbd.gema.lps.web.rest.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import es.udc.lbd.gema.lps.model.service.exceptions.NotFoundException;
import es.udc.lbd.gema.lps.model.service.dto.sensor.DataDTO;
import jakarta.inject.Inject;
import es.udc.lbd.gema.lps.model.service.sensor./*%= normalize(context.id, true) %*/Service;
import es.udc.lbd.gema.lps.model.service.dto.sensor./*%= normalize(context.id, true) %*/StateRequestDto;
/*% if (hasMovingSensors) { %*/
import es.udc.lbd.gema.lps.web.rest.custom.FeatureCollectionJSON;
/*% } %*/

@RestController
@RequestMapping(/*%= normalize(context.id, true) %*/Resource./*%= camelToSnakeCase(normalize(context.id)).toUpperCase() %*/_RESOURCE_URL)
public class /*%= normalize(context.id, true) %*/Resource {

    public final static String /*%= camelToSnakeCase(normalize(context.id)).toUpperCase() %*/_RESOURCE_URL = "/api/sensors//*%= pluralize(normalize(context.id)) %*/";

    private static final Logger logger = LoggerFactory.getLogger(/*%= normalize(context.id, true) %*/Resource.class);

    @Inject
    private /*%= normalize(context.id, true) %*/Service /*%= normalize(context.id) %*/Service;


    @PostMapping("/data")
    public ResponseEntity</*% if (hasMovingSensors) { %*/FeatureCollectionJSON/*% } else { %*/List<DataDTO>/*% } %*/> getData(@RequestBody /*%= normalize(context.id, true) %*/StateRequestDto params) {
      return new ResponseEntity<>(/*%= normalize(context.id) %*/Service.getData(params), HttpStatus.OK);
    }

    /*% if (!hasMovingSensors) { %*/
    @PostMapping("/{id}/data")
    public ResponseEntity<DataDTO> getData(@PathVariable Long id, @RequestBody /*%= normalize(context.id, true) %*/StateRequestDto params)
      throws NotFoundException {
      return new ResponseEntity<>(/*%= normalize(context.id) %*/Service.getData(id, params), HttpStatus.OK);
    }

    /*% } %*/
    /*% if(hasMovingSensors && feature.SV_P_SensorInfo) { %*/
    @PostMapping("/{id}/info")
    public ResponseEntity<Object> getData(
        @PathVariable Long id, @RequestBody /*%= normalize(context.id, true) %*/StateRequestDto params)
        throws NotFoundException {
      return new ResponseEntity<>(/*%= normalize(context.id) %*/Service.getInfo(id, params), HttpStatus.OK);
    }

    /*% } %*/
    @PostMapping("/{id}/data/histogram")
    public ResponseEntity<List<DataDTO>> getHistogramData(
      @PathVariable Long id, @RequestBody /*%= normalize(context.id, true) %*/StateRequestDto params) throws NotFoundException {
      return new ResponseEntity<>(/*%= normalize(context.id) %*/Service.getDataHistogram(id, params), HttpStatus.OK);
    }

}
