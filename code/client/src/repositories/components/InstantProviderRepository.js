/*% if (feature.SensorViewer) { %*/
import { HTTP } from "@/common/http-common";
import Logger from "js-logger";
const logger = Logger.get("logger");
const RESOURCE_NAME = "entities/instants";

export default {
  async getByDate(options) {
    try {
      return (await HTTP.get(`${RESOURCE_NAME}/elastic`, options)).data;
    } catch (err) {
      logger.error("Error fetching instants with params " + options);
      throw err;
    }
  },

  async getInstants(options, temporal_aggregation, locationURL) {
    try {
      const result = await HTTP.get(
        `${RESOURCE_NAME}/${locationURL}/${temporal_aggregation}/elastic`,
        options
      );
      return result.data;
    } catch (err) {
      logger.error("Error fetching all", options);
      throw err;
    }
  },
  async getYearItems(locationURL, options) {
    try {
      const result = await HTTP.get(
        `${RESOURCE_NAME}/${locationURL}/all-years/elastic`,
        options
      );
      return result?.data;
    } catch (err) {
      logger.error("Error fetching all", {});
      throw err;
    }
  },
  async getLastInstant(locationURL) {
    try {
      const result = await HTTP.get(
        `${RESOURCE_NAME}/${locationURL}/last-instant/elastic`,
        {}
      );
      return result.data;
    } catch (err) {
      logger.error("Error fetching all", {});
      throw err;
    }
  },
};
/*% } %*/
