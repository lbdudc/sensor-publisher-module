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
import filterService from "@/components/sensor-viewer/common/services/filterService";
import instantService from "@/components/sensor-viewer/common/services/instantService";

let totalInstantsPages = {
  data: 0,
};

const createGetValuesFunction = (spec) => {
  // Needs the spec to work
  const repo_url = spec.repository_url;
  const sensor_name = spec.name;
  const propertyItems = spec.store.properties;
  const spatialItems = spec.store.spatial;
  const operationalItems = spec.store.operational;
  const temporalItems = spec.store.temporal;
  /*% if (hasCategoricalDims) { %*/
  const categoryItems = spec.store.category;
  /*% } %*/
  const legendItems = spec.store.legend;

  // override the function logic to add property aggregation items
  return function getValuesFunction(propId, params, store) {
    switch (propId) {
      case "SPATIAL_AGGREGATION":
        return new Promise((resolve) => resolve(spatialItems));
      case "TEMPORAL_AGGREGATION":
        return new Promise((resolve) => resolve(temporalItems));
      /*% if (hasCategoricalDims) { %*/
      case "CATEGORY_AGGREGATION":
        return new Promise((resolve) => resolve(categoryItems));
      /*% } %*/
      case "OPERATIONAL_AGGREGATION":
        return new Promise((resolve) => resolve(operationalItems));
      case "PROPERTY_AGGREGATION":
        return new Promise((resolve) => resolve(propertyItems));
      case "YEAR_FILTER":
        return instantService.getYearItems(repo_url);
      case "SPATIAL_FILTER_TYPE":
        return new Promise((resolve) => resolve(spatialItems));
      case "SPATIAL_FILTER":
        return filterService.getSpatialFilterItems(params, 0);
      /*% if (hasCategoricalDims) { %*/
      case "CATEGORY_FILTER":
        return filterService.getCategoryFilterItems(
          params,
          repo_url,
          sensor_name,
          categoryItems,
          store
        );
      /*% } %*/
      case "PAGE_FILTER":
        return instantService.getPages();
      case "INSTANT_FILTER":
        return instantService.getInstants(
          !store ? params : store,
          repo_url,
          totalInstantsPages
        );
      case "INSTANT_SELECTOR":
        return instantService.getInstants(
          !store ? params : store,
          repo_url,
          totalInstantsPages,
          1440
        );
      case "LEGEND_TYPE":
        return new Promise((resolve) => resolve(legendItems));
    }
  };
};

export default {
  createGetValuesFunction,
  totalInstantsPages,
};
/*% } %*/
