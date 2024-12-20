/*% if (feature.SensorViewer) { %*/
import sensors from "@/components/sensor-viewer/common/config-files/sensors.json";

export const getSensorSpecById = (sensorId) => {
  return (
    sensors.specs.find(
      (sensorSpec) => sensorSpec.id.toLowerCase() === sensorId.toLowerCase()
    ) || null
  );
};
/*% } %*/
