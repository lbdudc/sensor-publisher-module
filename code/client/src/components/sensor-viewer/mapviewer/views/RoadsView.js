/*% if (feature.SensorViewer) { %*/
import { createOptionsForRequest } from "@/components/sensor-viewer/common/utils/maps-common-functions.js";
import { getStyle } from "@/common/map-styles-common";
import styles from "@/components/sensor-viewer/common/config-files/styles.json";
import madridItemsGetter from "@/components/sensor-viewer/common/services/madridItemsGetter.js";
import RepositoryFactory from "@/repositories/RepositoryFactory";
import RastersView from "./RastersView";
import SensorsView from "./SensorsView";
import {
  AGGREGATIONS,
  FLAGS,
} from "@/components/sensor-viewer/common/utils/const.js";
import { StaticIntervalsLayerStyle } from "@lbdudc/map-viewer";

const MedidaEntityElasticRepository = RepositoryFactory.get(
  "MedidaEntityElasticRepository"
);
const MedidaEntityRepository = RepositoryFactory.get("MedidaEntityRepository");

export default class RoadsView {
  constructor(map, store, route) {
    this.map = map;
    this.store = store;
    this.route = route;
    this.mapConfig = "osmSections";
  }
  getFeatures() {
    const promises = [];
    this.map.getOverlays().forEach((layer) => {
      if (
        layer.getId() === this.store.getSelector(AGGREGATIONS.SPATIAL).value ||
        (this.store.getSelector(AGGREGATIONS.SPATIAL).value == null &&
          layer.getId() === this.map.getLayer(this.mapConfig)?.getId())
      ) {
        if (!layer.isSelected()) {
          promises.push(this.map.showLayer(layer));
        }
      } else {
        if (layer.isSelected()) {
          promises.push(this.map.hideLayer(layer));
        }
      }
    });
    return promises;
  }

  async getValues(signal) {
    const repository = this.store.getSelector(FLAGS.ELASTIC).value
      ? MedidaEntityElasticRepository
      : MedidaEntityRepository;
    const params = createOptionsForRequest(this.store.objFromObservable, false);
    params.properties = [this.store.getSelector(AGGREGATIONS.PROPERTY).value];
    const options = {
      signal: signal,
    };
    return await repository.getData(params, options);
  }

  async setProps() {
    const props = await madridItemsGetter.getPropertyItems();
    let differentItems = false;
    const actualItems = this.store.getSelector(AGGREGATIONS.PROPERTY).items;
    props.forEach((prop) => {
      if (!actualItems.find((el) => el.value === prop.value)) {
        differentItems = true;
      }
    });
    if (differentItems) {
      this.store.setItems(AGGREGATIONS.PROPERTY, props);
    }
  }

  async getMinMax(property) {
    const repository = this.store.getSelector(FLAGS.ELASTIC).value
      ? MedidaEntityElasticRepository
      : MedidaEntityRepository;
    return await repository.getMinMax(property);
  }

  async getDataFromItem(id) {
    const repository = this.store.getSelector(FLAGS.ELASTIC).value
      ? MedidaEntityElasticRepository
      : MedidaEntityRepository;
    const params = createOptionsForRequest(this.store.objFromObservable, false);
    return await repository.getDataFromItem(id, params);
  }

  getDataByProperty(items, property) {
    let data = [];
    data = Array.from(
      items.filter((item) => parseInt(item.data[property]) !== -1),
      (item) => parseFloat(item.data[property])
    );
    return data;
  }

  createCustomStaticStyle(intervals, property) {
    return new StaticIntervalsLayerStyle(
      "CustomStaticStyle",
      false,
      `data.${property}`,
      intervals,
      getStyle(styles, "grayPoint")
    );
  }

  async getHistogramDataFromItem(id) {
    const repository = this.store.getSelector(FLAGS.ELASTIC).value
      ? MedidaEntityElasticRepository
      : MedidaEntityRepository;
    const params = createOptionsForRequest(this.store.objFromObservable, false);
    return await repository.getHistogramDataFromItem(id, params);
  }

  change() {
    if (
      this.store.getSelector(FLAGS.SAVE_RASTERS).value &&
      !this.store.getSelector(AGGREGATIONS.SPATIAL).value
    ) {
      this.store.setSelector(FLAGS.RASTERS, true, false, false);
      this.store.setSelector(FLAGS.SAVE_RASTERS, false, false, false);
      return new RastersView(this.map, this.store, this.route);
    } else if (this.store.getSelector(FLAGS.RASTERS).value) {
      return new RastersView(this.map, this.store, this.route);
    } else if (
      this.store.getSelector(FLAGS.SENSORS).value != null &&
      this.store.getSelector(FLAGS.SENSORS).value
    ) {
      return new SensorsView(this.map, this.store, this.route);
    } else {
      return this;
    }
  }
}
/*% } %*/
