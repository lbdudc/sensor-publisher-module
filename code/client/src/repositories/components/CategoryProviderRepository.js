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
import { HTTP } from "@/common/http-common";
import Logger from "js-logger";
const logger = Logger.get("logger");
const RESOURCE_NAME = "/sensors/categories";

export default {
  async getItems(options) {
    try {
      return (await HTTP.get(`${RESOURCE_NAME}`, options)).data;
    } catch (err) {
      logger.error("Error fetching instants with params " + options);
      throw err;
    }
  },
};
/*% } %*/
/*% } %*/

