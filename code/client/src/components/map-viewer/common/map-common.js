/*% if (feature.MapViewer) { %*/
/**
 * Common functions for Map-Viewer creation
 */
import i18n from "@/plugins/i18n";
import maps from "@/components/map-viewer/config-files/maps.json";
import layers from "../config-files/layers.json";
import properties from "@/properties";
import { createGeoJSONLayer, createWMSLayer } from "./map-layer-common";
import {
  LayerFactory,
  Map,
  TileLayer,
  WMSLayer,
} from "@lbdudc/map-viewer";

/*% if (feature.MV_LM_ExternalLayer) { %*/
import ResourceFactory from "../resources/ResourcesFactory";
/*% } %*/

/**
 * Creates a Map-Viewer map using the options from maps.layer configuration file
 */
function createMap(mapSelected) {
  // By default, if the user don't define his own CRS, we use the Leaflet's default
  let crs = L.CRS.EPSG3857;
  if (
    maps.maps[mapSelected].mapOptions &&
    maps.maps[mapSelected].mapOptions.crs
  ) {
    const crsOptions = maps.maps[mapSelected].mapOptions.crs;
    crs = new L.Proj.CRS(
      crsOptions.srid,
      crsOptions.proj4Config.params,
      crsOptions.proj4Config.options
    );
  }

  let mapOptions = { ...maps.maps[mapSelected].mapOptions };
  mapOptions.crs = crs;

  return new Map(
    "map",
    maps.maps[mapSelected].name,
    maps.maps[mapSelected].center,
    mapOptions
  );
}


/**
 * Loads the baseLayers selected in the mapSelected's configuration into the map's instance received
 */
function loadBaseLayers(map, mapSelected) {
  let baseLayers = layers.layers.filter((layer) =>
    maps.maps[mapSelected].layers.find(
      (mapLayer) => mapLayer.name == layer.name && mapLayer.baseLayer
    )
  );

  baseLayers
    .sort((a, b) => {
      return a.name - b.name;
    })
    .map((json) => {
      const baseLayerInMap = maps.maps[mapSelected].layers.find(
        (mapLayer) => mapLayer.name === json.name
      );
      return {
        id: json.name,
        label: i18n.t("mapViewer.layer-label." + json.name.replace(".", "-")),
        type: json.layerType,
        baseLayer: true,
        opacity: baseLayerInMap.opacity,
        selected: baseLayerInMap.selected === true,
        url: json.url || properties.GEOSERVER_URL + "/wms",
        params: json.options,
      };
    })
    .forEach((layerParams) => {
      let layer = null;
      if (layerParams.type === "tilelayer") {
        layer = new TileLayer(layerParams);
        map.addLayer(layer);
      } else if (layerParams.type === "wms") {
        layer = new WMSLayer(layerParams);
        map.addLayer(layer);
      }
    });
}

/**
 * Loads the overlays selected in the mapSelected's configuration into the map's instance received
 */
function loadOverlaysLayers(map, mapSelected) {
  const layersToShow = layers.layers.filter(l =>
    maps.maps[mapSelected].layers.find(
      mapLayer => !mapLayer.baseLayer && mapLayer.name === l.name
    )
  );

  layersToShow
    .sort((a, b) => {
      const layerA = maps.maps[mapSelected].layers.find(
          (mapLayer) => mapLayer.name === a.name
        ),
        layerB = maps.maps[mapSelected].layers.find(
          (mapLayer) => mapLayer.name === b.name
        );
      return (
        (layerA.order != null ? layerA.order : 100) -
        (layerB.order != null ? layerB.order : 100)
      );
    })
    .forEach(json => {
      const layerInMap = maps.maps[mapSelected].layers.find(
        mapLayer => mapLayer.name === json.name
      );

      const layerParams = {
        label: i18n.t("mapViewer.layer-label." + json.name.replace(".", "-")),
        /*% if (feature.MV_MS_GJ_Paginated) { %*/
        bounds: map.getLeafletMap().getBounds(),
        /*% } %*/
        added: false,
      };

      if (json.layerType === "wms") {
        map.addLayer(
          createWMSLayer(json, layerParams, layerInMap)
        );
      } else if (json.layerType === "geojson") {
        map.addLayer(
          createGeoJSONLayer(json, layerParams, layerInMap)
        );
      } else if (json.layerType === "tilelayer") {
        map.addLayer(
          new TileLayer({
            id: json.name,
            label: layerParams.label,
            baseLayer: false,
            opacity: layerInMap.opacity,
            selected: layerInMap.selected || layerInMap.selected == null,  // if no value is given, it is shown in map
            url: json.url,
            params: json.options,
          })
        );
      }
    });
}

/*% if (feature.MV_LM_ExternalLayer) { %*/
async function loadGeoParquetLayers(map, data) {
  const promises = data.layers.map(async (layer) => {
    if (layer.options.type === "GEOPARQUET") {
      const resource = ResourceFactory.get(layer.options.type);
      try {
        const result = await resource.search(
          layer.options.url,
          null,
          layer.options.label
        );

        const layerFactory = LayerFactory.getInstance();
        const initializedLayer = await layerFactory.initializeGeoParquetLayer(
          {
            ...layer.options,
            data: result,
            type: "GEOJSON"
          }
        );
        map?.addLayer(initializedLayer);
      } catch (error) {
        console.error(error);
      }
    }
  });

  await Promise.all(promises);
}
/*% } %*/

export { createMap, loadBaseLayers, loadOverlaysLayers /*% if (feature.MV_LM_ExternalLayer) { %*/, loadGeoParquetLayers /*% } %*/ };
/*% } %*/
