/*% if (feature.SensorViewer) { %*/
<template>
  <div id="layer-manager">
    /*% if (feature.SV_LayerManager) { %*/
    <layer-manager
      v-if="map"
      :layers="layers"
      :loadingMap="loadingMap"
      :map="map"
    ></layer-manager>
    /*% } %*/

    <div id="map-container">

      /*% if (feature.SV_FiltersBox) { %*/
      <!-- Aggregation filters box -->
      <filters-box
        v-if="store"
        :store="store"
        :showSourceSelector="canUseElastic"
        :logList="logList"
        @loading="loadingMap = true"
        @debug-log="onDebugChange"
        @show_rasters="displayPopulationGrid"
      ></filters-box>
      /*% } %*/

      <div ref="map" id="map"></div>

      <div v-if="loadingMap" id="loading-div">
        <v-col>
          <v-row justify="center">
            <v-progress-circular
              color="primary"
              indeterminate
              size="50"
            ></v-progress-circular>
          </v-row>
          <v-row class="mt-8" justify="center">
            <span>{{ $t("map-views.loading") }}</span>
          </v-row>
        </v-col>
      </div>

      <no-data-dialog
        v-model="noDataDialog"
        @close="noDataDialog = false"
      ></no-data-dialog>
      /*% if (feature.SV_Popup) { %*/
      <information-popup
        v-if="storeOptionsRequest"
        ref="informationPopup"
        :propertySelected="
          this.store?.getSelector('PROPERTY_AGGREGATION')?.value
        "
        :propertyAggItems="getHistogramPropertyAggItems()"
        :temporalAggSelected="
          this.store?.getSelector('TEMPORAL_AGGREGATION')?.value
        "
        :categoryAggSelected="
          this.store?.getSelector('CATEGORY_AGGREGATION')?.value
        "
        :categoryFilterSelected="
          this.store?.getSelector('CATEGORY_FILTER')?.value
        "
        :sensorSpec="this.spec"
        :form="popupForm"
        :layer="popupLayer"
        :loading="popupLoading"
        :store="storeOptionsRequest"
        :propertyUnits="propertyUnits"
        :histogramGetData="histogramGetDataFunc"
        @close="closePopup"
        /*% if (feature.SV_P_SensorInfo) { %*/
        :sensorId="sensorId"
        /*% } %*/
      ></information-popup>
      /*% } %*/
      /*% if (feature.SV_TimelineBox) { %*/
      <v-row no-gutters align="end">
        <v-col cols="12">
          <timeline-box
            v-if="store"
            :minValue="lastInstant"
            :store="store"
            :lastPage="totalInstantsPages.data"
            :is-map-loading="loadingMap"
            :is-instant-filter-in-url="isInstantFilterInUrl"
            :is-real-time-in-url="isRealTimeInUrl"
            @real-time-change="onRealTimeCheckboxChange"
            @avoid-map-loading="avoidLoadingMap"
          ></timeline-box>
        </v-col>
      </v-row>
      /*% } %*/
      /*% if (feature.SV_Legend) { %*/
      <interval-style-legend
        v-if="store"
        :items="styleIntervals"
        :loading="loadingLegend"
        :property="currentProperty"
        :store="store"
        :legendUnits="propertyUnits"
        :title="$t('map-views.legend.title')"
        @redraw="onLegendChange(true)"
      ></interval-style-legend>
      /*% } %*/
    </div>
  </div>
</template>

<script>
import RepositoryFactory from "@/repositories/RepositoryFactory";
import maps from "@/components/sensor-viewer/common/config-files/maps.json";
import layers from "@/components/sensor-viewer/common/config-files/layers.json";
import styles from "@/components/sensor-viewer/common/config-files/styles.json";
import { getStyle } from "@/components/sensor-viewer/common/utils/map-styles-common.js";
/*% if (feature.SV_LayerManager) { %*/
import LayerManager from "@/components/sensor-viewer/common/components/controls/LayerManager.vue";
/*% } %*/
import { filterResults } from "@/components/sensor-viewer/common/utils/maps-common-functions.js";
import {
  createMap,
  loadBaseLayers,
  loadOverlayLayers,
  reloadSensorsLayer,
} from "@/components/sensor-viewer/common/utils/map-layers-management";
import { createStore } from "magical-state";
import getSpec from "@/components/sensor-viewer/mapviewer/state/spec-utils.js";
import getValues from "@/components/sensor-viewer/mapviewer/state/getValues.js";
import defaulValuesGetter from "@/components/sensor-viewer/mapviewer/state/defaultValuesGetter.js";

/*% if (feature.SV_FiltersBox) { %*/
import FiltersBox from "@/components/sensor-viewer/common/components/controls/FiltersBox.vue";
/*% } %*/
/*% if (feature.SV_Popup) { %*/
import InformationPopup from "@/components/sensor-viewer/common/components/InformationPopup.vue";
/*% } %*/
import NoDataDialog from "@/components/sensor-viewer/common/components/NoDataDialog.vue";
/*% if (feature.SV_TimelineBox) { %*/
import TimelineBox from "@/components/sensor-viewer/common/components/TimelineBox.vue";
/*% } %*/
import instantService from "@/components/sensor-viewer/common/services/instantService";
/*% if (feature.SV_Legend) { %*/
import IntervalStyleLegend from "@/components/sensor-viewer/common/components/IntervalStyleLegend";
/*% } %*/
import { triggerInstantGetter } from "@/components/sensor-viewer/common/utils/instants-management.js";
import {
  sleep,
  loadUrlState,
  redirect,
  createOptionsForRequest,
} from "@/components/sensor-viewer/common/utils/maps-common-functions.js";
import {
  generateLegend,
  getIntervalStyles,
} from "@/components/sensor-viewer/common/utils/legend-utils.js";
import propertyUnits from "./common/propertyUnits.json";
// import SensorsView from "./views/SensorsView";
// import RastersView from "./views/RastersView";
// import RoadsView from "./views/RoadsView";
import {
  AGGREGATIONS,
  FILTERS,
  LEGEND,
  FLAGS,
} from "@/components/sensor-viewer/common/utils/const.js";

const minZoom = 12;

let controller = new AbortController();

export default {
  name: "Map",
  components: {
    NoDataDialog,
    /*% if (feature.SV_Popup) { %*/
    InformationPopup,
    /*% } %*/
    /*% if (feature.SV_FiltersBox) { %*/
    FiltersBox,
    /*% } %*/
    /*% if (feature.SV_TimelineBox) { %*/
    TimelineBox,
    /*% } %*/
    /*% if (feature.SV_LayerManager) { %*/
    LayerManager,
    /*% } %*/
    /*% if (feature.SV_Legend) { %*/
    IntervalStyleLegend,
    /*% } %*/
  },
  props: {
    spec: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      layers: layers.layers,
      requestTime: null,
      requestCount: null,
      loadingMap: true,
      loadingLegend: false,
      mapSelected: null,
      map: null,
      maps: [],
      noDataDialog: false,
      popupForm: null,
      popupLayer: null,
      popupLoading: true,
      store: null,
      styleIntervals: [],
      totalInstantsPages: getValues.totalInstantsPages,
      lastInstant: null,
      selectedSpatialAggregation: null,
      storeOptionsRequest: null,
      isInstantFilterInUrl: false,
      isRealTimeInUrl: false,
      minMax: {
        max: null,
        min: null,
      },
      propertyUnits: propertyUnits,
      debugLog: null,
      staticDataRepository: null,
      logList: [],
      canUseElastic: false,
      minMaxStatic: {
        max: null,
        min: null,
        elastic: null,
        property: null,
      },
      intervalId: null,
      avoidMapLoading: false,
      status: null,
      /*% if (feature.SV_P_SensorInfo) { %*/
      sensorId: null,
      /*% } %*/

    };
  },
  computed: {
    dataRepository() {
      return this.store
        ? RepositoryFactory.get(`${this.spec.repository}`)
        : null;
    },
    currentProperty() {
      return this.store?.getSelector(AGGREGATIONS.PROPERTY).value;
    },
    useElastic() {
      return this.canUseElastic && this.store?.getSelector(FLAGS.ELASTIC).value;
    },
    showRasters() {
      return this.store?.getSelector(FLAGS.RASTERS)?.value;
    },
  },
  watch: {
    $route(oldVal, newVal) {
      if (oldVal.params.id !== newVal.params.id) {
        const routeSelectedMap = this.maps.find(
          (map) => map.name === this.$route.params.id
        );
        if (routeSelectedMap) {
          this.mapSelected = routeSelectedMap.id;
          this.store = null;
          this.map.getLeafletMap().off("zoomstart", this.onZoomChange);
          this.loadMap();
        }
      }
    },
  },
  mounted() {
    this.maps = maps.maps.map((map, index) => {
      return {
        id: index,
        name: map.name,
        label: this.$t("mapViewer.map-label." + map.name.replace(".", "-")),
      };
    });

    this.mapSelected = this.maps.find(
      (map) => map.name === this.spec.mapInformation.defaultMap
    )?.id;

    if (this.mapSelected == null) {
      this.$notify({
        title: "ID not found",
        type: "error",
      });
      this.$router.push({ name: "NotFound" });
    }

    this.propertyUnits = this.getPropertyLegendUnits();

    this.loadMap();
  },
  methods: {
    async loadMap() {
      // Loads the map with the items, but without any information about the traffic
      if (this.map != null) {
        this.map.clearMap();
        this.map.getLeafletMap().off();
        this.map.getLeafletMap().remove();
      }
      this.map = createMap(this.mapSelected);
      this.store = null;
      /*% if (feature.SV_P_SensorInfo) { %*/
      this.sensorId = this.spec.id.charAt(0).toLowerCase() + this.spec.id.slice(1);
      /*% } %*/
      // Generate the getValues Function based on the spec
      const getValuesFunction = getValues.createGetValuesFunction(this.spec);

      this.store = await createStore(
        getSpec(),
        {
          getValues: getValuesFunction,
          defaultValuesGetter: defaulValuesGetter,
        },
        loadUrlState(
          getSpec(),
          this.$route,
          this.isRealTimeInUrl,
          this.isInstantFilterInUrl
        ),
        this.onFiltersChange
      );

      const repo_url = this.spec.repository_url;
      instantService.getLastInstant(repo_url).then((res) => {
        this.lastInstant = res;
      });

      loadBaseLayers(this.map, this.mapSelected);
      /*% if (feature.SV_Popup) { %*/
      loadOverlayLayers(
        this.map,
        this.mapSelected,
        this._geoJSONPopupFunction
      );
      /*% } else { %*/
      loadOverlayLayers(this.map, this.mapSelected, () => {});
      /*% } %*/

      // if real time is active we start the interval
      /*% if (feature.SV_TB_RealTime) { %*/
      if (this.store.objFromObservable[FLAGS.REAL_TIME]) {
        await this.onRealTimeCheckboxChange(true);
      } else {
        await this.onFiltersChange(this.store.objFromObservable);
      }
      /*% } else { %*/
      await this.onFiltersChange(this.store.objFromObservable);
      /*% } %*/
    },
    async onFiltersChange(store) {
      if (!this.avoidMapLoading) this.loadingMap = true;
      // We added sleep 0 to fix a bug with the popup's opening (it took too long to appear)
      await sleep(0);

      // Terminate previous request if pending
      controller.abort();

      // Reload sensor geometries if sensor type is dynamic
      if (this.spec?.isMoving) {
        await reloadSensorsLayer(
          this.spec.id.toLowerCase(),
          this.map,
          this.mapSelected,
          this._geoJSONPopupFunction
        );
      }

      const mapUpdatePromises = this._updateMap(store);
      const params = createOptionsForRequest(store, this.spec);
      params.properties = [store[AGGREGATIONS.PROPERTY]];

      // Enable cancel for request
      controller = new AbortController();
      const options = {
        signal: controller.signal,
      };

      if (store[FILTERS.SPATIAL] != null) {
        params["spatialFilter"] = store[FILTERS.SPATIAL_TYPE];
        params["spatialFilterId"] = store[FILTERS.SPATIAL];
      }

      return this.dataRepository
        .getData(params, options)
        .then((res) => {
          const data = Array.from(
            res.filter(
              (item) =>
                parseFloat(
                  item.data[
                    this.store.getSelector(AGGREGATIONS.PROPERTY)?.value.toLowerCase()
                  ]
                ) !== -1
            ),
            (item) =>
              parseFloat(
                item.data[this.store.getSelector(AGGREGATIONS.PROPERTY)?.value.toLowerCase()]
              )
          );
          this.minMax.min = Math.min(...data.filter((value) => value !== null && !isNaN(value)));
          this.minMax.max = Math.max(...data.filter((value) => value !== null && !isNaN(value)));
          Promise.all(mapUpdatePromises).then(
            async () => await this._updateMapInfo(res, store)
          );
        })
        .catch((err) => {
          if (err.message !== "ERR_CANCELED") throw err;
        });
    },
    _handleStatus(store) {
      if (store[FLAGS.RASTERS]) {
        return new RastersView(this.map, this.store, this.$route);
      } else if (store[FLAGS.SENSORS] != null && !store[FLAGS.SENSORS]) {
        return new RoadsView(this.map, this.store, this.$route);
      } else {
        return new SensorsView(this.map, this.store, this.$route);
      }
    },
    _updateMap(store) {
      if (this.selectedSpatialAggregation != store[AGGREGATIONS.SPATIAL]) {
        this.map.getLeafletMap().closePopup();
      }
      this.selectedSpatialAggregation = store[AGGREGATIONS.SPATIAL];
      if (this.store != null) {
        this.storeOptionsRequest = createOptionsForRequest(store, this.spec);
      }
      const promises = [];

      // Find layers affected by spatial aggregation
      const spatialAggr = store[AGGREGATIONS.SPATIAL];

      this.map.getOverlays().forEach((layer) => {
        if (spatialAggr) {
          if (spatialAggr === layer.getId()) {
            if (!layer.isSelected()) {
              promises.push(this.map.showLayer(layer));
            }
          } else if (layer.isSelected()) {
            promises.push(this.map.hideLayer(layer));
          }
        } else {
          if (layer.getId() !== this.spec.mapInformation.defaultLayer) {
            if (layer.isSelected()) {
              promises.push(this.map.hideLayer(layer));
            }
          } else if (!layer.isSelected()) {
            promises.push(this.map.showLayer(layer));
          }
        }
      });

      return promises;
    },
    _updateMapInfo(data, store) {
      this.styleIntervals = [];
      this.map
        .getVisibleOverlays()[0]
        .getLayer()
        .then(async (layer) => {
          const dataObj = {};
          data.forEach((item) => (dataObj[item.id] = item.data));
          layer.eachLayer((subLayer) => {
            const foundedFeatureProps = dataObj[subLayer.feature.id];
            if (foundedFeatureProps) {
              if (store[AGGREGATIONS.SPATIAL] != null) {
                subLayer._path.style.visibility = "visible";
              }
              subLayer.feature.properties.data = { ...foundedFeatureProps };
            } else {
              if (store[AGGREGATIONS.SPATIAL] != null) {
                subLayer._path.style.visibility = "hidden";
              }
              subLayer.feature.properties.data = null;
            }
          });

          await this.onLegendChange();
          if (this.popupLayer) {
            this._getFeatureData(this.popupLayer);
          }
        })
        .finally(() => (this.loadingMap = false));
    },
    /*% if (feature.SV_Popup) { %*/
    _geoJSONPopupFunction(form) {
      this.popupLoading = true;
      return (layer) => {
        this._getFeatureData(layer).then((res) => {
          layer.feature.properties.measurements = res.data.measurements;
          layer.feature.properties.sensorInfo = res.data.sensorInfo;
          this.popupForm = form;
          this.popupLayer = layer;
        }).finally(() => {
          this.popupLoading = false;
        });
        return this.$refs.informationPopup.$el;
      };
    },
    /*% } %*/
    _getFeatureData(layer) {
      this.popupLoading = true;
      const store = this.store.objFromObservable;
      const options = createOptionsForRequest(store, this.spec);
      const properties = this.spec.store.properties.map((elem) => elem.value);
      options.properties = properties;

      return this.dataRepository
        .getDataFromItem(layer.feature.id, options)
        .then((res) => {
          return res;
        })
        .finally(() => {
          this.popupLoading = false;
        });
    },
    closePopup() {
      this.popupForm = null;
      this.popupLayer = null;
    },
    onDebugChange(value) {
      this.debugLog = value;
      if (!value) this.logList = [this.logList.shift()];
    },
    /*% if (feature.SV_TB_RealTime) { %*/
    async onRealTimeCheckboxChange(val) {
      await this.store.setSelector(FLAGS.REAL_TIME, val, false, true);
      await this.onFiltersChange(this.store.objFromObservable);
      if (val) {
        if (this.intervalId == null) {
          //trae datos cada x segundos
          this.intervalId = setInterval(async () => {
            triggerInstantGetter(this.store);
          }, 1 * 1000);
        }
      } else {
        clearInterval(this.intervalId);
        this.intervalId = null;
      }
    },
    /*% } %*/
    async onLegendChange() {
      this.loadingLegend = true;
      const store = this.store.objFromObservable;
      const property = await this.store.getSelector(AGGREGATIONS.PROPERTY)
        ?.value;

      let subfix = !this.store.getSelector(AGGREGATIONS.SPATIAL)?.value
        ? "Point"
        : "Polygon";

      const intervals = getIntervalStyles(this.store, this.minMax, subfix);

      const query = { ms: this.store.exportStoreEncodedURL() };
      redirect(query, this.$router, this.$route);

      const { interv } = generateLegend(
        intervals,
        property,
        this.store,
        this.map,
        styles
      );

      // We set a different radius
      const newRadius = 3;
      const currentStyle = this.map.getVisibleOverlays()[0].getStyle();
      currentStyle._defaultStyle.setRadius(newRadius);
      currentStyle.intervals.forEach((interval) =>
        interval.style.setRadius(newRadius)
      );
      this.map.getVisibleOverlays()[0].setCustomStyle(currentStyle);

      this.styleIntervals = interv;
      this.loadingLegend = false;
    },
    histogramGetDataFunc(featureId, options) {
      options.properties = this.spec.store.properties.map((elem) => elem.value);
      return this.dataRepository.getHistogramDataFromItem(featureId, options);
    },
    getPropertyLegendUnits() {
      const obj = {};
      this.spec.store.properties.forEach((prop) => {
        obj[prop.value] = prop.units;
      });
      return obj;
    },
    async displayPopulationGrid(newVal) {
      if (this.store.getSelector(FLAGS.RASTERS).value !== newVal) {
        if (newVal) {
          this.store.change(FLAGS.RASTERS, newVal, false);
          this.store.change(LEGEND.TYPE, LEGEND.DYNAMIC, false);
        } else {
          this.store.change(FLAGS.RASTERS, newVal, false);
          this.store.change(FLAGS.DATA, "MedidaEntityElasticRepository", false);
        }
        this.onFiltersChange(this.store.objFromObservable);
      }
    },
    onZoomChange() {
      const mapZoom = this.map.getLeafletMap().getZoom();

      // We set a different radius depending on the zoom (the points are bigger in the closest zoom)
      const newRadius = mapZoom > 14 ? (mapZoom / 12) * 4 : 3;
      const currentStyle = this.map.getVisibleOverlays()[0].getStyle();
      currentStyle._defaultStyle.setRadius(newRadius);
      currentStyle.intervals.forEach((interval) =>
        interval.style.setRadius(newRadius)
      );
      this.map.getVisibleOverlays()[0].setCustomStyle(currentStyle);

      const level = Math.floor((mapZoom / 2) % 6) + 1;
      if (
        mapZoom >= minZoom &&
        level != this.store.getSelector(FLAGS.ZOOM)?.value
      ) {
        this.store.change(FLAGS.ZOOM, level, false);
        if (this.store.getSelector(FLAGS.RASTERS)?.value == true) {
          this.onFiltersChange(this.store.objFromObservable);
        }
      }
    },
    avoidLoadingMap(avoidLoading) {
      avoidLoading
        ? (this.avoidMapLoading = true)
        : (this.avoidMapLoading = false);
    },
    updateMinDate() {
      instantService
        .getLastInstant(this.$route.params.id, this.useElastic)
        .then((res) => {
          this.lastInstant = res;
        });
    },
    getHistogramPropertyAggItems() {
      return this.store
        ?.getSelector(AGGREGATIONS.PROPERTY)
        ?.items.filter((e) => e.value != "population");
    },
  },
};
</script>

<style lang="css" scoped>
#map-container {
  height: 100%;
}

#map {
  width: 100%;
  height: 100%;
  z-index: 1;
  top: 0px;
  position: fixed;
}

#loading-div {
  position: fixed;
  display: flex;
  align-items: center;
  justify-content: center;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.5);
  z-index: 1;
  user-select: none;
}

::v-deep .leaflet-top.leaflet-right {
  margin-top: 10px !important;
}

::v-deep .leaflet-top.leaflet-right .leaflet-control {
  margin-top: 0 !important;
}

::v-deep .leaflet-layer-manager-toggle {
  width: 30px !important;
  height: 30px !important;
}

::v-deep .leaflet-add-layer-control-icon {
  width: 30px !important;
  height: 30px !important;
}

::v-deep .leaflet-layer-manager-selector {
  vertical-align: text-top;
}

::v-deep .leaflet-layer-manager-style-selector {
  float: right;
  margin-right: 5px;
  margin-top: 3px;
}

::v-deep .leaflet-layer-manager-icon {
  max-height: 16px;
  position: relative;
  top: 4px;
}

::v-deep .leaflet-layer-manager-icon-square {
  display: inline-block;
  height: 13px;
  width: 16px;
  border: 2px solid white;
  border-right-width: 3px;
  border-bottom-width: 0px;
  margin-bottom: -1px;
  margin-top: 4px;
}

::v-deep .leaflet-layer-manager-button {
  float: right;
  margin-right: 5px;
  margin-top: 3px;
  background-repeat: no-repeat;
  background-size: 15px 15px;
  background-position: center;
  height: 16px;
  width: 10px;
}
</style>
/*% } %*/
