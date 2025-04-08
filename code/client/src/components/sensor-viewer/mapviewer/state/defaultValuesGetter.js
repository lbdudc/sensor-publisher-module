/*% if (feature.SensorViewer) {
const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
  return sensor.isMoving === true; }); %*/
  import instantService from "@/components/sensor-viewer/common/services/instantService.js";
  import filterService from "@/components/sensor-viewer/common/services/filterService.js";

  export default async (propId, items, store) => {
    return new Promise((resolve) => {
      switch (propId) {
        case "TEMPORAL_AGGREGATION":
          resolve("NONE");
          break;
        case "DATE_FILTER":
          resolve(filterService.getDateFilterDefaultValue(store));
          break;
        case "YEAR_FILTER":
          resolve(filterService.getYearFilterDefaultValue(store));
          break;
        case "INSTANT_FILTER":
          resolve(instantService.getInstantsDefaultValue(items, store));
          break;
        case "PAGE_FILTER":
          resolve(0);
          break;
        case "SHOW_SENSORS":
          resolve(1);
          break;
        case "OPERATIONAL_AGGREGATION":
          resolve(items[0].value);
          break;
        case "PROPERTY_AGGREGATION":
          resolve(items && items.length != 0 ? items[0].value : null);
          break;
        case "CATEGORY_AGGREGATION":
          resolve(items && items.length != 0 ? items[0].value : null);
          break;
        case "CATEGORY_FILTER":
          resolve(store.CATEGORY_FILTER ? store.CATEGORY_FILTER : null);
          break;
        case "LEGEND_TYPE":
          resolve("STATIC");
          break;
        case "REAL_TIME_FLAG":
          resolve(0);
          break;
        /*% if (hasMovingSensors) { %*/
        case "INIT_DATE_FILTER":
          resolve(filterService.getRangeDateFilterDefaultValue(store));
          break;
        case "END_DATE_FILTER":
          resolve(filterService.getRangeDateFilterDefaultValue(store));
          break;
        case "MEASUREMENTS_FLAG":
          resolve(store.MEASUREMENTS_FLAG ? store.MEASUREMENTS_FLAG : 1);
          break;
        /*% } %*/
        default:
          resolve(null);
          break;
      }
    });
  };
  /*% } %*/
