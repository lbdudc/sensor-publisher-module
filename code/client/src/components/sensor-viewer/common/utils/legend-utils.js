/*% if (feature.SensorViewer) { %*/
import { getStyle } from "@/components/sensor-viewer/common/utils/map-styles-common";
import { getIntervals } from "@/components/sensor-viewer/common/utils/interval-utils.js";
import { StaticIntervalsLayerStyle } from "@lbdudc/map-viewer";

/**
 * Returns the intervals for the legend
 * @param {*} store
 * @param {*} minMax
 * @param {*} subfix
 * @returns
 */
export function getIntervalStyles(store, minMax, subfix) {
  const storeObs = store;

  let intervals = [
    { minValue: null, maxValue: null, style: "green" + subfix },
    { minValue: null, maxValue: null, style: "orange" + subfix },
    { minValue: null, maxValue: null, style: "red" + subfix },
  ];
  switch (storeObs.getSelector("LEGEND_TYPE")?.value) {
    case "STATIC":
      break;
    case "DYNAMIC":
      getIntervals(minMax.min, minMax.max, intervals);
      break;
    case "CUSTOM":
      if (
        !storeObs.getSelector("MAX").value ||
        !storeObs.getSelector("MIN").value
      ) {
        break;
      }
      getIntervals(minMax.min, minMax.max, intervals);

      intervals[0].minValue = 0;
      intervals[0].maxValue = parseFloat(storeObs.getSelector("MIN")?.value);
      intervals[1].minValue = intervals[0].maxValue;
      intervals[1].maxValue = parseFloat(storeObs.getSelector("MAX")?.value);
      intervals[2].minValue = intervals[1].maxValue;
      break;
  }
  return intervals;
}

/**
 * Generates the legend for the map
 * @param {*} intervals
 * @param {*} property
 * @param {*} store
 * @param {*} map
 * @param {*} styles
 * @returns
 */
export function generateLegend(intervals, property, store, map, styles) {
  const legendType = store.getSelector("LEGEND_TYPE")?.value;
  const isPolygon = store.objFromObservable["SPATIAL_AGGREGATION"] != null;
  const changedCustom =
    !!store.getSelector("MIN").value && !!store.getSelector("MAX").value;

  let styleId = isPolygon
    ? store.objFromObservable["PROPERTY_AGGREGATION"] + "_POLYGON"
    : store.objFromObservable["PROPERTY_AGGREGATION"];

  if (legendType === "STATIC") {
    map.getVisibleOverlays()[0].setStyle(styleId);
  } else {
    if (!changedCustom && legendType === "CUSTOM") {
      const predefinedIntervals = styles.styles.find(
        (s) => s.name === styleId
      ).intervals;
      intervals.forEach((interval, index) => {
        interval.minValue = predefinedIntervals[index].minValue;
        interval.maxValue = predefinedIntervals[index].maxValue;
      });
    }
    intervals.forEach((interval) => {
      interval.style = getStyle(styles, interval.style);
    });
    const style = new StaticIntervalsLayerStyle(
      "CustomStaticStyle",
      false,
      "data." + property.toLowerCase(),
      intervals,
      getStyle(styles, isPolygon ? "grayPolygon" : "grayPoint")
    );
    map.getVisibleOverlays()[0].setCustomStyle(style);
  }

  const ovStyle = map.getVisibleOverlays()[0].getStyle();
  const aux = [ovStyle.getDefaultStyle()];
  const styleIntervals = aux.concat(ovStyle.getIntervals());

  return { interv: styleIntervals };
}
/*% } %*/
