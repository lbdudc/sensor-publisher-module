/*% if (feature.SensorViewer) { %*/
<template>
  <v-alert class="clickable" dense text type="info" @click="$emit('click')">
    <template #prepend>
      <div></div>
    </template>
    <v-row class="text-center" dense>
      <v-col>
        <v-tooltip bottom>
          <template v-slot:activator="{ on, attrs }">
            <v-icon v-bind="attrs" v-on="on" color="primary" small>
              info
            </v-icon>
            {{
              log?.title
                ? $t("filter.useElasticSwitch.elastic")
                : $t("filter.useElasticSwitch.postgres")
            }}
          </template>
          <div v-html="$t('map-views.debug.notification.tooltip')"></div>
        </v-tooltip>
      </v-col>
    </v-row>
    <v-row no-gutters>
      <v-col class="text-left">
        {{
          $t("map-views.debug.notification.text1", {
            time: log?.time,
          })
        }}
        <br />
        {{
          $t("map-views.debug.notification.text2", {
            count: log?.count,
          })
        }}
      </v-col>
    </v-row>
  </v-alert>
</template>

<script>
export default {
  name: "LogItem",
  props: {
    log: {
      required: true,
      type: Object,
    },
  },
};
</script>

<style scoped></style>
/*% } %*/
