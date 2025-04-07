/*% if (feature.SensorViewer && feature.SV_FiltersBox) {
const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
return sensor.isMoving === true; }); %*/
<template>
  <v-navigation-drawer
    v-model="localDrawer"
    width="240"
    app
    clipped
    class="navigation-drawer"
  >
    <v-card dense id="filter-box" v-if="store">
      <v-card-title class="title">
        <v-row no-gutters>
          <router-link
            to="/"
            class="v-toolbar__title"
            custom
            v-slot="{ navigate }"
          >
            <h2 class="headline" @click="navigate"> /*%= normalize(data.basicData.name).toLowerCase() %*/ </h2>
          </router-link>
        </v-row>
      </v-card-title>
      <v-card-text class="card-text">
        <v-container class="pt-0">
          <v-row no-gutters align="center" class="mt-2" /*% if (hasMovingSensors) { %*/v-if="this.store.getSelector('MEASUREMENTS_FLAG')?.value == 1"/*% } %*/ >
            <v-icon class="section-title mr-2">public</v-icon>
            <h3 class="section-title">
              {{ $t("filter.spatial") }}
            </h3>
            <v-spacer></v-spacer>
            <v-btn
              icon
              class="section-title"
              @click="() => (showSpatialBox = !showSpatialBox)"
            >
              <v-icon v-if="showSpatialBox">expand_less</v-icon>
              <v-icon v-else>expand_more</v-icon>
            </v-btn>
            <v-col cols="12" v-show="showSpatialBox">
              <v-row no-gutters>
                /*% if (feature.SV_FB_SpatialDimension) { %*/
                <v-col cols="12">
                  <m-selector
                    id="SPATIAL_AGGREGATION"
                    /*% if (feature.SV_TB_RealTime) { %*/
                    :disabled="realTime"
                    :hideLoading="realTime"
                    /*% } %*/
                    append-icon="layers"
                    clearable
                    dense
                    :i18nLabel="this.$t"
                    :i18nItems="this.$t"
                    outlined
                    :store="store"
                  ></m-selector>
                </v-col>
                <v-col cols="12" class="item-negative-margin">
                  <m-selector
                    id="SPATIAL_FILTER_TYPE"
                    append-icon="filter_alt"
                    clearable
                    dense
                    /*% if (feature.SV_TB_RealTime) { %*/
                    :disabled="
                        !this.store.getSelector('SPATIAL_AGGREGATION').value ||
                        realTime
                      "
                    :hideLoading="realTime"
                    /*% } else { %*/
                    :disabled="
                      !this.store.getSelector('SPATIAL_AGGREGATION').value
                    "
                    /*% } %*/
                    :i18nLabel="this.$t"
                    :i18nItems="this.$t"
                    outlined
                    :store="store"
                  ></m-selector>
                </v-col>
                /*% if(hasMovingSensors){ %*/
                <v-col cols="12" class="item-negative-margin">
                  <m-autocomplete
                    v-if="this.showSpatialOperationSelector()"
                    id="SPATIAL_OPERATION"
                    append-icon="filter_alt"
                    clearable
                    dense
                    :i18nLabel="this.$t"
                    :i18nItems="this.$t"
                    outlined
                    :store="store"
                  ></m-autocomplete>
                </v-col>
                /*% } %*/
                <v-col cols="12" class="item-negative-margin">
                  <m-autocomplete
                    /*% if(hasMovingSensors) { %*/
                    v-if="this.showSpatialFilterIdSelector()"
                    /*% } else { %*/
                    v-if="!!this.store.getSelector('SPATIAL_FILTER_TYPE').value"
                    /*% } %*/
                    id="SPATIAL_FILTER"
                    append-icon="filter_alt"
                    clearable
                    dense
                    :i18nLabel="getSpatialFilterLabel"
                    outlined
                    :store="store"
                  ></m-autocomplete>
                </v-col>
                /*% } %*/
              </v-row>
              <!-- <v-row no-gutters>
                <v-col
                  class="text-center"
                  v-show="!this.store.getSelector('SPATIAL_AGGREGATION').value"
                >
                  <v-btn-toggle mandatory v-model="viewType">
                    <v-tooltip bottom>
                      <template v-slot:activator="{ on }">
                        <v-btn v-on="on">
                          <v-icon>mdi-road-variant</v-icon>
                        </v-btn>
                      </template>
                      {{ $t("filter.showSensorsSwitch.streets") }}
                    </v-tooltip>
                    <v-tooltip bottom>
                      <template v-slot:activator="{ on }">
                        <v-btn v-on="on">
                          <v-icon>mdi-access-point</v-icon>
                        </v-btn>
                      </template>
                      {{ $t("filter.showSensorsSwitch.sensors") }}
                    </v-tooltip>
                    <v-tooltip bottom>
                      <template v-slot:activator="{ on }">
                        <v-btn v-on="on">
                          <v-icon>mdi-grid</v-icon>
                        </v-btn>
                      </template>
                      {{ $t("filter.rasters") }}
                    </v-tooltip>
                  </v-btn-toggle>
                </v-col>
              </v-row> -->
            </v-col>
          </v-row>

          /*% if (feature.SV_FB_TemporalDimension) { %*/
          <v-row no-gutters align="center">
            <v-icon class="section-title mr-2">
              mdi-clock-time-four-outline
            </v-icon>
            <h3 class="section-title">{{ $t("filter.temporal") }}</h3>
            <v-spacer></v-spacer>
            <v-btn
              class="section-title"
              icon
              @click="() => (showTemporalBox = !showTemporalBox)"
            >
              <v-icon v-if="showTemporalBox">expand_less</v-icon>
              <v-icon v-else>expand_more</v-icon>
            </v-btn>
            <v-col cols="12" v-show="showTemporalBox">
              <v-row no-gutters class="mt-0">
                <v-col cols="12">
                  <m-selector
                    id="TEMPORAL_AGGREGATION"
                    /*% if (feature.SV_TB_RealTime) { %*/
                    :disabled="realTime"
                    :hideLoading="realTime"
                    /*% } %*/
                    append-icon="mdi-calendar-search"
                    dense
                    :i18nLabel="this.$t"
                    :i18nItems="this.$t"
                    outlined
                    :store="store"
                  ></m-selector>
                </v-col>
              </v-row>
              /*% if (hasMovingSensors){ %*/
              <div
                v-if="
                  this.store.getSelector('TEMPORAL_AGGREGATION').value ==
                  'RANGE'
                "
              >
                <v-row no-gutters align="center">
                  <m-date-filter
                    dense
                    type="date"
                    :store="store"
                    :i18n="this.$t"
                    :firstDayOfWeek="1"
                    id="INIT_DATE_FILTER"
                    :max-value="maxValue"
                    @change="setInitDate"
                  ></m-date-filter>
                </v-row>
                <v-row no-gutters align="center">
                  <m-date-filter
                    dense
                    type="date"
                    :store="store"
                    :i18n="this.$t"
                    :firstDayOfWeek="1"
                    id="END_DATE_FILTER"
                    :max-value="maxValue"
                    :min-value="minValue"
                  ></m-date-filter>
                </v-row>
              </div>
              /*% } %*/
            </v-col>
          </v-row>
          /*% } %*/
          <v-row no-gutters align="center" v-if="showCategoryBox">
            <v-icon class="section-title mr-2"> mdi-ruler </v-icon>
            <h3 class="section-title">{{ $t("filter.categories") }}</h3>
            <v-spacer></v-spacer>
            <v-btn
              class="section-title"
              icon
              @click="() => (showCategoryBox = !showCategoryBox)"
            >
              <v-icon v-if="showCategoryBox">expand_less</v-icon>
              <v-icon v-else>expand_more</v-icon>
            </v-btn>
            <v-col cols="12">
              <v-row no-gutters class="mt-0">
                <v-col cols="12">
                  <m-selector
                    id="CATEGORY_AGGREGATION"
                    /*% if (feature.SV_TB_RealTime) { %*/
                    :disabled="realTime"
                    :hideLoading="realTime"
                    /*% } %*/
                    dense
                    :i18nLabel="this.$t"
                    :i18nItems="this.$t"
                    outlined
                    :store="store"
                  ></m-selector>
                </v-col>
                <v-col cols="12" class="item-negative-margin">
                  <m-selector
                    id="CATEGORY_FILTER"
                    /*% if (feature.SV_TB_RealTime) { %*/
                    :disabled="
                        !this.store?.getSelector('CATEGORY_AGGREGATION')?.value ||
                        realTime
                      "
                    :hideLoading="realTime"
                    /*% } else { %*/
                    :disabled="
                      !this.store?.getSelector('CATEGORY_AGGREGATION')?.value
                    "
                    /*% } %*/
                    dense
                    :i18nLabel="this.$t"
                    outlined
                    clearable
                    :store="store"
                  ></m-selector>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
          /*% if(hasMovingSensors){ %*/
          <v-row no-gutters align="center">
            <v-icon class="section-title mr-2"> mdi-ferry </v-icon>
            <h3 class="section-title">{{ $t("filter.sensor") }}</h3>
            <v-spacer></v-spacer>
            <v-btn
              class="section-title"
              icon
              @click="() => (showSensorsBox = !showSensorsBox)"
            >
              <v-icon v-if="showSensorsBox">expand_less</v-icon>
              <v-icon v-else>expand_more</v-icon>
            </v-btn>
            <v-col cols="12" v-if="showSensorsBox">
              <v-row no-gutters class="mt-0">
                <v-col cols="12">
                  <m-selector
                    id="SENSOR_FILTER"
                    :hideLoading="realTime"
                    dense
                    :i18nLabel="this.$t"
                    outlined
                    :store="store"
                    clearable
                  ></m-selector>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
          /*% } %*/
          <v-row no-gutters align="center">
            <v-icon class="section-title mr-2">tune</v-icon>
            <h3 class="section-title">{{ $t("filter.others") }}</h3>
            <v-spacer></v-spacer>
            <v-btn
              class="section-title"
              icon
              @click="() => (showOtherBox = !showOtherBox)"
            >
              <v-icon v-if="showOtherBox">expand_less</v-icon>
              <v-icon v-else>expand_more</v-icon>
            </v-btn>
            <v-col cols="12" v-show="showOtherBox">
              <v-row no-gutters>
                <v-col cols="12">
                  <m-selector
                    id="PROPERTY_AGGREGATION"
                    /*% if (feature.SV_TB_RealTime) { %*/
                    :disabled="realTime"
                    :hideLoading="realTime"
                    /*% } %*/
                    append-icon="category"
                    dense
                    :i18nLabel="this.$t"
                    :i18nItems="this.$t"
                    outlined
                    :store="store"
                  ></m-selector>
                </v-col>

                <v-col cols="12" class="item-negative-margin">
                  <m-selector
                    v-if="
                      this.store.getSelector('TEMPORAL_AGGREGATION').value !==
                        'NONE' ||
                      !!this.store.getSelector('SPATIAL_AGGREGATION').value
                    "
                    id="OPERATIONAL_AGGREGATION"
                    /*% if (feature.SV_TB_RealTime) { %*/
                    :disabled="realTime"
                    :hideLoading="realTime"
                    /*% } %*/
                    append-icon="functions"
                    dense
                    :i18nLabel="this.$t"
                    :i18nItems="this.$t"
                    outlined
                    :store="store"
                  ></m-selector>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>

      <div class="hide-navigation-btn-container">
        <v-btn @click.stop="drawer = !drawer" class="hide-navigation-btn">
          <v-icon v-if="drawer">arrow_left</v-icon>
          <v-icon v-else>mdi-filter-cog-outline</v-icon>
        </v-btn>
      </div>
    </v-card>
  </v-navigation-drawer>
</template>

<script>
import { MSelector, MAutocomplete /*% if(hasMovingSensors){ %*/ , MDateFilter/*% } %*/ } from "magical-state/vue2-components";
import {
  FILTERS,
  FLAGS,
  RASTERS,
  TIME_INTERVALS,
} from "@/components/sensor-viewer/common/utils/const.js";
/*% if(hasMovingSensors){ %*/
import { formatDateNowTimeZone } from "@/components/sensor-viewer/common/utils/date-utils";
/*% } %*/

export default {
  name: "FiltersBox",
  components: { MSelector, MAutocomplete /*% if(hasMovingSensors){ %*/ , MDateFilter/*% } %*/ },
  props: {
    showSourceSelector: {
      type: Boolean,
      required: false,
      default: true,
    },
    store: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      showSpatialBox: true,
      showTemporalBox: true,
      showCategoryBox:
        this.store?.getSelector("CATEGORY_AGGREGATION")?.items != null &&
        this.store?.getSelector("CATEGORY_AGGREGATION")?.items.length > 0,
      showOtherBox: true,
      drawer: true,
      /*% if(hasMovingSensors){ %*/
      showSensorsBox: true,
      maxValue: formatDateNowTimeZone(),
      minValue: formatDateNowTimeZone(),
      /*% } %*/
    };
  },
  computed: {
    realTime() {
      return this.store.getSelector(FLAGS.REAL_TIME)?.value;
    },
    viewType: {
      get() {
        if (this.store.getSelector(FLAGS.RASTERS)?.value) {
          return RASTERS;
        } else {
          return this.store.getSelector(FLAGS.SENSORS)?.value;
        }
      },
      async set(newVal) {
        if (newVal === RASTERS) {
          this.$emit("show_rasters", true);
        } else {
          this.$emit("show_rasters", false);
          await this.store.change(FLAGS.SENSORS, newVal);
        }
      },
    },
    localDrawer: {
      get() {
        return this.drawer;
      },
      set(val) {
        this.drawer = val;
        this.$emit("drawer-changed", val);
      },
    },
  },
  methods: {
    getSpatialFilterLabel(label) {
      return this.$t(
        label + "." + this.store.getSelector(FILTERS.SPATIAL_TYPE).value
      );
    },
    changeZIndex() {
      const tempAggregation = this.store.getSelector(FILTERS.DATE);
      if (!!tempAggregation && tempAggregation.val === TIME_INTERVALS.NONE) {
        const zIndexFilter = document.getElementById("filter-box").style.zIndex,
          zIndexTimeLine = document.getElementById("timeline-box").style.zIndex;
        if (
          parseInt(zIndexTimeLine) > parseInt(zIndexFilter) ||
          zIndexFilter === "" ||
          zIndexTimeLine === ""
        ) {
          document.getElementById("filter-box").style.zIndex = 3;
          document.getElementById("timeline-box").style.zIndex = 2;
        }
      }
    },
    /*% if(hasMovingSensors){ %*/
    setInitDate() {
      this.minValue = this.store?.getSelector("INIT_DATE_FILTER").value;
    },
    showSpatialOperationSelector() {
      const agg = this.store.getSelector("SPATIAL_AGGREGATION").value;
      const filter = this.store.getSelector("SPATIAL_FILTER_TYPE").value;
      return agg != null && filter != null && agg !== filter;
    },
    showSpatialFilterIdSelector() {
      const { value: agg } = this.store.getSelector("SPATIAL_AGGREGATION");
      const { value: filter } = this.store.getSelector("SPATIAL_FILTER_TYPE");
      const { value: op } = this.store.getSelector("SPATIAL_OPERATION");
      return (
        agg != null &&
        filter != null &&
        (agg === filter ? op == null : op != null)
      );
    },
    /*% } %*/
  },
};
</script>

<style lang="css" scoped>
#filter-box {
  position: absolute;
  z-index: 3;
  max-width: 240px;
  cursor: default;
  min-width: 240px;
  height: 100vh !important;
  border-radius: 0px;
}

.navigation-drawer {
  overflow: visible;
  top: 0 !important;
}

.card-text {
  height: 90%;
  padding: 0;
  overflow-y: auto;
}

.clickable {
  cursor: pointer;
}

.title {
  background-color: #1781eb !important;
  color: white;
  padding: 0.5em;
  height: 66px;
}

.section-title {
  color: #1781eb !important;
}

.item-negative-margin {
  margin-top: -8px !important;
}

.hide-navigation-btn-container {
  visibility: visible;
  position: absolute;
  z-index: 0;
  display: block;
  top: calc(50% - 25px);
  left: 240px;
}

.hide-navigation-btn {
  border: 0;
  box-shadow: 5px 0 5px rgba(60, 64, 67, 0.3), 0 -5px 5px rgba(60, 64, 67, 0.15),
    0px 5px 5px rgba(60, 64, 67, 0.15);
  padding: 0 !important;
  width: 24px !important;
  height: 60px !important;
  min-width: 0 !important;
  cursor: pointer;
  border-left: 1px solid #dadce0;
  border-radius: 0 8px 8px 0;
  background: #fff 7px center/7px 10px no-repeat;
}

.title {
  background-color: #1781eb !important;
  color: white;
  padding: 0.5em;
}
</style>
/*% } %*/
