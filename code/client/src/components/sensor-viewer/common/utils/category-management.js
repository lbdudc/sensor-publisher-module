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
import {
  AGGREGATIONS,
  FILTERS,
} from "@/components/sensor-viewer/common/utils/const.js";

function calculateCategoryRangeValues(store, spec) {
  const calcCat = spec.store?.category.find(
    (cat) =>
      cat.value.toLowerCase() === store[AGGREGATIONS.CATEGORY].toLowerCase()
  );

  if (calcCat == null || calcCat.categories == null)
    return {
      categoryAggregation: store[AGGREGATIONS.CATEGORY],
      categoryFilter: store[FILTERS.CATEGORY],
    };

  const calcRange = calcCat.categories.find(
    (range) =>
      range.label.toLowerCase() == store[FILTERS.CATEGORY].toLowerCase()
  );

  if (calcRange == null)
    return {
      categoryAggregation: store[AGGREGATIONS.CATEGORY],
      categoryFilter: store[FILTERS.CATEGORY],
    };

  return {
    categoryFrom: calcRange.from,
    categoryTo: calcRange.to,
    categoryAggregation: store[AGGREGATIONS.CATEGORY],
    categoryFilter: "custom.ranged",
  };
}

export { calculateCategoryRangeValues };
/*% } %*/
/*% } %*/
