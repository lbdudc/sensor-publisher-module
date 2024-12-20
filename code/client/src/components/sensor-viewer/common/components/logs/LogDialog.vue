/*% if (feature.SensorViewer) { %*/
<template>
  <v-dialog v-model="dialogOpen" width="300px">
    <v-card>
      <v-card-title class="title">
        <span> {{ $t("filter.debug-log.log-dialog.title") }} </span>
      </v-card-title>
      <v-card-text class="pt-2">
        <div v-for="(log, idx) in logList" :key="idx">
          <log-item :log="log"></log-item>
        </div>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
import LogItem from "@/components/sensor-viewer/common/components/logs/LogItem.vue";

export default {
  name: "LogDialog",
  components: { LogItem },
  props: {
    value: {
      required: true,
      type: Boolean,
    },
    logList: {
      required: true,
      type: Array,
    },
  },
  computed: {
    dialogOpen: {
      get() {
        return this.value;
      },
      set() {
        this.$emit("close");
      },
    },
  },
};
</script>
<style lang="css" scoped>
.v-card {
  display: flex !important;
  flex-direction: column;
}
.v-card__text {
  flex-grow: 1;
  overflow: auto;
}
</style>
/*% } %*/
