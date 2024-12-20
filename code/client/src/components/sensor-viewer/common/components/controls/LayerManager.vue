/*% if (feature.SensorViewer && feature.SV_LayerManager) { %*/
<template>
  <v-tooltip left open-delay="200" color="#001f63">
    <template v-slot:activator="{ on: onMenu, attrs }">
      <v-menu
        v-if="map"
        offset-y
        :close-on-content-click="false"
        v-model="isMenuOpen"
        transition="slide-y-transition"
        class="menu"
        v-bind="attrs"
        v-on="onMenu"
      >
        <template v-slot:activator="{ on: onTooltip, attrs }">
          <v-btn
            class="boton"
            color="white"
            v-bind="attrs"
            v-on="{ ...onMenu, ...onTooltip }"
          >
            <v-icon>mdi-layers-triple</v-icon>
          </v-btn>
        </template>
        <v-container flex class="pb-2 pl-0 pr-0 white layers-container">
          <v-row class="title-row">
            <v-col class="col-visibility">
              <div class="flex-container">
                <v-btn x-small icon @click="hideAllLayers" v-if="layersShown">
                  <v-icon color="white">mdi-eye</v-icon>
                </v-btn>
                <v-btn x-small icon @click="showAllLayers" v-else>
                  <v-icon color="white">mdi-eye-off</v-icon>
                </v-btn>
                <strong class="ml-1">
                  {{ $t("layerManager.name") }}
                </strong>
              </div>
            </v-col>
            /*% if (feature.SV_LM_Center || feature.SV_LM_Order) { %*/
            <v-col class="flex-container">
              /*% if (feature.SV_LM_Center) { %*/
                {{ $t("layerManager.center") }}
              /*% } %*/
              /*% if (feature.SV_LM_Order) { %*/{{ $t("layerManager.order") }} /*% } %*/
            </v-col>
            /*% } %*/
            /*% if (feature.SV_LM_Opacity) { %*/
            <v-col class="flex-container">
              {{ $t("layerManager.opacity") }}
            </v-col>
            /*% } %*/
            /*% if (feature.SV_LM_DownloadLayer) { %*/
            <v-col class="flex-container">
              {{ $t("layerManager.download") }}
            </v-col>
            /*% } %*/
          </v-row>

          <div class="rows-container">
            <v-row
              align="center"
              class="overlay-row"
              v-for="layer in calcOverlays"
              :key="layer.name"
            >
              <v-col class="overlay-col">
                <v-checkbox
                  class="mt-1 pt-0"
                  :label="layer.name"
                  v-model="layer.selected"
                  @change="(val) => changeSelectedLayer(layer, val)"
                >
                </v-checkbox>
              </v-col>

              <v-col class="ma-0 pa-0 flex-container">
                /*% if (feature.SV_LM_Center) { %*/
                <v-btn small icon @click="setCenterOnLayer(layer)">
                  <v-icon :color="layer.centered ? 'primary' : ''">
                    mdi-crosshairs-gps
                  </v-icon>
                </v-btn>
                /*% } %*/
                /*% if (feature.SV_LM_ShowWMSStyle) { %*/
                <v-btn icon @click="wmsLegendSelected(layer)">
                  <v-icon>mdi-map-legend</v-icon>
                </v-btn>
                /*% } %*/
                /*% if (feature.SV_LM_Order) { %*/
                <v-btn
                  small
                  icon
                  :disabled="layer.order === 0"
                  @click="layerOrderUp(layer)"
                >
                  <v-icon> mdi-arrow-up </v-icon>
                </v-btn>
                <v-btn
                  small
                  icon
                  :disabled="layer.order === calcOverlays.length - 1"
                  @click="layerOrderDown(layer)"
                >
                  <v-icon> mdi-arrow-down</v-icon>
                </v-btn>
                /*% } %*/
              </v-col>

              /*% if (feature.SV_LM_Opacity) { %*/
              <v-col class="ma-0 pa-0 flex-container">
                <v-slider
                  v-bind:value="layer.slider"
                  min="0"
                  max="100"
                  thumb-label
                  @change="(val) => changeOpacity(val, layer)"
                ></v-slider>
              </v-col>
              /*% } %*/

              /*% if (feature.SV_LM_DownloadLayer) { %*/
              <v-col class="ma-0 pa-0 flex-container">
                <v-btn small icon @click="downloadLayer(layer)">
                  <v-icon> mdi-download </v-icon>
                </v-btn>
              </v-col>
              /*% } %*/
            </v-row>
          </div>
        </v-container>
      </v-menu>
    </template>
    <span>{{ $t("layerManager.title") }}</span>
  </v-tooltip>
</template>

<script>
import properties from "@/properties";

export default {
  props: {
    layers: {
      type: Array,
      required: false,
    },
    baseLayers: {
      type: Array,
      required: false,
    },
    value: {
      type: String,
      required: false,
    },
    map: {
      type: Object,
      required: true,
    },
    overlays: {
      type: Array,
      required: false,
    },
    multiple: {
      type: Boolean,
      required: false,
      default: false,
    },
    loadingMap: {
      type: Boolean,
      required: false,
    },
    buildWMSLegendControl: {
      type: Function,
      required: false,
      default: () => {},
    },
  },
  data() {
    return {
      selectedTileLayer: null,
      selectedOverlay: null,
      isMenuOpen: false,
      calcOverlays: [],
      orderedOverlays: [],
    };
  },
  watch: {
    isMenuOpen(newVal) {
      this.$emit("menu-open", newVal);
    },
    loadingMap(newVal) {
      if (!newVal) this.calculateOverlays();
    },
  },
  computed: {
    layersShown() {
      return this.calcOverlays.filter((layer) => layer.selected).length > 0;
    },
  },
  mounted() {
    this.calculateOverlays();
  },
  methods: {
    calculateOverlays() {
      this.calcOverlays = this.map
        .getLayers()
        .filter((el) => !el.options.baseLayer)
        .map((layer, index) => {
          return {
            id: layer.options.id,
            name: layer.options.label,
            selected: layer.options.selected,
            slider: layer.options.opacity * 100,
            order: index,
            centered: false,
          };
        });
    },
    /*% if (feature.SV_LM_Opacity) { %*/
    changeOpacity(newVal, layerSelected) {
      const calcOpacity = newVal;
      const realLayer = this.getLayer(layerSelected);
      realLayer.setOpacity(calcOpacity);
    },
    /*% } %*/
    changeSelectedLayer(layerSelected, newVal) {
      const realLayer = this.getLayer(layerSelected);
      if (!newVal) {
        layerSelected.selected = false;
        this.map.hideLayer(realLayer);
      } else {
        layerSelected.selected = true;
        this.map.showLayer(realLayer);
      }
    },
    /*% if (feature.SV_LM_Center) { %*/
    setCenterOnLayer(layerSelected) {
      // If layer was already selected, we center on all layers
      if (layerSelected.centered) {
        this.calcOverlays = this.calcOverlays.map((el) => {
          el.centered = false;
          return el;
        });
        this.map.centerView("visible");
        return;
      }

      // Else, set center with the layer bounds
      const realLayer = this.getLayer(layerSelected);
      realLayer.getBounds().then((bounds) => {
        this.map.centerView(bounds);
        // Change state and style
        this.calcOverlays = this.calcOverlays.map((el) => {
          if (el.centered) el.centered = false;
          if (el.name === layerSelected.name) el.centered = true;
          return el;
        });
      });
    },
    /*% } %*/
    /*% if (feature.SV_LM_Order) { %*/
    // Sets the order of a layer to a higher value
    layerOrderUp(layer) {
      if (layer.order === 0) return;
      const index = layer.order;

      // Set new order on layer list
      this.calcOverlays[index].order = this.calcOverlays[index].order - 1;
      this.calcOverlays[index - 1].order =
        this.calcOverlays[index - 1].order + 1;

      // Set new order on map
      this.getLayer(layer).setOrder(layer.order);
      this.getLayer(this.calcOverlays[index - 1]).setOrder(layer.order + 1);

      // Re-render list sorted
      this.calcOverlays = this.calcOverlays.sort((a, b) => a.order - b.order);
    },
    // Sets the order of a layer to a lower value
    layerOrderDown(layer) {
      if (layer.order === this.calcOverlays.length - 1) return;

      const index = layer.order;
      // Set new order on layer list
      this.calcOverlays[index].order = this.calcOverlays[index].order + 1;
      this.calcOverlays[index + 1].order =
        this.calcOverlays[index + 1].order - 1;

      // Set new order on map
      this.getLayer(layer).setOrder(layer.order);
      this.getLayer(this.calcOverlays[index + 1]).setOrder(layer.order - 1);

      // Re-render list sorted
      this.calcOverlays = this.calcOverlays.sort((a, b) => a.order - b.order);
    },
    /*% } %*/
    showAllLayers() {
      this.calcOverlays.forEach((layer) => {
        this.changeSelectedLayer(layer, true);
      });
    },
    hideAllLayers() {
      this.calcOverlays.forEach((layer) => {
        this.changeSelectedLayer(layer, false);
      });
    },
    getLayer(layerSelected) {
      const findLayer = this.map.getLayers().find((layer) => {
        return layer.options.id === layerSelected.id;
      });

      return findLayer || null;
    },
    wmsLegendSelected(layer) {
      this.$emit("wms-legend-selected", layer);
    },
    /*% if (feature.SV_LM_DownloadLayer) { %*/
    async downloadLayer(layer) {
      const selectedLayer = this.layers.filter((l) => l.name === layer.id);
      let layerName = null;

      if (selectedLayer.length > 0) {
        layerName = selectedLayer[0].options.layers[0];
      } else {
        layerName = layer.name;
      }

      const attributes = await this._getAttributes(layerName);

      let urlAttributes = "";
      attributes.dataAttributes.forEach((att) => {
        urlAttributes = urlAttributes + att + ",";
      });

      let data = null;
      attributes.geomAttributes.forEach(async (geomAtt) => {
        data = await this._getShapefileData(layerName, geomAtt, urlAttributes);
        const blob = await data.blob();
        if (blob.type != "application/zip") {
          this.$notify({
            text: this.$t("layerManager.layerNotAvailable"),
            type: "error",
          });
          return;
        }

        const newBlob = new Blob([blob]);
        const blobUrl = window.URL.createObjectURL(newBlob);
        const link = document.createElement("a");
        link.href = blobUrl;
        link.setAttribute("download", layer.name + ".zip");
        document.body.appendChild(link);
        link.click();
        link.parentNode.removeChild(link);
        window.URL.revokeObjectURL(blobUrl);
      });
    },

    async _getShapefileData(layerName, geomAtt, urlAttributes) {
      urlAttributes = urlAttributes + "," + geomAtt;
      let shapefilesUrl = properties.GEOSERVER_URL.substring(
        0,
        properties.GEOSERVER_URL.indexOf("geoserver")
      ).concat(
        "geoserver/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=" +
          layerName +
          "&outputFormat=shape-zip&cql_filter=" +
          geomAtt +
          "%20is%20not%20null&&propertyName=" +
          urlAttributes
      );

      return await fetch(shapefilesUrl, {
        method: "GET",
      });
    },

    async _getAttributes(layerName) {
      const attributesUrl = properties.GEOSERVER_URL.substring(
        0,
        properties.GEOSERVER_URL.indexOf("geoserver")
      ).concat(
        "geoserver/webeiel/wfs?service=WFS&version=1.0.0&request=DescribeFeatureType&typeName=" +
          layerName +
          "&outputFormat=application/json"
      );

      const attributes = await (
        await fetch(attributesUrl, {
          method: "GET",
        })
      ).json();
      let geomAttributes = [];
      let dataAttributes = [];
      attributes.featureTypes[0].properties.forEach((att) => {
        if (
          att.localType === "MultiPoint" ||
          att.localType === "MultiLineString" ||
          att.localType === "MultiPolygon" ||
          att.localType === "Point" ||
          att.localType === "LineString" ||
          att.localType === "Polygon"
        ) {
          geomAttributes.push(att.name);
        } else {
          dataAttributes.push(att.name);
        }
      });
      return { geomAttributes: geomAttributes, dataAttributes: dataAttributes };
    },
    /*% } %*/
  },
};
</script>
<style scoped>
body {
  padding: 0;
  margin: 0;
}

.flex-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.boton {
  position: absolute !important;
  top: 10px !important;
  right: 10px !important;
  z-index: 3 !important;
}

.layers-container {
  padding-top: 0;
  margin-top: 0;
}

.title-row {
  margin-top: 0;
  padding-top: 0;
  margin-bottom: 1px;
  position: sticky;
  top: 0;
  background-color: #1976d2;
  opacity: 1;
  z-index: 100;
  font-size: small;
  color: white;
  box-shadow: 0 2px 2px -1px rgba(0, 0, 0, 0.4);
}

.col-visibility {
  padding-left: 11px;
}

.rows-container {
  overflow-y: auto;
  overflow-x: hidden;
  min-width: 510px;
  max-height: 50vh;
}

.overlay-row {
  margin-top: 0;
  padding-top: 0;
  margin-left: 0.8em;
  padding-bottom: 1em;
}

.overlay-col {
  margin: 0;
  padding: 0;
  padding-top: 5px;
}

.overlay-style-col {
  font-size: small;
}

::v-deep .v-messages {
  display: none;
}

::v-deep .v-label {
  font-size: small;
}

::v-deep .v-slider__thumb-label {
  position: absolute;
}
</style>
/*% } %*/
