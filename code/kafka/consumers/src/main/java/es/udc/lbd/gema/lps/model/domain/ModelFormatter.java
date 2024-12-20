/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Consumers) { %*/
package es.udc.lbd.gema.lps.model.domain;

public class ModelFormatter {

  /*% data.dataWarehouse.sensors.forEach(function(sensor) { %*/
  public static /*%= normalize(sensor.id, true) %*/ apply/*%= normalize(sensor.id, true) %*/Triggers(/*%= normalize(sensor.id, true) %*/ /*%= normalize(sensor.id, false) %*/) {
    /*%= normalize(sensor.id, true) %*/ /*%= normalize(sensor.id, false) %*/Res = new /*%= normalize(sensor.id, true) %*/();

    // TODO, add triggers here if needed

    return /*%= normalize(sensor.id, false) %*/Res;
  }

  /*% }); %*/
}
/*% } %*/
