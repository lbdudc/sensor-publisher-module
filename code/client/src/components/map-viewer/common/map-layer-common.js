/*% if (feature.MapViewer || feature.MV_LM_ExternalLayer) { %*/
/**
 * Common functions to create and modify layers
 */
import RepositoryFactory from "@/repositories/RepositoryFactory";
/*% if (feature.MV_MS_GJ_Paginated) { %*/
import layers from "../config-files/layers.json";
/*% } %*/
import { getStyle } from "@/components/map-viewer/common/map-styles-common";
import {
  GeoPackageLayer,
  GeoJSONLayer,
  WMSLayer,
  WCSLayer,
  GeoTIFFLayer,
  ShapeFileLayer,
 } from "@lbdudc/map-viewer";



/**
 * Creates a WCS Layer.
 */
async function createWCSLayer(json, layerParams, layerInMap = {}) {
  const options = json.options;

  const availableStyles = _getAvailableStyles(json);
  const defaultStyle = _getDefaultStyle(json, availableStyles, layerInMap);

  var wcsLayer = new WCSLayer(
    {
      id: json.name,
      label: layerParams.label,
      baseLayer: false,
      selected: layerInMap.selected || layerInMap.selected == null,
      url: json.url || properties.GEOSERVER_URL + "/wcs",
      params: options,
      added: layerParams.added,
    },
    availableStyles,
    defaultStyle
  );

  await wcsLayer.createLayerFromGeoraster();
  return wcsLayer;
}


/**
 * Creates a GeoPackage Layer.
 */
async function createGeoPackageLayer(json, layerParams, layerInMap = {}) {
  const availableStyles = _getAvailableStyles(json);
  const defaultStyle = _getDefaultStyle(json, availableStyles, layerInMap);

  return await GeoPackageLayer.create(
    json.data,
    {
      id: json.name || layerParams.label,
      label: layerParams.label,
      baseLayer: false,
      selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
      url: json.url,
      added: layerParams.added,
    },
    availableStyles,
    defaultStyle
  );
}

/**
 * Creates a GeoTIFF Layer.
 */
async function createGeoTIFFLayer(json, layerParams, layerInMap = {}) {
  const options = json.options;

  const availableStyles = _getAvailableStyles(json);
  const defaultStyle = _getDefaultStyle(json, availableStyles, layerInMap);

  const geoTIFFLayer = new GeoTIFFLayer(
    {
      id: json.name,
      label: layerParams.label,
      baseLayer: false,
      selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
      url: json.url,
      params: options,
      added: layerParams.added,
      resolution: json.resolution,
      opacity: json.opacity,
    },
    availableStyles,
    defaultStyle
  );

  await geoTIFFLayer.createGeoRasterLayer();
  return geoTIFFLayer;
}

/**
 * Creates a Shapefile Layer.
 */
function createShapefileLayer(json, layerParams, layerInMap = {}) {
  const availableStyles = _getAvailableStyles(json);
  const defaultStyle = _getDefaultStyle(json, availableStyles, layerInMap);

  return new ShapeFileLayer(
    {
      id: json.name,
      label: layerParams.label,
      baseLayer: false,
      selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
      url: json.url,
      added: layerParams.added,
    },
    availableStyles,
    defaultStyle
  );
}

/**
 * Generates a unique layer ID for an existing map.
 */
function getUniqueLayerId(map, layerName, count = 1) {
  const uniqueId = `${layerName}.${count}`;
  if (map.getLayer(uniqueId)) {
    return getUniqueLayerId(map, layerName, count + 1);
  } else {
    return uniqueId;
  }
}

/**
 * Creates a WMS Layer.
 */
function createWMSLayer(json, layerParams, layerInMap = {},) {
  const options =

  json.options;

  const availableStyles = _getAvailableStyles(json);
  const defaultStyle = _getDefaultStyle(json, availableStyles, layerInMap);

  return new WMSLayer(
    {
      id: layerParams.label,
      label: layerParams.label,
      baseLayer: false,
      selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
      url: json.url,
      params: options,
      added: layerParams.added,
    },
    availableStyles,
    defaultStyle
  );
}

function createExternalGeoJSONLayer(json, layerParams, layerInMap = {}) {
  const availableStyles = _getAvailableStyles(json);
  const defaultStyle = _getDefaultStyle(json, availableStyles, layerInMap);

  return new GeoJSONLayer(
    json,
    {
      id: json.name || layerParams.label,
      label: layerParams.label,
      baseLayer: false,
      selected: layerInMap.selected || layerInMap.selected == null, // if no value is given, it is shown in map
      url: json.url,
      added: layerParams.added,
      type: layerParams.type,
    },
    availableStyles,
    defaultStyle
  );
}

/**
 * Creates a GeoJSON Layer.
 */
function createGeoJSONLayer(json, layerParams, layerInMap = {}) {
    const repository = RepositoryFactory.get(
      _getRepositoryNameFromJSON(json)
    );

    /*% if (feature.MV_MS_GJ_Paginated) { %*/
    const options = _getBBoxPagination(layerParams.bounds);
    /*% } %*/
    const availableStyles = _getAvailableStyles(json);
    const defaultStyle = _getDefaultStyle(json, availableStyles, layerInMap);
    /*% if (feature.MV_MS_GJ_Cached) { %*/
    const repositoryName = _getRepositoryNameFromJSON(json);
    const fileName = repositoryName.includes("Entity")
        ? _getEntityNameFromJSON(json)
        : _getPropertyNameFromJSON(json);
    /*% } %*/

    return new GeoJSONLayer(
        repository.getGeom(
            /*% if (feature.MV_MS_GJ_Cached) { %*/fileName, /*% } else { %*/_getPropertyNameFromJSON(json), /*% } %*/
            /*% if (feature.MV_MS_GJ_Paginated) { %*/ options), /*% } else { %*/
            {
                params: {
                    properties: true,
                }
            }),
            /*% } %*/
        {
            id: json.name,
            label: layerParams.label,
            baseLayer: false,
            selected: layerInMap.selected || layerInMap.selected == null,  // if no value is given, it is shown in map
            /*% if (feature.MV_Clustering) { %*/
            clustering: true,
            /*% } %*/
            url: json.url,
            added: layerParams.added,
            type: layerParams.type,
        },
        availableStyles,
        defaultStyle,
    );
}

/*% if (feature.MV_MS_GJ_Paginated) { %*/
/**
 * Updates GeoJSON layer features within a given bounding box.
 */
function updateLayer(map, bbox, popupFn) {
  const layersToShow = layers.layers.filter(
    (layer) => layer.layerType === "geojson" && map.getLayer(layer.name)
  );

  const options = _getBBoxPagination(bbox);

  layersToShow.forEach((json) => {
    const repository = RepositoryFactory.get(
      _getRepositoryNameFromJSON(json)
    );

    repository
      .getGeom(_getPropertyNameFromJSON(json), options)
      .then((data) => {
        if (data.features.length !== 0) {
          let layer = map.getLayer(json.name);
          layer
            /*% if (feature.MV_Clustering) { %*/
            ._getRealLayer()
            /*% } else { %*/
            .getLayer()
            /*% } %*/
            .then((layr) => {
              /*% if (feature.MV_Clustering) { %*/
              layer.getLayer().then((cluster) => {
                cluster.clearLayers();
                cluster.addLayer(_updateLayerData(layr, data));
              });
              /*% } else { %*/
              _updateLayerData(layr, data);
              /*% } %*/
            });
        }
      });
  });
}

function _updateLayerData(layer, data) {
  if (Object.keys(layer._layers).length === 0) {
    layer.addData(data);
  } else {
    const dataObj = {};
    data.features.forEach((item) => (dataObj[item.id] = item));
    layer.eachLayer((subLayer) => {
      const found = dataObj[subLayer.feature.id];
      if (found) {
        delete dataObj[subLayer.feature.id];
      } else {
        layer.removeLayer(subLayer);
      }
    });

    const newFeatures = Object.values(dataObj);
    layer.addData(newFeatures);
  }
  /*% if (feature.MV_Clustering) { %*/
  return layer;
  /*% } %*/
}
/*% } %*/

function _getAvailableStyles(json) {
  return json.availableStyles?.map((availableStyleName) =>
    getStyle(availableStyleName)
  );
}

function _getDefaultStyle(json, availableStyles, layerInMap) {
  return layerInMap.style != null
    ? availableStyles.find((style) => style.id === layerInMap.style)
    : json.defaultStyle != null
      ? availableStyles.find((style) => style.id === json.defaultStyle)
      : null;
}


function _getEntityNameFromJSON(json) {
  const prop = json.entityName != null ? "entityName" : "name";
  const nameParts = json[prop].split("-");
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

function _getRepositoryNameFromJSON(json) {
  let entityName = _getEntityNameFromJSON(json);
  let repositorySuffix = "EntityRepository";
  // Check if component entity
  if (entityName.indexOf("-") != -1) {
    repositorySuffix = "Repository";
    entityName = entityName.replace("-", "");
  }
  return (
    entityName.charAt(0).toUpperCase() + entityName.slice(1) + repositorySuffix
  );
}

function _getPropertyNameFromJSON(json) {
  // returns last substring after a '-'
  const prop = json.entityName != null ? "entityName" : "name";
  const nameParts = json[prop].split("-");
  return nameParts[nameParts.length - 1].split(".")[0];
}

/*% if (feature.MV_MS_GJ_Paginated) { %*/
function _getBBoxPagination(bounds) {
  const augmentedBBox = _incrementBBox(
    bounds.getWest(),
    bounds.getEast(),
    bounds.getSouth(),
    bounds.getNorth()
  );

  const options = { params: {} };
  options.params.xmin = augmentedBBox.xmin;
  options.params.xmax = augmentedBBox.xmax;
  options.params.ymin = augmentedBBox.ymin;
  options.params.ymax = augmentedBBox.ymax;

  return options;
}

function _incrementBBox(xmin, xmax, ymin, ymax) {
  let incrementNS = (ymax - ymin) * 0.3;
  let incrementEW = (xmax - xmin) * 0.3;

  let augmentedXmin = xmin - incrementEW;
  let augmentedXmax = xmax + incrementEW;
  let augmentedYmin = ymin - incrementNS;
  let augmentedYmax = ymax + incrementNS;

  return {
    xmin: augmentedXmin,
    xmax: augmentedXmax,
    ymin: augmentedYmin,
    ymax: augmentedYmax,
  };
}
/*% } %*/

export {
  createWMSLayer,
  createGeoJSONLayer,
  createGeoPackageLayer,
  createWCSLayer,
  createGeoTIFFLayer,
  createShapefileLayer,
  getUniqueLayerId,
  createExternalGeoJSONLayer
  /*% if (feature.MV_MS_GJ_Paginated) { %*/, updateLayer/*% } %*/ };
/*% } %*/
