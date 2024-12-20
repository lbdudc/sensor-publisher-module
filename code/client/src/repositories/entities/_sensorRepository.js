/*%@
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + 'EntityRepository.js',
          context: sen
      };
    });
%*/
import { HTTP } from "@/common/http-common";
import Logger from "js-logger";

const logger = Logger.get("logger");
const RESOURCE_NAME = "sensors//*%= pluralize(normalize(context.id)) %*/";

export default {
  async getReport() {
    try {
      return (
        await HTTP.get(`${RESOURCE_NAME}/report`, {
          responseType: "blob",
        })
      ).data;
    } catch (err) {
      logger.error("Error getting report");
      throw err;
    }
  },

  async getData(params = {}, options) {
    try {
      const response = (
        await HTTP.post(`${RESOURCE_NAME}/data`, params, options)
      )?.data;
      if (!!response) return response;
      throw new Error("ERR_CANCELED");
    } catch (err) {
      if (err.message != "ERR_CANCELED") {
        logger.error("Error fetching data", params);
      }
      throw err;
    }
  },

  async getDataFromItem(id, params, options) {
    try {
      const response = (
        await HTTP.post(`${RESOURCE_NAME}/${id}/data`, params, options)
      )?.data;
      if (!!response) return response;
      throw new Error("ERR_CANCELED");
    } catch (err) {
      if (err.message != "ERR_CANCELED") {
        logger.error("Error fetching data for element with id " + id);
      }
      throw err;
    }
  },

  async getHistogramDataFromItem(id, params, options) {
    try {
      const response = (
        await HTTP.post(
          `${RESOURCE_NAME}/${id}/data/histogram`,
          params,
          options
        )
      )?.data;
      if (!!response) return response;
      throw new Error("ERR_CANCELED");
    } catch (err) {
      if (err.message != "ERR_CANCELED") {
        logger.error("Error fetching data for element with id " + id);
      }
      throw err;
    }
  },
};
