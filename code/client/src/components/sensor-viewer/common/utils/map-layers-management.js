/*% if (feature.SensorViewer) {
const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
  return sensor.isMoving === true; }); %*/
  import i18n from "@/plugins/i18n";
  import maps from "@/components/sensor-viewer/common/config-files/maps";
  import layers from "@/components/sensor-viewer/common/config-files/layers";
  import styles from "@/components/sensor-viewer/common/config-files/styles.json";

  import properties from "@/properties";
  import { getStyle /*% if(hasMovingSensors){ %*/, createShipRoutesRandomStyleDefinition/*% } %*/ } from "@/common/sensor-styles-common";
  import RepositoryFactory from "@/repositories/RepositoryFactory";
  import { Map, TileLayer, WMSLayer, GeoJSONLayer } from "@lbdudc/map-viewer";

  /**
   * Creates a Map-Viewer map using the options from maps.layer configuration file
   * @param mapSelected : ID of the map to load
   * @returns {Map} : instance of Map-Viewer's Map
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

    const mapOptions = {
      ...maps.maps[mapSelected].mapOptions,
      crs: crs,
    };

    return new Map(
      "map",
      maps.maps[mapSelected].name,
      maps.maps[mapSelected].center,
      mapOptions
    );
  }

  /**
   * Loads the baseLayers selected in the mapSelected's configuration into the map's instance received
   * @param map: instance of the map
   * @param mapSelected: ID of the map
   */
  function loadBaseLayers(map, mapSelected) {
    const baseLayers = layers.layers.filter((layer) =>
      maps.maps[mapSelected].layers.find(
        (mapLayer) => mapLayer.name === layer.name && mapLayer.baseLayer
      )
    );
    baseLayers
      .sort((a, b) => {
        return a - b;
      })
      .map((baseLayer) => {
        const baseLayerInMap = maps.maps[mapSelected].layers.find(
          (mapLayer) => mapLayer.name === baseLayer.name
        );
        return {
          id: baseLayer.name,
          label: i18n.t(
            "mapViewer.layer-label." + baseLayer.name.replace(".", "-")
          ),
          type: baseLayer.layerType,
          baseLayer: true,
          selected: baseLayerInMap.selected === true,
          url: baseLayer.url || properties.GEOSERVER_URL + "/wms",
          params: baseLayer.options,
        };
      })
      .forEach((layerParams) => {
        if (layerParams.type === "tilelayer") {
          map.addLayer(new TileLayer(layerParams));
        } else if (layerParams.type === "wms") {
          map.addLayer(new WMSLayer(layerParams));
        }
      });
  }

  /*% if(hasMovingSensors) { %*/
  async function loadOverlayLayers(
    spec,
    map,
    mapSelected,
    params,
    options,
    popupFn,
    store
  ) {
    const layer = layers.layers.find(
      (l) => l.name == spec.id.toLowerCase() + "-layer"
    );
    const layerInMap = maps.maps[mapSelected].layers.find(
      (mapLayer) => mapLayer.name === layer.name
    );

    const label = i18n.t("mapViewer.layer-label." + layer.name.replace(".", "-"));
    const form = layerInMap.form || layer.form;
    const repository = RepositoryFactory.get(`${spec.repository}`);

    const data = await repository.getData(params, options);
    const routesFeatureCollection = store.getSelector("SPATIAL_AGGREGATION")
      ?.value
      ? data
      : _buildRoutesFeatureCollection(data);

    //Adss random styles to styles json definition
    createShipRoutesRandomStyleDefinition(routesFeatureCollection).forEach(
      (newStyle) => {
        let foundStyle = styles.styles.find(
          (style) => style.name === newStyle.name
        );
        if (!foundStyle) {
          styles.styles.push(newStyle);
        } else {
          Object.assign(foundStyle, newStyle);
        }
      }
    );

    // Get all available styles
    let availableStyles = layer.availableStyles?.map((availableStyleName) =>
      getStyle(styles, availableStyleName)
    );

    // Get default style
    const defaultStyle =
      layerInMap.style != null
        ? availableStyles.find((style) => style.id === layerInMap.style)
        : layer.defaultStyle != null
        ? availableStyles.find((style) => style.id === layer.defaultStyle)
        : null;

    map.removeLayer(layer.name);
    await map.addLayer(
      new GeoJSONLayer(
        routesFeatureCollection,
        {
          id: layer.name,
          label: label,
          baseLayer: false,
          selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
          clustering: false,
          list: layerInMap.list || layer.list,
          popup: popupFn(form),
        },
        availableStyles,
        defaultStyle
      )
    );
  }

  // Liks route segments of the same ship
  function _buildRoutesFeatureCollection(featureCollection) {
    if (featureCollection.features.length < 2) {
      return featureCollection;
    }
    let routesFeatures = [];
    featureCollection.features.forEach((feature, index) => {
      if (index < featureCollection.features.length - 1) {
        const nextFeature = featureCollection.features[index + 1];
        if (
          feature.properties.sensor_id ==
          featureCollection.features[index + 1].properties.sensor_id
        ) {
          let coordinates =
            feature.geometry.type == "Point"
              ? [feature.geometry.coordinates]
              : (coordinates = feature.geometry.coordinates);

          if (nextFeature.geometry.type == "Point") {
            const nextPoint = nextFeature.geometry.coordinates;
            coordinates.push(nextPoint);
          } else {
            coordinates.push(nextFeature.geometry.coordinates[0]);
          }
          const geometry = {
            type: "LineString",
            coordinates: coordinates,
          };
          const extendedLine = {
            id: feature.id,
            type: "Feature",
            geometry: geometry,
            properties: feature.properties,
          };
          routesFeatures.push(extendedLine);
        } else {
          routesFeatures.push(feature);
        }
      }
    });
    return { type: "FeatureCollection", features: routesFeatures };
  }
  /*% } else { %*/
  /**
   * Loads the overlays selected in the mapSelected's configuration into the map's instance received
   * @param map: instance of the map
   * @param mapSelected: ID of the map
   * @param popupFn: function to create the GeoJSON layers' popup
   */
  function loadOverlayLayers(map, mapSelected, popupFn) {
    const overlays = layers.layers.filter((l) =>
      maps.maps[mapSelected]?.layers.find(
        (mapLayer) => !mapLayer.baseLayer && mapLayer.name === l.name
      )
    );
    overlays
      .sort((a, b) => {
        return a - b;
      })
      .forEach((layer) => {
        const layerInMap = maps.maps[mapSelected].layers.find(
          (mapLayer) => mapLayer.name === layer.name
        );
        const label = i18n.t(
          "mapViewer.layer-label." + layer.name.replace(".", "-")
        );
        let availableStyles, defaultStyle;
        // Tilelayers hasn't got styles
        if (layer.layerType !== "tilelayer") {
          // Get all available styles
          availableStyles = layer.availableStyles?.map((availableStyleName) =>
            getStyle(styles, availableStyleName)
          );
          // Get default style
          defaultStyle =
            layerInMap.style != null
              ? availableStyles.find((style) => style.id === layerInMap.style)
              : layer.defaultStyle != null
              ? availableStyles.find((style) => style.id === layer.defaultStyle)
              : null;
        }

        if (layer.layerType === "wms") {
          map.addLayer(
            new WMSLayer(
              {
                id: layer.name,
                label: label,
                baseLayer: false,
                selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
                list: layerInMap.list || layer.list,
                url: layer.url || properties.GEOSERVER_URL + "/wms",
                params: layer.options,
              },
              availableStyles,
              defaultStyle
            )
          );
        } else if (layer.layerType === "tilelayer") {
          map.addLayer(
            new TileLayer({
              id: layer.name,
              label: label,
              baseLayer: false,
              selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
              url: layer.url,
              params: layer.options,
            })
          );
        } else if (layer.layerType === "geojson") {
          const repository = RepositoryFactory.get(
            _getRepositoryNameFromJSON(layer)
          );
          const fileName = `${_getPropertyNameFromJSON(layer)}`;
          const form = layerInMap.form || layer.form;
          map.addLayer(
            new GeoJSONLayer(
              repository.getGeom(fileName),
              {
                id: layer.name,
                label: label,
                baseLayer: false,
                selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
                clustering: false,
                list: layerInMap.list || layer.list,
                popup: popupFn(form),
              },
              availableStyles,
              defaultStyle
            )
          );
        }
      });
  }
  /**
   * Reloads sensor layer when sensor type is dynamic
   * @param map: instance of the map
   * @param mapSelected: ID of the map
   * @param popupFn: function to create the GeoJSON layers' popup
   */
  async function reloadSensorsLayer(sensorName, map, mapSelected, popupFn) {
    const layer = layers.layers.find((l) => l.name == sensorName + "-layer");
    const fileName = `${_getPropertyNameFromJSON(layer)}`;
    const layerInMap = maps.maps[mapSelected].layers.find(
      (mapLayer) => mapLayer.name === layer.name
    );

    const label = i18n.t("mapViewer.layer-label." + layer.name.replace(".", "-"));
    const form = layerInMap.form || layer.form;
    const repository = RepositoryFactory.get(_getRepositoryNameFromJSON(layer));
    // Get all available styles
    const availableStyles = layer.availableStyles?.map((availableStyleName) =>
      getStyle(styles, availableStyleName)
    );
    // Get default style
    const defaultStyle =
      layerInMap.style != null
        ? availableStyles.find((style) => style.id === layerInMap.style)
        : layer.defaultStyle != null
        ? availableStyles.find((style) => style.id === layer.defaultStyle)
        : null;

    const layerToHide = map
      .getOverlays()
      .find((l) => l.options.id === layer.name);
    await map.hideLayer(layerToHide);
    await map.addLayer(
      new GeoJSONLayer(
        repository.getGeom(fileName),
        {
          id: layer.name,
          label: label,
          baseLayer: false,
          selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
          clustering: false,
          list: layerInMap.list || layer.list,
          popup: popupFn(form),
        },
        availableStyles,
        defaultStyle
      )
    );
  }

  function _getEntityNameFromJSON(layer) {
    const prop = layer.entityName != null ? "entityName" : "name";
    const nameParts = layer[prop].split("-");
    if (nameParts.length > 2) {
      return (
        nameParts[0] +
        "-" +
        nameParts[1].charAt(0).toUpperCase() +
        nameParts[1].slice(1)
      );
    }
    return nameParts[0];
  }

  function _getRepositoryNameFromJSON(layer) {
    let entityName = _getEntityNameFromJSON(layer);
    let repositorySuffix = "EntityRepository";
    // Check if component entity
    if (entityName.indexOf("-") !== -1) {
      repositorySuffix = "Repository";
      entityName = entityName.replace("-", "");
    }
    return (
      entityName.charAt(0).toUpperCase() + entityName.slice(1) + repositorySuffix
    );
  }

  function _getPropertyNameFromJSON(layer) {
    // returns last substring after a '-'
    const prop = layer.entityName != null ? "entityName" : "name";
    const nameParts = layer[prop].split("-");
    return _toCamelCase(nameParts[0]);
  }

  function _toCamelCase(str){
    return str
          .replace(/\s(.)/g, function (a) {
              return a.toUpperCase();
          })
          .replace(/\s/g, '')
          .replace(/^(.)/, function (b) {
              return b.toLowerCase();
          });
  }
  /*% } %*/

  export { createMap, loadBaseLayers, loadOverlayLayers/*% if(!hasMovingSensors) { %*/, reloadSensorsLayer /*% } %*/};
  /*% } %*/
