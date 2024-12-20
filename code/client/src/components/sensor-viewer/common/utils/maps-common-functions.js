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
import { calculateStartEnd } from "./instants-management.js";
/*% if (hasCategoricalDims) { %*/
import { calculateCategoryRangeValues } from "./category-management.js";
/*% } %*/
import { parseUrl } from "magical-state";
import {
  AGGREGATIONS,
  LEGEND,
  FILTERS,
  FLAGS,
  TIME_INTERVALS,
} from "@/components/sensor-viewer/common/utils/const.js";

function sleep(timeout) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve();
    }, timeout);
  });
}

function loadUrlState(spec, route, isRealTimeInUrl, isInstantFilterInUrl) {
  const urlState = parseUrl(route.query.ms, spec);
  if (Object.keys(urlState).length > 0) {
    if (
      urlState[AGGREGATIONS.TEMPORAL] === TIME_INTERVALS.YEAR ||
      urlState[AGGREGATIONS.TEMPORAL] === TIME_INTERVALS.MONTH
    ) {
      urlState[FILTERS.YEAR] = `${urlState[FILTERS.YEAR]}`;
    }
    if (urlState[FILTERS.INSTANT]) {
      isInstantFilterInUrl = true;
      urlState[FILTERS.INSTANT] = urlState[FILTERS.INSTANT]
        .split(",")
        .map((el) => parseInt(el));
    }
    if (!urlState[FILTERS.PAGE]) {
      urlState[FILTERS.PAGE] = 0;
    }
    if (urlState[FLAGS.REAL_TIME]) {
      isRealTimeInUrl = true;
    }
    if (urlState[FLAGS.ZOOM]) {
      urlState[FLAGS.ZOOM] = 1;
    }
    return urlState;
  } else {
    return null;
  }
}

/**
 * Added debouncer to solve redirection errors with the same URL
 */
let isRedirecting = false;
function redirect(query, router, route) {
  if (isRedirecting) return;
  isRedirecting = true;
  if (JSON.stringify(route.query) !== JSON.stringify(query)) {
    router.replace({
      path: route.path,
      query: query,
    });
  }
  setTimeout(() => {
    isRedirecting = false;
  }, 100);
}

function createOptionsForRequest(store, spec) {
  return {
    spatialAggregation: store[AGGREGATIONS.SPATIAL],
    temporalAggregation:
      store[AGGREGATIONS.TEMPORAL] !== TIME_INTERVALS.NONE
        ? store[AGGREGATIONS.TEMPORAL]
        : undefined,
    propertyAggregation: store[AGGREGATIONS.PROPERTY],
    calcAggregation: store[AGGREGATIONS.OPERATIONAL],
    spatialFilter:
      store[FILTERS.SPATIAL] != null ? store[FILTERS.SPATIAL_TYPE] : undefined,
    spatialFilterId:
      store[FILTERS.SPATIAL] != null ? store[FILTERS.SPATIAL] : undefined,
    sensorsData: store[FLAGS.SENSORS],
    zoom: store[FLAGS.ZOOM],
    /*% if (hasCategoricalDims) { %*/
    categoryAggregation: store[AGGREGATIONS.CATEGORY],
    categoryFilter: store[FILTERS.CATEGORY],
    ...calculateCategoryRangeValues(store, spec),
    /*% } %*/
    ...calculateStartEnd(store),
  };
}

function filterElasticResults(data, store) {
  const calc = store.getSelector(AGGREGATIONS.OPERATIONAL).value.toLowerCase();
  let resAsArray = Object.entries(data);
  let filteredRes = {};
  resAsArray = resAsArray.filter((res) => res[0].includes(calc));
  resAsArray.forEach((elem) => {
    filteredRes[elem[0].substring(0, elem[0].indexOf("_"))] = elem[1];
  });
  return filteredRes;
}

function filterResults(data, store, hasCategory) {
  if (hasCategory) {
    return data;
  } else {
    const calc = store.getSelector("OPERATIONAL_AGGREGATION").value.toLowerCase();
    let resAsArray = Object.entries(data);
    let filteredRes = {};
    resAsArray = resAsArray.filter((res) => res[0].includes(calc));
    resAsArray.forEach((elem) => {
      filteredRes[elem[0].substring(0, elem[0].indexOf("_"))] = elem[1];
    });
    return filteredRes;
  }
}

export {
  sleep,
  loadUrlState,
  redirect,
  createOptionsForRequest,
  filterElasticResults,
  filterResults,
};
/*% } %*/
