/*% if (feature.SensorViewer) { %*/
const AGGREGATIONS = {
  TEMPORAL: "TEMPORAL_AGGREGATION",
  SPATIAL: "SPATIAL_AGGREGATION",
  OPERATIONAL: "OPERATIONAL_AGGREGATION",
  PROPERTY: "PROPERTY_AGGREGATION",
  CATEGORY: "CATEGORY_AGGREGATION",
};

const TIME_INTERVALS = {
  DAY: "DAY",
  MONTH: "MONTH",
  YEAR: "YEAR",
  NONE: "NONE",
  HOUR: "HOUR",
  WEEK: "WEEK",
};

const FILTERS = {
  SPATIAL: "SPATIAL_FILTER",
  SPATIAL_TYPE: "SPATIAL_FILTER_TYPE",
  YEAR: "YEAR_FILTER",
  DATE: "DATE_FILTER",
  PAGE: "PAGE_FILTER",
  INSTANT: "INSTANT_FILTER",
  INSTANT_SELECTOR: "INSTANT_SELECTOR",
  CATEGORY: "CATEGORY_FILTER",
};

const FLAGS = {
  ELASTIC: "USE_ELASTIC",
  REAL_TIME: "REAL_TIME_FLAG",
  SENSORS: "SHOW_SENSORS",
  VORONOI: "SHOW_VORONOI",
  RASTERS: "SHOW_RASTERS",
  SAVE_RASTERS: "SAVE_RASTERS_VALUE",
  ZOOM: "ZOOM_LEVEL",
  DATA: "DATA_REPOSITORY",
};

const LEGEND = {
  TYPE: "LEGEND_TYPE",
  STATIC: "STATIC",
  DYNAMIC: "DYNAMIC",
  CUSTOM: "CUSTOM",
  MIN: "MIN",
  MAX: "MAX",
};

const ROADS = 0;
const SENSORS = 1;
const RASTERS = 2;

export {
  AGGREGATIONS,
  TIME_INTERVALS,
  FILTERS,
  FLAGS,
  LEGEND,
  ROADS,
  SENSORS,
  RASTERS,
};
/*% } %*/