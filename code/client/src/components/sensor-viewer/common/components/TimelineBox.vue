/*% if (feature.SensorViewer && feature.SV_TimelineBox) { %*/
<template>
  <v-card dense class="card" id="timeline-box" v-if="store">
    <v-btn class="hide-btn" @click="hideBar()">
      <v-icon v-if="!showTimeLine">mdi-calendar-filter-outline</v-icon>
      <v-icon v-else>mdi-menu-down</v-icon>
    </v-btn>
    <v-card-text class="card-text" id="card-text-id">
      <v-row justify="start" align="center" class="timeline-row">
        /*% if (feature.SV_TB_RealTime) { %*/
        <v-col cols="2" md="1" :style="{ 'text-align': 'center' }">
          <div class="d-flex flex-column align-center">
            <label class="checkbox-label">{{
              $t("map-views.real-time-label")
            }}</label>
            <v-checkbox
              v-model="rTCheckbox"
              class="checkbox"
              @change="onRealTimeChange(rTCheckbox)"
            ></v-checkbox>
          </div>
        </v-col>
       /*% } %*/
        <v-col cols="5" md="2" v-if="temporalElement !== 'YEAR'">
          <m-selector
            v-if="temporalElement === 'MONTH'"
            /*% if (feature.SV_TB_RealTime) { %*/
            :disabled="loading || rTCheckbox"
            /*% } else { %*/
            :disabled="loading"
            /*% } %*/
            :i18nLabel="this.$t"
            :store="store"
            id="YEAR_FILTER"
            @change="setDateFilter"
          ></m-selector>
          <m-date-filter
            v-if="temporalElement === 'NONE' || temporalElement === 'DAY' || temporalElement === 'HOUR'"
            /*% if (feature.SV_TB_RealTime) { %*/
            :disabled="loading || rTCheckbox"
            :hideLoading="rTCheckbox"
            /*% } else { %*/
            :disabled="loading"
            /*% } %*/
            :min-value="minValue"
            :type="temporalElement === 'DAY' ? 'month' : 'date'"
            :store="store"
            :i18n="this.$t"
            :firstDayOfWeek="1"
            :max-value="maxValue"
            id="DATE_FILTER"
            @change="setDateFilter"
          ></m-date-filter>
        </v-col>

        <v-col cols="5" md="1">
          <m-selector
            superdense
            :disabled="loading"
            :i18nLabel="this.$t"
            :i18nItems="translate"
            :store="store"
            id="INSTANT_SELECTOR"
            @change="setCurrentInstant"
          >
          </m-selector>
        </v-col>

        <v-col cols="12" :md="temporalElement !== 'YEAR' ? 8 : 10">
          <m-timeline
            /*% if (feature.SV_TB_RealTime) { %*/
            :ignoreRenderCondition="rTCheckbox"
            /*% } %*/
            :i18n="translate"
            :store="store"
            id="INSTANT_FILTER"
            :disablePlayButton="disablePlayButton"
            :disableStopButton="disableStopButton"
            limitButtons
            :availableSpeeds="availableSpeeds"
            :isDisabled="loading"
            :currentMomentReached="currentMomentReached"
            @reproductionStarted="reproductionStarted"
            @reproductionStopped="reproductionStopped"
            @firstItemReached="goBackInTime"
            @lastItemReached="goFordward"
            @goToFirstItem="goToLimit('first')"
            @goToLastItem="goToLimit('last')"
            @change="change"
            @next="change"
            @prev="change"
            @timelineAdvanced="timelineAdvanced"
          >
          </m-timeline>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script>
import {
  MTimeline,
  MDateFilter,
  MSelector,
} from "magical-state/vue2-components";
import { compareAggregationDates } from "@/common/comparation-utils";
import PAGE_SIZE from "@/components/sensor-viewer/common/services/instantService";
import {
  AGGREGATIONS,
  FILTERS,
  TIME_INTERVALS,
  FLAGS,
} from "@/components/sensor-viewer/common/utils/const.js";
import {
  formatDateNowTimeZone,
  getNextDay,
  getNextMonth,
  getPrevDay,
  getPrevMonth,
} from "@/components/sensor-viewer/common/utils/date-utils";

export default {
  props: {
    store: {
      type: Object,
      required: true,
    },
    lastPage: {
      type: Number,
      required: true,
    },
    isMapLoading: {
      type: Boolean,
      required: false,
      default: false,
    },
    minValue: {
      type: String,
      required: false,
    },
    isInstantFilterInUrl: {
      type: Boolean,
      default: false,
    },
    isRealTimeInUrl: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    MTimeline,
    MDateFilter,
    MSelector,
  },
  data() {
    return {
      showTimeLine: true,
      loadingInterval: false,
      isPlaying: false,
      index: 4,
      rTCheckbox: this.store.getSelector(FLAGS.REAL_TIME)?.value,
      availableSpeeds: [
        {
          key: "1x",
          value: 1,
        },
        {
          key: "1.5x",
          value: 1.5,
        },
        {
          key: "2x",
          value: 2,
        },
        {
          key: "3x",
          value: 2,
        },
        {
          key: "5x",
          value: 5,
        },
      ],
      currentMomentReached: false,
      maxValue: formatDateNowTimeZone(),
    };
  },
  computed: {
    temporalElement() {
      return this.store.getSelector(AGGREGATIONS.TEMPORAL).value;
    },
    paginationElement() {
      return this.store.getSelector(FILTERS.PAGE);
    },
    instantsElement() {
      return this.store.getSelector(FILTERS.INSTANT);
    },
    loading() {
      return this.isMapLoading || this.loadingInterval;
    },
    disablePlayButton() {
      return (
        this.isPlaying ||
        this.instantsElement.items.length <= 0 ||
        this.loadingInterval
      );
    },
    disableStopButton() {
      return !this.isPlaying || this.instantsElement.items.length <= 0;
    },
  },
  async mounted() {
    if (this.isRealTimeInUrl) {
      this.rTCheckbox = true;
    }
    if (this.isInstantFilterInUrl && this.instantsElement.items.length > 0) {
      const itemsAsArrayOfStrings = [...this.instantsElement.items].map((el) =>
        [...el.value].toString()
      );

      const index = itemsAsArrayOfStrings.indexOf(
        [...this.store.getSelector(this.instantsElement.id).value].toString()
      );
      if (index >= 0) {
        await this.setInstant(this.instantsElement.items[index].value);
      } else {
        await this.setInstant(
          this.instantsElement.items[this.instantsElement.items.length - 1]
            .value
        );
      }
    } else if (this.instantsElement.items.length > 0) {
      await this.setInstant(
        this.instantsElement.items[this.instantsElement.items.length - 1].value
      );
    }
  },
  methods: {
    async onRealTimeChange(newVal) {
      this.rTCheckbox = newVal;
      this.loadingInterval = newVal;
      this.$emit("real-time-change", newVal);
    },

    async setDateFilter() {
      if (
        this.rTCheckbox &&
        !compareAggregationDates(
          new Date(),
          new Date(this.store.getSelector("DATE_FILTER").value),
          this.temporalElement
        )
      ) {
        this.onRealTimeChange(false);
      }
    },

    /**
     * Updates timeline with the 5 previous instants
     */
    async goBackInTime() {
      this.onRealTimeChange(false);
      this.loadingInterval = true;
      this.lastPage - 1 >= this.paginationElement.value + 1
        ? await this.retrievePrevInstants()
        : await this.changeTimeUnit(true);
      await this.resetInstantSelector();
      this.loadingInterval = false;
    },

    /**
     * Updates timeline with the 5 following instants
     */
    async goFordward() {
      this.currentMomentReached = false;
      this.loadingInterval = true;
      if (this.paginationElement.value !== 0) {
        await this.retrieveNextInstants();
      } else if (!this.rTCheckbox) {
        await this.changeTimeUnit(false);
      }
      if (
        !this.currentMomentReached &&
        this.store.getSelector(FILTERS.INSTANT).items[0]?.value
      ) {
        await this.setInstant(
          this.store.getSelector(FILTERS.INSTANT).items[0].value
        );
      }
      await this.resetInstantSelector();
      if (!this.isPlaying) this.loadingInterval = false;
    },

    /**
     * Updates timeline with the first/last 5 instants of the selected day, month or year
     */
    async goToLimit(moveTo) {
      this.loadingInterval = true;
      if (moveTo === "first") {
        this.onRealTimeChange(false);
        await this.setPagination(this.lastPage - 1);
        if (this.instantsElement.items[0])
          await this.setInstant(this.instantsElement.items[0].value);
        this.currentMomentReached = false;
      } else if (moveTo === "last") {
        await this.setPagination(0);
      }
      await this.updateInstantSelectorValues();
      this.loadingInterval = false;
    },

    async timelineAdvanced() {
      await this.resetInstantSelector();
    },

    translate(label, params) {
      return isNaN(parseInt(label)) ? this.$t(label, params) : label;
    },

    setInstant(newVal) {
      return this.store.setSelector(
        this.instantsElement.id,
        newVal,
        false,
        false
      );
    },

    setPagination(newVal) {
      return this.store.setSelector(
        this.paginationElement.id,
        newVal,
        false,
        false
      );
    },

    setDate(newVal) {
      return this.store.setSelector(FILTERS.DATE, newVal, false, false);
    },

    hideBar() {
      this.showTimeLine = !this.showTimeLine;
      if (!this.showTimeLine) {
        document.getElementById("card-text-id").classList.add("bar-hidden");
      } else {
        document.getElementById("card-text-id").classList.remove("bar-hidden");
      }
    },

    reproductionStarted() {
      this.isPlaying = true;
      this.currentMomentReached = false;
      this.loadingInterval = true;
      this.$emit("avoid-map-loading", true);
    },

    reproductionStopped() {
      this.isPlaying = false;
      this.currentMomentReached = false;
      this.loadingInterval = false;
      this.$emit("avoid-map-loading", false);
    },

    reachedCurrentMoment() {
      this.isPlaying = false;
      this.currentMomentReached = true;
    },

    async setCurrentInstant() {
      const reversedItems = [
        ...this.store.getSelector(FILTERS.INSTANT_SELECTOR).items,
      ]
        .reverse()
        .map((el) => el.value);

      const itemIndex = reversedItems.indexOf(
        this.store.getSelector(FILTERS.INSTANT_SELECTOR).value
      );
      const itemPage = Math.floor(itemIndex / PAGE_SIZE.PAGE_SIZE);
      await this.setPagination(itemPage);
      await this.setInstant(
        this.store.getSelector(FILTERS.INSTANT_SELECTOR).value
      );
      await this.change();
    },

    async change() {
      this.currentMomentReached = false;
      this.onRealTimeChange(false);
      await this.resetInstantSelector();
    },

    /**
     * Updates timeline with the following 5 instants of the same day, month or year
     */
    async retrieveNextInstants() {
      if (this.instantsElement.items.length) {
        await this.setPagination(this.paginationElement.value - 1);
        await this.setInstant(this.instantsElement.items[0].value);
      }
    },

    /**
     * Updates timeline with the previous 5 instants of the same day, month or year
     */
    async retrievePrevInstants() {
      await this.setPagination(this.paginationElement.value + 1);
      await this.setInstant(
        this.store.getSelector(FILTERS.INSTANT).items[
          this.store.getSelector(FILTERS.INSTANT).items.length - 1
        ].value
      );
    },

    /**
     * Resests instant selector value to be consistent with instant filter
     */
    async resetInstantSelector() {
      await this.store.change(
        FILTERS.INSTANT_SELECTOR,
        this.store.getSelector(FILTERS.INSTANT).value
      );
    },

    /**
     * Updates instants selector with new instans from instant filter
     */
    async updateInstantSelectorValues() {
      const instantSelectorObs = this.store.observable.find(
        (el) => el.id == FILTERS.INSTANT_SELECTOR
      );
      const existingLabels = instantSelectorObs.items.map((el) => el.label);
      this.store.getSelector(FILTERS.INSTANT).items.forEach((item) => {
        if (existingLabels.indexOf(item.label) == -1) {
          instantSelectorObs.items.push(item);
        }
      });
      await this.resetInstantSelector();
    },

    /**
     * Updates timeline whith data of previous/next day, month or year
     */
    async changeTimeUnit(goBackwards) {
      return goBackwards
        ? await this.goToPrevTimeUnit()
        : await this.goToNextTimeUnit();
    },

    /**
     * Updates timeline whith data of next day, month or year
     */
    async goToNextTimeUnit() {
      if (
        this.store.getSelector(AGGREGATIONS.TEMPORAL).value ==
        TIME_INTERVALS.DAY
      )
        return await this.resetSelectorsWithNextInstants(TIME_INTERVALS.MONTH);
      if (
        this.store.getSelector(AGGREGATIONS.TEMPORAL).value ==
        TIME_INTERVALS.MONTH
      )
        return await this.resetSelectorsWithNextInstants(TIME_INTERVALS.YEAR);
      if (
        this.store.getSelector(AGGREGATIONS.TEMPORAL).value ==
        TIME_INTERVALS.YEAR
      )
        return this.$notify(this.$t("map-views.no-more-data-notify"));
      return await this.resetSelectorsWithNextInstants(TIME_INTERVALS.DAY);
    },

    /**
     * Updates timeline whith data of previous day, month or year
     */
    async goToPrevTimeUnit() {
      if (
        this.store.getSelector(AGGREGATIONS.TEMPORAL).value ==
        TIME_INTERVALS.DAY
      )
        return await this.resetSelectorsWithPrevInstants(TIME_INTERVALS.MONTH);
      if (
        this.store.getSelector(AGGREGATIONS.TEMPORAL).value ==
        TIME_INTERVALS.MONTH
      )
        return await this.resetSelectorsWithPrevInstants(TIME_INTERVALS.YEAR);
      if (
        this.store.getSelector(AGGREGATIONS.TEMPORAL).value ==
        TIME_INTERVALS.YEAR
      )
        return this.$notify(this.$t("map-views.no-more-data-notify"));
      return await this.resetSelectorsWithPrevInstants(TIME_INTERVALS.DAY);
    },

    /**
     * Updates timeline selectors with data of
     * the next day, month or year
     */
    async resetSelectorsWithNextInstants(timeUnit) {
      const nextTimeUnit = this.getNextTimeUnit(timeUnit);
      if (new Date(nextTimeUnit) > new Date()) {
        this.$notify(this.$t("map-views.no-more-data-notify"));
        this.reachedCurrentMoment();
        return;
      }
      await this.setDate(nextTimeUnit);
      if (timeUnit == TIME_INTERVALS.YEAR) {
        await this.store.setSelector(FILTERS.YEAR, nextTimeUnit, false, false);
      }
      if (this.lastPage > 0) await this.setPagination(this.lastPage - 1);
      if (this.store.getSelector(FILTERS.INSTANT).items.length) {
        await this.setInstant(
          this.store.getSelector(FILTERS.INSTANT).items[0].value
        );
      }
    },

    /**
     * Updates timeline selectors with data of
     * the previous day, month or year
     */
    async resetSelectorsWithPrevInstants(timeUnit) {
      const prevTimeUnit = this.getPrevTimeUnit(timeUnit);
      if (timeUnit == TIME_INTERVALS.YEAR) {
        if (
          !this.store
            .getSelector(FILTERS.YEAR)
            .items.find((item) => item.value == prevTimeUnit)
        ) {
          this.$notify(this.$t("map-views.no-more-data-notify"));
          return;
        }
        await this.setDate(prevTimeUnit);
        await this.store.change(FILTERS.YEAR, prevTimeUnit);
      } else {
        await this.setDate(prevTimeUnit);
      }
      if (!this.store.getSelector(FILTERS.INSTANT).items.length) {
        this.$notify(this.$t("map-views.no-more-data-notify"));
      }
    },

    /**
     * Returns following day, month or year
     */
    getNextTimeUnit(timeUnit) {
      if (timeUnit == TIME_INTERVALS.DAY) {
        return getNextDay(this.store.getSelector(FILTERS.DATE).value);
      } else if (timeUnit == TIME_INTERVALS.MONTH) {
        return getNextMonth(this.store.getSelector(FILTERS.DATE).value);
      } else {
        return String(parseInt(this.store.getSelector(FILTERS.YEAR).value) + 1);
      }
    },

    /**
     * Returns previous day, month or year
     */
    getPrevTimeUnit(timeUnit) {
      if (timeUnit == TIME_INTERVALS.DAY) {
        return getPrevDay(this.store.getSelector(FILTERS.DATE).value);
      } else if (timeUnit == TIME_INTERVALS.MONTH) {
        return getPrevMonth(this.store.getSelector(FILTERS.DATE).value);
      } else {
        return String(parseInt(this.store.getSelector(FILTERS.YEAR).value) - 1);
      }
    },
  },
};
</script>

<style scoped>
.card {
  width: 100%;
  min-width: 400px;
  position: absolute;
  z-index: 2;
  bottom: 0;
  left: 0;
  right: 0;
  margin-top: 5px;
  margin-left: auto;
  margin-right: auto;
  background-color: transparent;
  -webkit-box-shadow: none !important;
}

.checkbox-label {
  color: rgba(0, 0, 0, 0.6);
  font-size: 1.2vh;
}

.checkbox {
  margin-top: 0 !important;
  padding: 0;
}

.hide-btn {
  z-index: 2;
  width: 60px !important;
  height: 24px !important;
  box-shadow: 0px -5px 5px rgba(60, 64, 67, 0.3),
    5px 0px 5px rgba(60, 64, 67, 0.3), -5px 0px 5px rgba(60, 64, 67, 0.3);
  border: 0px;
  border-radius: 8px 8px 0 0;
  left: 50%;
  overflow: hidden;
}

.card-text {
  background-color: white;
  box-shadow: 0 -5px 5px rgba(60, 64, 67, 0.15);
  transition: height 0.3s;
}

::v-deep .v-card__subtitle,
.v-card__text,
.v-card__title {
  padding: 0px;
}

.bar-hidden {
  height: 0px;
  overflow: hidden;
}

.timeline-row {
  margin: 0px;
  background-color: white;
}
</style>
/*% } %*/
