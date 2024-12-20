/*%@
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
        fileName: normalize(sen.id, true) + 'ItemsGetter.js',
        context: sen
      };
    });
%*/
import commonItems from "@/components/sensor-viewer/common/utils/commonItems.json";

const propertyItems = [
  /*% context.measureData.forEach(prop => { %*/
  {
    label: "aggregation.property.items./*%= prop.name.toLowerCase() %*/",
    value: "/*%= prop.name %*/"
  },
  /*% }); %*/
];

async function getSpatialItems() {
  const f = () => {
    return new Promise((resolve) => {
      resolve(commonItems./*%= normalize(context.id, false) %*/SpatialItems);
    });
  };
  return await f();
}

async function getTemporalItems() {
  const f = () => {
    return new Promise((resolve) => {
      resolve(commonItems.temporalItems);
    });
  };
  return await f();
}

async function getOperationalItems() {
  const f = () => {
    return new Promise((resolve) => {
      resolve(commonItems.operationalItems);
    });
  };
  return await f();
}

async function getPropertyItems() {
  const f = () => {
    return new Promise((resolve) => {
      resolve(propertyItems);
    });
  };
  return await f();
}

async function getLegendTypes() {
  const f = () => {
    return new Promise((resolve) => {
      resolve(commonItems.legendTypes);
    });
  };
  return await f();
}

export default {
  getSpatialItems,
  getTemporalItems,
  getOperationalItems,
  getPropertyItems,
  getLegendTypes,
};
