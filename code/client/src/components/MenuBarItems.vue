/*% if (feature.GUI_M_Top && !feature.GUI_M_Custom) { %*/
<template>
  <v-container>
        <v-menu offset-y transition="slide-y-transition" class="dropdown-content">
          <template v-slot:activator="{ on }">
            <v-btn text v-on="on" :class="{ 'button-on-collapse': collapsed }" tile>
              {{ $t('menu.language') }}
              <v-icon>arrow_drop_down</v-icon>
            </v-btn>
          </template>
          <v-list>
    /*%
      data.basicData.languages.forEach(function(lang) {
    %*/
            <v-list-item
              @click="changeLocale('/*%= lang.toLocaleUpperCase() %*/')"
            >
              <v-list-item-title>{{ $t('languages./*%= normalize(lang) %*/') }}</v-list-item-title>
            </v-list-item>
    /*%
      });
    %*/
          </v-list>
        </v-menu>
    /*% if (data.lists.length > 0) { %*/
        <v-menu offset-y transition="slide-y-transition" class="dropdown-content">
          <template v-slot:activator="{ on }">
            <v-btn text v-on="on" :class="{ 'button-on-collapse': collapsed }" tile>
              {{ $t('menu.lists') }}
              <v-icon>arrow_drop_down</v-icon>
            </v-btn>
          </template>
          <v-list>
      /*% data.lists.forEach(function(list) { %*/
            <v-list-item :to="{ name: '/*%= list.id %*/' }">
              <v-list-item-content>
                <v-list-item-title>{{ $t('t_/*%= normalize(list.entity) %*/.headers./*%= normalize(list.id) %*/') }}</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
      /*% }); %*/
          </v-list>
        </v-menu>
    /*% } %*/
    /*% if (feature.MV_MM_MMV_MapSelectorInMapViewer || feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
        <v-menu offset-y transition="slide-y-transition" class="dropdown-content">
          <template v-slot:activator="{ on }">
            <v-btn text v-on="on" :class="{ 'button-on-collapse': collapsed }">
              {{ $t('menu.components') }}
              <v-icon>arrow_drop_down</v-icon>
            </v-btn>
          </template>
          <v-list>
      /*% if (feature.MV_MM_MMV_MapSelectorInMapViewer && !feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
            <v-list-item :to="{ name: 'MapViewer'}">
              <v-list-item-title>
                {{ $t('menu.mapViewer') }}
              </v-list-item-title>
            </v-list-item>
      /*% } %*/
      /*% if (feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
            <v-list-item v-if="maps.length > 0">
              <v-list-item-title>
                <v-menu offset-x left>
                  <template v-slot:activator="{ on }">
                    <v-btn
                      text
                      v-on="on"
                      :class="{ 'button-on-collapse': collapsed }"

                    >
                      <v-icon>chevron_left</v-icon>
                      {{ $t("menu.maps") }}
                    </v-btn>
                  </template>
                  <v-list v-for="map in maps" :key="map.id">
                    <v-list-item :to="{ name: 'MapViewer', params: { id: map.name } }">
                      {{ map.label }}
                    </v-list-item>
                  </v-list>
                </v-menu>
              </v-list-item-title>
            </v-list-item>
      /*% } %*/
      /*% if (feature.SensorViewer) { %*/
           <v-list-item>
              <v-list-item-title>
                <v-menu offset-x left>
                  <template v-slot:activator="{ on }">
                    <v-btn
                      text
                      v-on="on"
                      :class="{ 'button-on-collapse': collapsed }"

                    >
                      <v-icon>chevron_left</v-icon>
                      {{ $t("menu.sensorViewer") }}
                    </v-btn>
                  </template>
                  <v-list>
                    /*% data.dataWarehouse.sensors.forEach(function(sensor) { %*/
                    <v-list-item :to="{ name: '/*%= sensor.id %*/SensorMap' }">
                      {{ $t('t_/*%= normalize(sensor.entity) %*/.headers./*%= normalize(sensor.id) %*/') }}
                    </v-list-item>
                    /*% }); %*/
                  </v-list>
                </v-menu>
              </v-list-item-title>
            </v-list-item>

      /*% } %*/
          </v-list>
        </v-menu>
    /*% } %*/
  </v-container>
</template>

<script>
/*% if (feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
import maps from "@/components/map-viewer/config-files/maps.json";
/*% } %*/

export default {
  name: "MenuBarItems",
  props: {
    collapsed: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  /*% if (feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
  data() {
    return {
      /*% if (feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
      maps: [],
      /*% } %*/
    };
  },
  /*% } %*/
  mounted() {
    window.addEventListener("load", () => this.$emit("bar-items-loaded"));
    /*% if (feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
    this.maps = maps.maps.map((map, index) => {
      return {
        id: index,
        name: map.name,
        label: this.$t("mapViewer.map-label." + map.name.replace(".", "-")),
      };
    });
    /*% } %*/
  },
  beforeDestroy() {
    window.removeEventListener("load", this.resizeHandler);
  },
  methods: {
    changeLocale(locale) {
      this.$i18n.locale = locale;
    },
  }
};
</script>
<style scoped>
.container {
  padding: 0px;
  height: 100%;
}
.v-btn:not(.v-btn--round).v-size--default {
  height: 100%;
}
.button-on-collapse {
  display: list-item;
  padding: 5px;
  justify-content: left;
}
.v-list {
  text-align: center;
  max-height: 70vh;
  overflow-y: auto;
}
.dropdown-content {
  max-height: 70vh;
  overflow-y: auto;
}
</style>
/*% } %*/
