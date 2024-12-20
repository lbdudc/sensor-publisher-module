/*% if (feature.SensorViewer) { %*/
<template>
  <div class="pa-0 ma-0">
    <v-dialog v-model="dialog" fullscreen transition="dialog-bottom-transition">
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          block
          color="primary"
          dark
          v-bind="attrs"
          v-on="on"
          :loading="!datasets"
        >
          {{ $t("histogram.label") }}
          <v-icon class="ml-1"> mdi-chart-histogram </v-icon>
        </v-btn>
      </template>
      <v-card flex class="pa-0 ma-0">
        <v-toolbar dark color="primary">
          <v-btn icon dark @click="dialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-toolbar-title v-if="title">{{ title }}</v-toolbar-title>
        </v-toolbar>

        <v-container>
          <v-form ref="form">
            <v-row
              no-gutters
              justify="space-around"
              align="center"
              class="mb-4 mt-6"
            >
              <v-col cols="6">
                <v-select
                  :label="this.$t('aggregation.property.label')"
                  v-model="propertyAggSelected"
                  :items="store.properties"
                  :item-text="(el) => this.$t(el.label)"
                  @change="calcChartData(datasets)"
                  outlined
                  dense
                >
                </v-select>
              </v-col>
              <v-col cols="6">
                <v-select
                  v-if="categories"
                  :label="this.$t('histogram.selector-label.category')"
                  v-model="chartFilterCategory"
                  :items="categories"
                  item-text="label"
                  multiple
                  clearable
                  outlined
                  dense
                  @change="changeCategory()"
                >
                </v-select>
                <v-select
                  v-else
                  :label="this.$t('histogram.selector-label.operation')"
                  v-model="chartFilterOperation"
                  :items="aggregationOperations"
                  item-text="value"
                  item-value="value"
                  multiple
                  clearable
                  outlined
                  dense
                  @change="changeOperation()"
                >
                </v-select>
              </v-col>

              <v-col cols="6">
                <v-select
                  :label="this.$t('histogram.selector-label.from')"
                  v-model="chartFilterFrom"
                  :items="chartLabelItems"
                  clearable
                  outlined
                  dense
                  prepend-inner-icon="mdi-calendar-arrow-right"
                  item-text="id"
                  item-value="value"
                  :rules="chartFilterFromRules"
                  @change="validateForm"
                  @click:clear="validateForm"
                ></v-select>
              </v-col>
              <v-col cols="6">
                <v-select
                  :label="this.$t('histogram.selector-label.to')"
                  v-model="chartFilterTo"
                  :items="chartLabelItems"
                  clearable
                  outlined
                  dense
                  prepend-inner-icon="mdi-calendar-arrow-left"
                  item-text="id"
                  item-value="value"
                  :rules="chartFilterToRules"
                  @change="validateForm"
                  @click:clear="validateForm"
                ></v-select>
              </v-col>
            </v-row>
          </v-form>
        </v-container>
        <v-container id="popup-container" v-if="propertyAggSelected">
          <div class="zoom-container" v-if="!histogramLoading">
            <div class="zoom-reset-btn">
              <v-btn icon @click="resetZoom">
                <v-icon>mdi-magnify-minus-outline</v-icon>
              </v-btn>
            </div>
            <v-row no-gutters>
              <v-col cols="12">
                <LineChartGenerator
                  class="chart-container"
                  ref="lineChart"
                  :chart-options="chartOptions"
                  :chart-data="histogramDatasets"
                  :chart-id="chartId"
                  :dataset-id-key="datasetIdKey"
                  :plugins="plugins"
                  :css-classes="cssClasses"
                  :styles="styles"
                  :width="width"
                  :height="height"
                />
              </v-col>
            </v-row>
          </div>
          <div v-else>
            <v-row no-gutters>
              <v-col cols="12" class="d-flex justify-center align-center">
                <v-progress-circular color="primary" indeterminate :size="45">
                </v-progress-circular>
              </v-col>
            </v-row>
          </div>
        </v-container>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import { Bar as LineChartGenerator } from "vue-chartjs/legacy";
import zoomPlugin from "chartjs-plugin-zoom";
import { TIME_INTERVALS } from "@/components/sensor-viewer/common/utils/const.js";

import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  ScatterController,
} from "chart.js";

ChartJS.register(
  Title,
  Tooltip,
  Legend,
  BarElement,
  LinearScale,
  CategoryScale,
  LineController,
  LineElement,
  PointElement,
  ScatterController,
  zoomPlugin
);

const CALC_COLOR = [
  "rgba(106, 76, 147, 1)",
  "rgba(25, 130, 196, 1)",
  "rgba(138, 201, 38, 1)",
  "rgba(255, 202, 58, 1)",
  "rgba(255, 89, 94, 1)",
];

export default {
  components: {
    LineChartGenerator,
  },
  props: {
    store: {
      type: Object,
      required: true,
    },
    layer: {
      type: Object,
      required: true,
    },
    categories: {
      type: Array,
      required: false,
    },
    title: {
      type: String,
      required: false,
      default: null,
    },
    chartId: {
      type: String,
      default: "lineChart",
    },
    chartData: {
      type: Object,
      default: () => ({}),
    },
    datasetIdKey: {
      type: String,
      default: "label",
    },
    width: {
      type: Number,
      default: 400,
    },
    height: {
      type: Number,
      default: 400,
    },
    cssClasses: {
      default: "",
      type: String,
    },
    styles: {
      type: Object,
      default: () => {},
    },
    plugins: {
      type: Object,
      default: () => {},
    },
    histogramGetData: {
      type: Function,
      required: false,
    },
    propertyUnits: {
      type: Object,
      required: true,
    },
    aggregationOperations: {
      type: Array,
      required: false,
    },
  },
  async mounted() {
    // Retrieve the histogram data making a request to the API
    this.calculateInitialFilterValues();
    await this.retrieveHistogramData(this.getHistogramLabel(), this.store);
    this.calcChartData(this.datasets);
    this.layer.updatePopup();
  },
  data() {
    return {
      labels: null,
      data: null,
      // Object with {id: "property", value: [arrayWithProps]}
      datasets: [],
      propertyAggSelected: null,

      // Operation filter
      chartFilterOperation: [],
      chartOperationItems: [],

      chartFilterDataset: [],
      chartFilterDatasetItems: [],

      // Chart Filters and Rules
      chartFilterFrom: null,

      chartFilterTo: null,
      dialog: false,
      chartFilterFromRules: [
        (v) => {
          if (this.chartFilterTo) {
            return v < this.chartFilterTo || "From must be < To";
          }
          return true;
        },
      ],
      chartFilterToRules: [
        (v) => {
          if (this.chartFilterFrom) {
            return v > this.chartFilterFrom || "To must be > From";
          }
          return true;
        },
      ],
      histogramDatasets: {},
      chartFilterCategory: [],
      histogramLoading: false,
    };
  },
  computed: {
    chartOptions() {
      return {
        responsive: "true",
        pan: {
          enabled: true,
          mode: "xy",
          modifierKey: "ctrl",
        },
        zoom: {
          mode: "xy",
          drag: {
            enabled: true,
            borderColor: "rgb(54, 162, 235)",
            borderWidth: 1,
            backgroundColor: "rgba(54, 162, 235, 0.3)",
          },
        },
        plugins: {
          legend: {
            position: "bottom",
            onClick: null,
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                return `${context.dataset.label}: ${
                  context.formattedValue
                } ${this.$t(this.propertyUnits[this.propertyAggSelected])}`;
              },
            },
          },
          zoom: {
            zoom: {
              mode: "x",
              drag: {
                enabled: true,
                borderColor: "rgb(54, 162, 235)",
                borderWidth: 1,
                backgroundColor: "rgba(54, 162, 235, 0.3)",
              },
            },
          },
        },
        animation: true,
        maintainAspectRatio: false,
        scales: {
          x: {
            ticks: {
              autoSkip: false,
            },
          },
          y: {
            ticks: {
              callback: (value, index, ticks) => {
                return (
                  value +
                  " " +
                  this.$t(this.propertyUnits[this.propertyAggSelected])
                );
              },
            },
          },
        },
      };
    },
    chartLabelItems() {
      let labels = [];
      this.datasets.forEach((dataset) => {
        dataset.data.forEach((data) => {
          //parseDateWithAggregation(date.id, this.store.temporalAggregation)
          labels.push(
            this.parseDateWithAggregation(
              data.id,
              this.store.temporalAggregation
            )
          );
        });
      });
      return Array.from(new Set(labels));
    },
  },
  methods: {
    async retrieveHistogramData(histId, options) {
      return this.histogramGetData(this.layer.feature.id, options).then(
        (res) => {
          this.datasets?.push({ id: histId, data: res });
        }
      );
    },
    resetZoom() {
      this.$refs.lineChart.getCurrentChart().resetZoom();
    },
    getChartDatasetsWithData(data) {
      return data.map((data, index) => {
        const color = CALC_COLOR[index % 5];
        return {
          ...data,
          backgroundColor: color || "rgba(58, 134, 255, 1)",
          pointBackgroundColor: color || "rgba(58, 134, 255, 1)",
          borderColor: color || "rgba(58, 134, 255, 1)",
        };
      });
    },
    parseDateWithAggregation(date, aggLevel) {
      let dateParsed = new Date(date);
      let year = dateParsed.getFullYear();
      let month = dateParsed.getMonth() + 1;
      let day = dateParsed.getDate().toString().padStart(2, "0");
      let hours = dateParsed.getHours().toString().padStart(2, "0");
      let minutes = dateParsed.getMinutes().toString().padStart(2, "0");

      switch (aggLevel) {
        case TIME_INTERVALS.DAY:
          return `${hours}:${minutes}`;
        case TIME_INTERVALS.MONTH:
          return `${day}/${month}`;
        case TIME_INTERVALS.YEAR:
          return `${month}/${year}`;
        default:
          return `${day}/${month}/${year}`;
      }
    },
    calculateInitialFilterValues() {
      this.propertyAggSelected = this.store.propertyAggregation;
      if (!this.categories) {
        this.chartFilterOperation = [this.store.calcAggregation];
        return;
      }
      let initialCat = [];
      if (this.store.categoryFrom && this.store.categoryTo) {
        initialCat.push(
          this.categories.find(
            (cat) =>
              cat.from == this.store.categoryFrom &&
              cat.to == this.store.categoryTo
          )
        );
      } else if (this.store.categoryFilter) {
        initialCat.push(
          this.categories.find((cat) => cat.value == this.store.categoryFilter)
        );
      }
      this.chartFilterCategory = initialCat;
    },
    validateForm() {
      if (this.$refs.form.validate()) {
        this.calcChartData(this.datasets);
      }
      this.resetZoom();
    },

    calculateDatasets(datasets) {
      let result = [];
      datasets.forEach((dataset) => {
        let filteredResults = [];
        let histData = {};
        histData.id = dataset.id;
        dataset.data.forEach((prop) => {
          let measurements = {};
          measurements.x = this.parseDateWithAggregation(
            prop.id,
            this.store.temporalAggregation
          );
          measurements.y = prop.data[this.propertyAggSelected.toLowerCase()];
          filteredResults.push(measurements);
        });
        histData.data = filteredResults;
        result.push(histData);
      });
      return result;
    },

    filterResultsByTime(measurements) {
      if (this.chartFilterFrom && this.chartFilterTo) {
        const fromIdx = this.chartLabelItems.indexOf(this.chartFilterFrom);
        const toIdx = this.chartLabelItems.indexOf(this.chartFilterTo);
        measurements.forEach((measurement) => {
          const filterData = measurement.data.slice(fromIdx, toIdx);
          measurement.data = filterData;
        });
      }
      return measurements;
    },

    calcChartData(datasets) {
      let calcDatasets = [];
      let measurementData = this.filterResultsByTime(
        this.calculateDatasets(datasets)
      );
      measurementData.forEach((meas) => {
        const foundDataset = calcDatasets.find(
          (dataset) => dataset.label === meas.id
        );
        const color = CALC_COLOR[5];
        if (foundDataset) {
          foundDataset.data.push(...meas.data);
          return;
        } else {
          calcDatasets.push({
            label: meas.id,
            type: meas.type || "line",
            backgroundColor: color || "rgba(58, 134, 255, 1)",
            pointBackgroundColor: color || "rgba(58, 134, 255, 1)",
            borderColor: color || "rgba(58, 134, 255, 1)",
            order: meas.order || 1,
            data: meas.data || [],
          });
        }
      });
      this.histogramDatasets = {
        datasets: this.getChartDatasetsWithData(calcDatasets),
        labels: this.getAllLabels(datasets),
      };
    },

    getAllLabels(data) {
      let dates = Array.from(
        new Set(data.flatMap((elem) => elem.data.map((e) => e.id)))
      );
      dates.sort((a, b) => new Date(a) - new Date(b));
      const start = dates[0];
      const end = dates[dates.length - 1];
      const allDates = this.getAllInstantsInBetween(
        start,
        end,
        this.store.temporalAggregation
      );
      const parsedDates = allDates.map((date) =>
        this.parseDateWithAggregation(date, this.store.temporalAggregation)
      );
      return parsedDates;
    },

    getAllInstantsInBetween(startDate, endDate, aggLevel) {
      const dates = [];
      const currentDate = new Date(startDate);
      while (currentDate <= new Date(endDate)) {
        const formattedDate =
          currentDate.getFullYear() +
          "-" +
          String(currentDate.getMonth() + 1).padStart(2, "0") +
          "-" +
          String(currentDate.getDate()).padStart(2, "0") +
          " " +
          String(currentDate.getHours()).padStart(2, "0") +
          ":" +
          String(currentDate.getMinutes()).padStart(2, "0") +
          ":" +
          String(currentDate.getSeconds()).padStart(2, "0") +
          "." +
          String(currentDate.getMilliseconds());
        dates.push(formattedDate);
        switch (aggLevel) {
          case TIME_INTERVALS.HOUR:
            currentDate.setMinutes(currentDate.getMinutes() + 1);
            break;
          case TIME_INTERVALS.DAY:
            currentDate.setHours(currentDate.getHours() + 1);
            break;
          case TIME_INTERVALS.MONTH:
            currentDate.setDate(currentDate.getDate() + 1);
            break;
          case TIME_INTERVALS.YEAR:
            currentDate.setMonth(currentDate.getMonth() + 1);
            break;
          default:
            currentDate;
        }
      }
      return dates;
    },

    async changeCategory() {
      this.histogramLoading = true;
      const selectedCategories = this.categories.filter((cat) =>
        this.chartFilterCategory.includes(cat.label)
      );
      this.datasets = [];
      for (let i = 0; i < selectedCategories.length; i++) {
        let cat = selectedCategories[i];
        let options = { ...this.store };
        if (options.categoryFrom) {
          //agg categórica con rango
          options.categoryFrom = cat.from;
          options.categoryTo = cat.to;
        } else {
          //agg categórica sin rango
          options.categoryFilter = cat.label;
        }
        await this.retrieveHistogramData(cat.label, options);
      }
      this.calcChartData(this.datasets);
      this.histogramLoading = false;
    },

    async changeOperation() {
      this.histogramLoading = true;
      this.datasets = [];
      for (let i = 0; i < this.chartFilterOperation.length; i++) {
        let op = this.chartFilterOperation[i];
        let options = { ...this.store };
        options.calcAggregation = op;
        await this.retrieveHistogramData(op, options);
      }
      this.calcChartData(this.datasets);
      this.histogramLoading = false;
    },

    getHistogramLabel() {
      if (!this.categories) return this.store.calcAggregation;
      let cat = this.store.categoryFrom
        ? this.categories.find(
            (cat) =>
              cat.from == this.store.categoryFrom &&
              cat.to == this.store.categoryTo
          )
        : this.categories.find((cat) => cat.value == this.store.categoryFilter);

      return cat ? cat.label : null;
    },
  },
};
</script>

<style lang="css">
#popup-container {
  min-width: 80vw;
  overflow: hidden;
}

.chart-container {
  max-height: 70vh;
}
.zoom-container {
  position: relative;
  width: 100%;
  height: 100%;
}
.zoom-reset-btn {
  position: absolute;
  top: 0;
  right: 0;
  margin: 0.5rem;
}
</style>
/*% } %*/
