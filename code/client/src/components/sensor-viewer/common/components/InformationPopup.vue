/*% if (feature.SensorViewer && feature.SV_Popup) { %*/

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

<template>
  <v-container v-if="layer && !loading" :class="'ma-0 pa-0'" fluid>
    <v-row class="px-3 mt-1">
      <v-row
        v-if="featureName"
        class="justify-center text-title mt-1"
        style="width: 100%"
      >
        <span style="white-space: nowrap"> Sensor: {{ featureName }} </span>
      </v-row>

      /*% if(feature.SV_P_SensorInfo) { %*/
      <v-row class="ml-0 mr-0" style="width: 100%">
        <v-tabs v-model="tab">
          <v-tab>{{ $t("map-views.popup.measurements") }}</v-tab>
          <v-tab>{{ $t("map-views.popup.info") }}</v-tab>
        </v-tabs>
        <v-tabs-items
          v-model="tab"
          class="justify-center mb-4"
          style="width: 100%"
        >
          <v-tab-item>
            <v-card flat>
              <v-card-text>
                <v-row class="mt-1 mb-1">
                  <v-simple-table
                    style="width: 100%"
                    v-if="parsedFeature.length"
                  >
                    <template v-slot:default>
                      <tbody>
                        <tr>
                          <td
                            v-for="prop in parsedFeature"
                            :key="prop.key"
                            style="text-align: center"
                          >
                            /*% const measurements = [];
                            data.dataWarehouse.sensors.forEach(function(sensor)
                            { const props = sensor.measureData;
                            props.forEach(prop => { if (!measurements.find(m =>
                            m.name === prop.name)) { measurements.push(prop); }
                            }); }); %*/ /*%
                            measurements.forEach(function(sensor) { %*/
                            <v-icon
                              color="primary"
                              v-if="
                                prop.key.toLowerCase() ==
                                '/*%= sensor.name.toLowerCase() %*/'
                              "
                            >
                              /*% if (sensor.icon) { %*/ mdi-/*%= sensor.icon
                              %*/ /*% } else { %*/ network_check /*% } %*/
                            </v-icon>
                            /*% }); %*/
                            <br />
                            <small style="white-space: nowrap">
                              <b>
                                {{
                                  $t(
                                    `aggregation.property.items.${prop.key.toLowerCase()}`
                                  )
                                }}
                              </b></small
                            >
                            <br />
                            {{
                              prop.value
                                | popupFormat(
                                  prop.key.toLowerCase(),
                                  propertyUnits
                                )
                            }}
                          </td>
                        </tr>
                      </tbody>
                    </template>
                  </v-simple-table>

                  <span v-else class="result mt-4">
                    {{ $t("map-views.legend.no-data") }}
                  </span>
                </v-row>

                /*% if (feature.SV_P_Histogram) { %*/
                <v-row class="mb-4">
                  <v-row
                    justify="center"
                    v-if="
                      parsedFeature.length &&
                      temporalAggSelected !== 'NONE' &&
                      store
                    "
                  >
                    <histogram
                      :layer="layer"
                      :store="store"
                      :histogramGetData="histogramGetData"
                      /*% if (hasCategoricalDims) { %*/
                      :categories="categories"
                      /*% } %*/
                      :propertyUnits="propertyUnits"
                      :aggregationOperations="getAggregationOperations()"
                      :title="$t('histogram.label') + ' ' + featureName"
                    ></histogram>
                  </v-row>
                </v-row>
                /*% } %*/
              </v-card-text>
            </v-card>
          </v-tab-item>
          <v-tab-item>
            <v-card flat>
              <v-card-text>
                <v-row class="mt-1 mb-1">
                  <v-simple-table style="width: 100%">
                    <template v-slot:default>
                      <tbody>
                        <tr
                          v-for="(value, key) in layer.feature.properties
                            .sensorInfo"
                          :key="key"
                          class="mt-1 mb-1 ml-1"
                        >
                          <td>
                            {{ $t("t_" + sensorId + "Entity.prop." + key) }}
                          </td>
                          <td>{{ value }}</td>
                        </tr>
                      </tbody>
                    </template>
                  </v-simple-table>
                </v-row>
              </v-card-text>
            </v-card>
          </v-tab-item>
        </v-tabs-items>
      </v-row>
      /*% } else { %*/
      <v-row class="width: 100% mt-5 mb-3 mr-0 ml-1">
        <v-simple-table style="width: 100%" v-if="parsedFeature.length">
          <template v-slot:default>
            <tbody>
              <tr>
                <td
                  v-for="prop in parsedFeature"
                  :key="prop.key"
                  style="text-align: center"
                >
                  /*% const measurements = [];
                  data.dataWarehouse.sensors.forEach(function(sensor) { const
                  props = sensor.measureData; props.forEach(prop => { if
                  (!measurements.find(m => m.name === prop.name)) {
                  measurements.push(prop); } }); }); %*/ /*%
                  measurements.forEach(function(sensor) { %*/
                  <v-icon
                    color="primary"
                    v-if="
                      prop.key.toLowerCase() ==
                      '/*%= sensor.name.toLowerCase() %*/'
                    "
                  >
                    /*% if (sensor.icon) { %*/ mdi-/*%= sensor.icon %*/ /*% }
                    else { %*/ network_check /*% } %*/
                  </v-icon>
                  /*% }); %*/
                  <br />
                  <small style="white-space: nowrap">
                    <b>
                      {{
                        $t(
                          `aggregation.property.items.${prop.key.toLowerCase()}`
                        )
                      }}
                    </b></small
                  >
                  <br />
                  {{
                    prop.value
                      | popupFormat(
                        prop.key.toLowerCase(),
                        propertyUnits
                      )
                  }}
                </td>
              </tr>
            </tbody>
          </template>
        </v-simple-table>

        <span v-else class="result mt-4">
          {{ $t("map-views.legend.no-data") }}
        </span>
      </v-row>

      <v-row class="mb-4">
        <v-row
          justify="center"
          v-if="parsedFeature.length && temporalAggSelected !== 'NONE' && store"
        >
          <histogram
            :layer="layer"
            :store="store"
            :histogramGetData="histogramGetData"
            /*% if (hasCategoricalDims) { %*/
            :categories="categories"
            /*% } %*/
            :propertyUnits="propertyUnits"
            :aggregationOperations="getAggregationOperations()"
            :title="$t('histogram.label') + ' ' + featureName"
          ></histogram>
        </v-row>
      </v-row>
      /*% } %*/
    </v-row>
  </v-container>
  <div v-else>
    <v-row justify="center">
      <v-progress-circular
        color="primary"
        indeterminate
        size="35"
      ></v-progress-circular>
    </v-row>
    <v-row justify="center">
      <td>{{ $t("map-views.loading") }}</td>
    </v-row>
  </div>
</template>

<script>
/*% if (feature.SV_P_Histogram) { %*/
import Histogram from "@/components/sensor-viewer/common/components/popup/Histogram.vue";
/*% } %*/
/*% if (hasCategoricalDims) { %*/
import categoryRepository from "@/repositories/components/CategoryProviderRepository.js";
/*% } %*/

export default {
  name: "InformationPopup",
  components: {
    /*% if (feature.SV_P_Histogram) { %*/
    Histogram,
    /*% } %*/
  },
  props: {
    form: {
      required: true,
    },
    layer: {
      required: true,
    },
    loading: {
      required: true,
    },
    propertySelected: {
      required: false,
    },
    propertyAggItems: {
      required: false,
      default: () => [],
    },
    temporalAggSelected: {
      required: false,
    },
    categoryFilterSelected: {
      required: true,
    },
    categoryAggSelected: {
      required: true,
    },
    sensorSpec: {
      type: Object,
      required: false,
      default: () => {},
    },
    store: {
      required: true,
      default: null,
    },
    histogramGetData: {
      type: Function,
      required: false,
      default: () => {},
    },
    propertyUnits: {
      type: Object,
      required: false,
      default: () => {},
    },
    /*% if (feature.SV_P_SensorInfo) { %*/
    sensorId: {
      required: true,
    },
    /*% } %*/
  },
  data() {
    return {
      /*% if (feature.SV_P_SensorInfo) { %*/
      tab: null,
      /*% } %*/
      categories: [],
    };
  },
  async created() {
    this.getAggregationOperations();
  },
  computed: {
    featureName() {
      if (
        this.layer?.feature?.properties?.name &&
        this.layer.feature.properties.name !== "null"
      ) {
        return this.layer.feature.properties.name;
      } else if (
        this.layer?.feature?.properties?.displayString &&
        this.layer.feature.properties.displayString !== "null"
      ) {
        return this.layer.feature.properties.displayString;
      }
      return "";
    },
    parsedFeature() {
      if (this.layer.feature.properties.data == null) return [];
      const result = Object.entries(this.layer.feature.properties.data).map(
        ([key, value]) => ({
          key,
          value,
        })
      );
      return result;
    },
  },
  watch: {
    layer() {
      if (this.layer) {
        this.layer
          .getPopup()
          .addOneTimeEventListener("remove", () => this.$emit("close"));
      }
    },
    /*% if (hasCategoricalDims) { %*/
    async loading() {
      if (!this.loading) {
        this.categories = await this.getCategories();
      }
    },
    /*% } %*/
  },
  updated() {
    if (this.layer) {
      this.customPopupUpdate();
      this.layer.updatePopup();
    }
  },
  methods: {
    customPopupUpdate() {
      if (this.layer._latlngs) {
        const geometry = this.layer.feature.geometry,
          latLngs = this.layer.getLatLngs();
        if (geometry.type === "Polygon" && latLngs[0].length === 4) {
          const lat =
              latLngs[0][0].lat + (latLngs[0][1].lat - latLngs[0][0].lat) / 2,
            lng =
              latLngs[0][1].lng + (latLngs[0][2].lng - latLngs[0][1].lng) / 2,
            newLatLng = new L.LatLng(lat, lng);
          this.layer._popup.setLatLng(newLatLng);
        }
      }
    },

    /*% if (hasCategoricalDims) { %*/
    async getCategories() {
      if (!this.sensorSpec?.store?.category) {
        return null;
      }
      const calcCat = this.sensorSpec?.store?.category.find(
        (cat) =>
          cat.value.toLowerCase() ===
          this.store["categoryAggregation"].toLowerCase()
      );
      if (calcCat.categories) {
        //agg categórica con rango
        return calcCat?.categories ? calcCat.categories : [];
      } else {
        //agg categórica simple
        const params = {
          repoUrl: this.sensorSpec.repository_url,
          sensorName: this.sensorSpec.id,
          category: this.store.categoryAggregation,
          date: this.store.start,
          aggregation: this.store.temporalAggregation,
        };
        const res = await categoryRepository.getItems({ params });
        return res.length > 0 ? res : [];
      }
    },
    /*% } %*/

    getAggregationOperations() {
      return this.sensorSpec?.store?.operational;
    },
  },
};
</script>

<style scoped>
.big-popup {
  width: auto;
  height: auto;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.small-popup {
  display: flex;
  align-items: center;
  justify-content: center;
}
.popup-header {
  display: grid;
}
.result {
  overflow: hidden;
  white-space: nowrap;
  width: 100%;
  font-size: 15px;
  /*% if (feature.SV_P_SensorInfo) { %*/
  text-align: center;
  /*% } %*/
}
.text-title {
  text-align: left;
  margin-top: 15px;
  font-weight: bold;
  font-size: 15px;
  display: inline-flex;
}
.table-header {
  font-weight: bolder;
  text-decoration: underline;
}
.v-application .px-3 {
  padding-right: 8px !important;
  padding-left: 12px !important;
}
/*% if (feature.SV_P_SensorInfo) { %*/
.v-tabs:not(.v-tabs--vertical) .v-tab {
  white-space: nowrap;
}
.v-tab {
  font-size: none;
  text-transform: none;
}
.v-card__subtitle,
.v-card__text,
.v-card__title {
  padding: 0px;
}
/*% } %*/
.leaflet-popup-content {
  width: auto !important;
}
</style>
/*% } %*/
