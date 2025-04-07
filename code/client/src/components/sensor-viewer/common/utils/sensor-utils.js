/*% if (feature.SensorViewer) { %*/
import sensors from "@/components/sensor-viewer/common/config-files/sensors.json";
  /*% const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
        return sensor.isMoving === true; });
      if (hasMovingSensors) { %*/
import spatialDimOperations from "@/components/sensor-viewer/common/config-files/spatialDimOperations.json";
      /*% } %*/

export const getSensorSpecById = (sensorId) => {
  let spec =
  sensors.specs.find(
    (sensorSpec) => sensorSpec.id.toLowerCase() === sensorId.toLowerCase()
  ) || null;
  /*% if (hasMovingSensors) { %*/
  spec.store.spatial_operation = spatialDimOperations.spatial_operation;
  /*% } %*/
  return spec
};
/*% } %*/
