/*% const hasMovingSensors = data.dataWarehouse?.sensors?.find(function(sensor) {
return sensor.isMoving === true; });
if (feature.SensorViewer && feature.SV_Legend && hasMovingSensors) { %*/
<template>
  <div v-if="items.length && store">
    <v-btn class="legend-btn" small @click="showLegend = !showLegend">
      <v-icon>mdi-map-legend</v-icon>
      <span class="material-icons" v-if="!showLegend"> expand_more </span>
      <span class="material-icons" v-if="showLegend"> expand_less </span>
    </v-btn>
    <v-container v-if="showLegend" class="legend-box scrollable">
      <v-row no-gutters>
        <v-col v-if="loading" class="text-center">
          <v-progress-circular
            color="light-blue"
            height="10"
            indeterminate
            striped
          ></v-progress-circular>
          <span class="d-block">
            {{ $t("map-views.loading") }}
          </span>
        </v-col>
        <v-list class="legend-list" v-else-if="typeSelected !== 'CUSTOM'">
          <v-list-item v-for="(item, idx) in items" :key="idx" class="mt-n4">
            <template>
              <v-avatar
                class="rounded"
                :size="avatarSize"
                :color="getColor(item.style ? item.style : item)"
              >
              </v-avatar>
              <span
                v-if="item.maxValue != null && item.minValue != null"
                class="ml-2 legend-text"
                ><b>Sensor {{ formatValue(item.minValue) }}</b></span
              >
              <span
                v-if="item.maxValue == null && item.minValue == null"
                class="ml-2 legend-text"
              >
                {{ $t(`map-views.legend.no-data`) }}
              </span>
            </template>
          </v-list-item>
        </v-list>
      </v-row>
    </v-container>
  </div>
</template>

<script>
import {
  LEGEND,
  FLAGS,
} from "@/components/sensor-viewer/common/utils/const.js";
export default {
  name: "SensorLegend",
  props: {
    avatarSize: {
      type: String,
      default: "20px",
    },
    loading: {
      type: Boolean,
      default: false,
    },
    items: {
      required: false,
      type: Array,
    },
    property: {
      type: String,
      default: "",
    },
    store: {
      type: Object,
      default: null,
    },
    title: {
      type: String,
      default: "",
    },
    legendUnits: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      customItems: [],
      saved: true,
      validForm: true,
      showLegend: true,
    };
  },

  watch: {
    items(newVal) {
      if (this.typeSelected == LEGEND.CUSTOM) {
        this.customItems = newVal;
      }
    },
  },

  computed: {
    metricsUnit() {
      const str = !!this.legendUnits
        ? this.legendUnits[this.property]
        : this.property;
      return this.$t(str);
    },
    maxIsGreaterThanMin() {
      return () =>
        parseFloat(this.customItems[2].minValue) <
        parseFloat(this.customItems[2].maxValue);
    },
    typeSelected() {
      return this.store.getSelector(LEGEND.TYPE)?.value;
    },
    realTime() {
      return this.store.getSelector(FLAGS.REAL_TIME)?.value;
    },
  },
  methods: {
    getColor(item) {
      const opacity = item.fillOpacity > 0.7 ? item.fillOpacity : 0.7;
      const _opacity = Math.round(Math.min(Math.max(opacity || 1, 0), 1) * 255);
      return item.fillColor + _opacity.toString(16).toUpperCase();
    },
    onTypeChanged() {
      if (this.typeSelected === LEGEND.CUSTOM) {
        this.customItems = JSON.parse(JSON.stringify(this.items));
        this.customItems[3].maxValue = -1;
      }
      this.$emit("redraw");
    },
    async onSave() {
      this.saved = true;
      const setPromises = [
        this.store.setSelector(LEGEND.MIN, this.customItems[2].minValue),
        this.store.setSelector(LEGEND.MAX, this.customItems[2].maxValue),
        this.store.setSelector(LEGEND.TYPE, LEGEND.CUSTOM),
      ];
      await Promise.all(setPromises);
      this.$emit("redraw");
    },

    formatValue(val) {
      if (!val) return this.$n(val);
      const showRasters = this.store.getSelector(FLAGS.RASTERS)?.value;
      return this.$n(
        showRasters ? Number(val).toFixed(0) : Number(val).toFixed(2)
      );
    },
  },
};
</script>
<style lang="css" scoped>
.legend-box {
  position: fixed;
  background-color: white;
  right: 10px;
  top: 120px;
  z-index: 2;
  width: 205px;
  border: 2px solid;
  border-collapse: collapse;
  padding: 5px;
  border-color: #135e9b96;
}
.legend-title {
  color: #135e9b;
  font-size: 15px;
}

.legend-list {
  padding-bottom: 0;
  justify-content: center;
}
.legend-text {
  font-size: 13px;
}
.divider {
  opacity: 40%;
}
#info-icon {
  margin-top: 3px;
  padding-left: 2px;
}
.legend-btn {
  position: fixed;
  background-color: transparent;
  z-index: 2;
  right: 1.5vw;
  top: 10px;
  width: 40px;
  height: 30px;
  box-shadow: 0px -5px 5px rgba(60, 64, 67, 0.3),
    5px 0px 5px rgba(60, 64, 67, 0.3), -5px 0px 5px rgba(60, 64, 67, 0.3),
    0px 5px 5px rgba(60, 64, 67, 0.3);
}

.scrollable {
  max-height: 300px; /* Adjust height as needed */
  overflow-y: auto; /* Enables vertical scrolling */
}
</style>
/*% } %*/
