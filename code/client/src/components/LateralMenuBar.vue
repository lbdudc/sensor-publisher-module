/*% if ( !feature.GUI_M_Custom && (feature.GUI_M_Top || feature.GUI_M_Left) ) { %*/
<template>
  <!-- Menú lateral -->
  <v-navigation-drawer v-model="localDrawer" app>

    <v-list dense>

      <v-list-group no-action>
        <template v-slot:activator>
          <v-list-item-icon>
            <v-icon>mdi-web</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>{{ $t('menu.language') }}</v-list-item-title>
          </v-list-item-content>
        </template>
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
      </v-list-group>

  /*% if (data.lists.length > 0) { %*/
      <v-list-group no-action>
        <template v-slot:activator>
          <v-list-item-icon>
            <v-icon>list</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>{{ $t('menu.lists') }}</v-list-item-title>
          </v-list-item-content>
        </template>
        /*% data.lists.forEach(function(list) { %*/
        <v-list-item :to="{ name: '/*%= list.id %*/' }">
          <v-list-item-content>
            <v-list-item-title>{{ $t('t_/*%= normalize(list.entity) %*/.headers./*%= normalize(list.id) %*/') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        /*% }); %*/
      </v-list-group>
  /*% } %*/
  /*% if (feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
      <v-list-group no-action>
        <template v-slot:activator>
          <v-list-item-icon>
            <v-icon>category</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>{{ $t('menu.components') }}</v-list-item-title>
          </v-list-item-content>
        </template>
        /*% if (feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
        <v-list-item v-if="maps.length > 0">
          <v-list-item-content>
            <v-list-group no-action sub-group>
              <template v-slot:activator>
                <v-list-item-title>{{ $t("menu.maps") }}</v-list-item-title>
              </template>
              <v-list v-for="map in maps" :key="map.id">
                <v-list-item :to="{ name: 'MapViewer', params: { id: map.name } }">
                  {{ map.label }}
                </v-list-item>
              </v-list>
            </v-list-group>
          </v-list-item-content>
        </v-list-item>
        /*% } %*/
      </v-list-group>
  /*% } %*/
    </v-list>
  </v-navigation-drawer>
</template>

<script>
/*% if (feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
import maps from "@/components/map-viewer/config-files/maps.json";
/*% } %*/

export default {
  name: "LateralMenuBar",
  props: {
    drawer: {
      type: Boolean,
      required: true
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
  /*% if (feature.MV_MM_MMV_MapSelectorInMenuElement) { %*/
  mounted() {
    this.maps = maps.maps.map((map, index) => {
      return {
        id: index,
        name: map.name,
        label: this.$t("mapViewer.map-label." + map.name.replace(".", "-")),
      };
    });
  },
  /*% } %*/
  computed: {
    localDrawer: {
      get() {
        return this.drawer;
      },
      set(val) {
        this.$emit("drawer-changed", val);
      }
    }
  },
  methods: {
    changeLocale(locale) {
      this.$i18n.locale = locale;
    },
  }
};
</script>
<style scoped>
.v-list-item__title,
.v-list-item__subtitle {
  white-space: normal;
}
</style>
/*% } %*/
