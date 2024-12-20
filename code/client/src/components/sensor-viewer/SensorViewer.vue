/*% if (feature.SensorViewer) { %*/
<template>
  <div class="map-viewer" v-if="spec">
    <MapViewer class="map-cont" :spec="spec" />
  </div>
</template>

<script>
import MapViewer from "./mapviewer/MapViewer.vue";
import { getSensorSpecById } from "@/components/sensor-viewer/common/utils/sensor-utils.js";

export default {
  components: {
    MapViewer: MapViewer,
  },
  data() {
    return {
      spec: null,
    };
  },
  mounted() {
    const fullPath = this.$route.fullPath;
    let id = fullPath.split("/sensor-viewer/")[1];
    if (id.indexOf("?") != -1) id = id.substring(0, id.indexOf("?"));
    this.spec = getSensorSpecById(id);
  },
};
</script>

<style>
.map-viewer {
  height: 93vh;
}

.map-cont {
  height: 100%;
  width: 100%;
  padding: 0px;
}
</style>
/*% } %*/
