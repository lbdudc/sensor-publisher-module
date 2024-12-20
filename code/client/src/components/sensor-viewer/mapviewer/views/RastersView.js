/*% if (feature.SensorViewer) { %*/
import maps from "@/components/sensor-viewer/common/config-files/maps.json";
import { getStyle } from "@/common/map-styles-common";
import styles from "@/components/sensor-viewer/common/config-files/styles.json";
import { calculateStartEnd } from "@/components/sensor-viewer/common/utils/instants-management.js";
import RepositoryFactory from "@/repositories/RepositoryFactory";
import RoadsView from "./RoadsView";
import SensorsView from "./SensorsView";
import {
  AGGREGATIONS,
  FLAGS,
  SAVE_RASTERS,
} from "@/components/sensor-viewer/common/utils/const.js";
import { StaticIntervalsLayerStyle } from "@lbdudc/map-viewer";

const KPIEntityRepository = RepositoryFactory.get("KPIEntityRepository");

export default class RastersView {
  constructor(map, store, route) {
    this.map = map;
    this.store = store;
    this.route = route;
    this.mapConfig = maps.maps.find((map) => map.name === this.route.params.id);
  }
  getFeatures() {
    const promises = [];
    this.map.getOverlays().forEach((layer) => {
      if (
        layer.getId() === this.store.getSelector(AGGREGATIONS.SPATIAL).value ||
        (this.store.getSelector(AGGREGATIONS.SPATIAL).value == null &&
          layer.getId() ===
          `raster_zoom_${this.store.getSelector(FLAGS.ZOOM).value}`)
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
    // Get selected KPI values for rasters
    const name = await this.store.getSelector(AGGREGATIONS.PROPERTY).value;
    const startEndDate = calculateStartEnd(this.store.objFromObservable);
    const op = await this.store.getSelector(AGGREGATIONS.OPERATIONAL).value;
    const zoom = await this.store.getSelector(FLAGS.ZOOM).value;
    const options = {
      signal: signal,
      params: {
        startDate: startEndDate.start,
        endDate: startEndDate.end,
        zoom: zoom,
        op: op,
      },
    };
    return await KPIEntityRepository.getKPIValue(name, options);
  }

  async setProps() {
    const kpis = await KPIEntityRepository.getKPIs();
    let differentItems = false;
    const actualItems = this.store.getSelector(AGGREGATIONS.PROPERTY).items;
    kpis.forEach((kpi) => {
      if (!actualItems.find((el) => el.value === kpi.value)) {
        differentItems = true;
      }
    });
    if (differentItems) {
      this.store.setItems(AGGREGATIONS.PROPERTY, kpis);
    }
  }

  async getMinMax(name) {
    return await KPIEntityRepository.getMinMax(
      name.toLowerCase(),
      this.store.getSelector(FLAGS.ZOOM).value
    );
  }

  async getDataFromItem(id) {
    const name = this.store.getSelector(AGGREGATIONS.PROPERTY).value;
    const startEndDate = calculateStartEnd(this.store.objFromObservable);
    const op = await this.store.getSelector(AGGREGATIONS.OPERATIONAL).value;
    const options = {
      params: {
        startDate: startEndDate.start,
        endDate: startEndDate.end,
        op: op,
      },
    };
    return await KPIEntityRepository.getRasterKPIValue(id, name, options);
  }
  getDataByProperty(items, property) {
    let data = [];
    data = Array.from(
      items.filter(
        (item) => parseInt(item.data[property.toLowerCase()]) !== -1
      ),
      (item) => parseFloat(item.data[property.toLowerCase()])
    );
    return data;
  }
  createCustomStaticStyle(intervals, property) {
    return new StaticIntervalsLayerStyle(
      "CustomStaticStyle",
      false,
      `data.${property.toLowerCase()}`,
      intervals,
      getStyle(styles, "grayPolygon")
    );
  }

  async getHistogramDataFromItem(id) {
    const name = this.store.getSelector(AGGREGATIONS.PROPERTY).value;
    const startEndDate = calculateStartEnd(this.store.objFromObservable);
    const op = await this.store.getSelector(AGGREGATIONS.OPERATIONAL).value;
    const temporalAggregation = await this.store.getSelector(
      AGGREGATIONS.TEMPORAL
    ).value;
    const zoom = await this.store.getSelector(FLAGS.ZOOM).value;
    const options = {
      params: {
        startDate: startEndDate.start,
        endDate: startEndDate.end,
        temporalAggregation: temporalAggregation,
      },
    };
    return await KPIEntityRepository.getHistogramDataFromItem(
      id,
      name,
      options
    );
  }

  change() {
    if (!!this.store.getSelector(AGGREGATIONS.SPATIAL).value) {
      this.store.setSelector(FLAGS.SAVE_RASTERS, true, false, false);
      this.store.setSelector(FLAGS.RASTERS, false, false, false);
      return new SensorsView(this.map, this.store, this.route);
    } else {
      if (
        this.store.getSelector(FLAGS.SENSORS).value &&
        !this.store.getSelector(FLAGS.RASTERS).value
      ) {
        this.store.setSelector(FLAGS.SAVE_RASTERS, false, false, false);
        this.store.setSelector(FLAGS.RASTERS, false, false, false);
        return new SensorsView(this.map, this.store, this.route);
      } else if (
        !this.store.getSelector(FLAGS.SENSORS).value &&
        !this.store.getSelector(FLAGS.RASTERS).value
      ) {
        this.store.setSelector(FLAGS.SAVE_RASTERS, false, false, false);
        this.store.setSelector(FLAGS.RASTERS, false, false, false);
        return new RoadsView(this.map, this.store, this.route);
      } else {
        return this;
      }
    }
  }
}
/*% } %*/
