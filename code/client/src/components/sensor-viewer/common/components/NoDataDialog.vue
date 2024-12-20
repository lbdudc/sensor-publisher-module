/*% if (feature.SensorViewer) { %*/
<template>
  <v-dialog v-model="dialogOpen" max-width="300px">
    <v-card>
      <v-card-title class="title">
        <span> {{ $t("map-views.no-data-dialog.title") }} </span>
      </v-card-title>
      <v-card-text class="pt-2">
        <span>
          {{ $t("map-views.no-data-dialog.content") }}
        </span>
      </v-card-text>
      <v-card-actions class="pt-0">
        <v-col class="text-right pt-0">
          <v-btn plain color="primary" @click="$emit('close')">
            {{ $t("map-views.no-data-dialog.actions.close") }}
          </v-btn>
        </v-col>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "NoDataDialog",
  props: {
    value: {
      required: true,
      type: Boolean,
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

<style scoped>
.title {
  background-color: rgba(25, 118, 210, 0.5);
}
</style>
/*% } %*/
