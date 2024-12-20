/*%@
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + 'SpatialFilter.java',
          context: sen
      };
    });
%*/

package es.udc.lbd.gema.lps.model.domain.sensor;
import com.fasterxml.jackson.annotation.JsonProperty;

/*% if (feature.SensorViewer) { %*/
public enum /*%= normalize(context.id, true) %*/SpatialFilter {
   /*% const entities = []; %*/
  /*% data?.dataWarehouse?.sensors.filter(sensor => sensor.id === context.id).forEach(function(sensor) { %*/
    /*% sensor.dimensions.forEach(function(dimension, dimensionIndex) { %*/
      /*% dimension.entities?.forEach(function(entity, index) { %*/
        /*% entities.push(entity); %*/
      /*% }); %*/
    /*% }); %*/
  /*% }); %*/

  /*% entities.flat().forEach(function(entity, index) { %*/
    @JsonProperty("/*%= normalize(entity.toLowerCase(), true) %*/")
    /*%= normalize(entity.toLowerCase(), true) %*/
    /*% if (index < entities.length - 1) { %*/,/*% } %*/
  /*% }); %*/
}
/*% } %*/







