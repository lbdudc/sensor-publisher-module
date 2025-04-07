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
  const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
  return sensor.isMoving === true; });
%*/
import RepositoryFactory from "@/repositories/RepositoryFactory";
import {
  formatDateInverse,
  dateStringToDate,
  dateArrayToDate,
} from "@/common/conversion-utils";

async function getSpatialFilterItems(params, ccaa) {
  let res = [];
  const options = {
    params: { ccaa: ccaa },
  };

  if (params["SPATIAL_FILTER_TYPE"] == null) return [];

  const repoName =
    params["SPATIAL_FILTER_TYPE"].charAt(0).toUpperCase() +
    params["SPATIAL_FILTER_TYPE"].slice(1);

  const repository = RepositoryFactory.get(`${repoName}EntityRepository`);

  try {
    res = await repository.getAll(options);
  } catch (e) {
    return [];
  }

  return res.content.map((item) => ({
    value: item.id,
    label: item.name ? item.name : item.id,
  }));
}

/*% if (hasCategoricalDims) { %*/
async function getCategoryFilterItems(
  params,
  repo_url,
  sensor_name,
  categoryItems
) {
  let res = [];
  if (params["CATEGORY_AGGREGATION"] == null) {
    return [];
  }
  // Early return category Items if are defined in the spec
  const categoryItemsSpec = categoryItems?.find(
    (cat) =>
      cat.value.toLowerCase() === params["CATEGORY_AGGREGATION"].toLowerCase()
  );
  if (categoryItemsSpec && categoryItemsSpec.categories) {
    return categoryItemsSpec.categories.map((cat) => ({
      value: cat.label,
      label: cat.label,
    }));
  }
  // Build options for the request
  const options = {
    params: {
      repoUrl: repo_url,
      sensorName: sensor_name,
      category: params["CATEGORY_AGGREGATION"]
        ? params["CATEGORY_AGGREGATION"].toLowerCase()
        : null,
      sensorId: null,
    },
  };

  const repository = RepositoryFactory.get(`CategoryProviderRepository`);

  try {
    res = await repository.getItems(options);
  } catch (e) {
    return [];
  }
  return res;
}
/*% } %*/

function getDateFilterDefaultValue(store) {
  const date = !!store["INSTANT_FILTER"]
    ? formatDateInverse(dateArrayToDate(store["INSTANT_FILTER"]))
    : store["DATE_FILTER"];
  const aux = store["REAL_TIME_FLAG"] ? new Date() : dateStringToDate(date);
  switch (store["TEMPORAL_AGGREGATION"]) {
    case "NONE":
    case "HOUR":
      if (!date) {
        return getDefaultDate();
      } else {
        return formatDateInverse(aux);
      }
    case "DAY":
      if (!date) {
        return getDefaultMonth();
      } else {
        let dateAux = formatDateInverse(aux);
        dateAux = dateAux.split("-");
        dateAux = `${dateAux[0]}-${dateAux[1]}`;
        return dateAux;
      }
    case "MONTH":
      return `${aux.getFullYear()}`;
    case "YEAR":
      return `${aux.getFullYear()}`;
    default:
      return date;
  }
}

/*% if(hasMovingSensors){ %*/
function getRangeDateFilterDefaultValue(store) {
  return new Date();
}
/*% } %*/

function getYearFilterDefaultValue(store) {
  const date = !!store["INSTANT_FILTER"]
    ? `${dateArrayToDate(store["INSTANT_FILTER"]).getFullYear()}`
    : store["YEAR_FILTER"];
  if (!date) {
    return getDefaultYear();
  } else {
    return date;
  }
}

function getDefaultYear() {
  const today = new Date();
  return `${today.getFullYear()}`;
}

function getDefaultDate() {
  return formatDateInverse(new Date());
}

function getDefaultMonth() {
  const today = new Date();
  let mm = today.getMonth() + 1;
  if (mm < 10) mm = "0" + mm;
  return today.getFullYear() + "-" + mm;
}

/*% if(hasMovingSensors){ %*/
async function getSensorFilterItems() {
  const repository = RepositoryFactory.get(
    `ShipObservationEntityEntityRepository`
  );
  let result = [];
  try {
    const res = await repository.getAll();
    res.content.forEach((e) => {
      result.push({ label: e.name, value: e.id });
    });
  } catch (e) {
    return [];
  }
  return result;
}
/*% } %*/

export default {
  getSpatialFilterItems,
/*% if (hasCategoricalDims) { %*/
  getCategoryFilterItems,
/*% } %*/
  getDateFilterDefaultValue,
  getYearFilterDefaultValue,
  /*% if(hasMovingSensors){ %*/
  getRangeDateFilterDefaultValue,
  getSensorFilterItems,
/*% } %*/
};
/*% } %*/
