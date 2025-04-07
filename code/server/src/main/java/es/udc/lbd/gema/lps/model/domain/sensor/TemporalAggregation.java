/*% if (feature.SensorViewer) {
  const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
    return sensor.isMoving === true; });
%*/

package es.udc.lbd.gema.lps.model.domain.sensor;

public enum TemporalAggregation {
  YEAR,
  MONTH,
  WEEK,
  DAY,
  HOUR,
  MINUTE,
  SECOND,
  /*% if (hasMovingSensors) { %*/
  RANGE,
  /*% } %*/
  NONE;
}
/*% } %*/
