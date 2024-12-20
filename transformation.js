module.exports = modelTransformation;
// OJO este fichero debe ser el mismo que services/transformation.js

var geographicClasses = [
  "Point",
  "MultiPoint",
  "LineString",
  "MultiLineString",
  "Polygon",
  "MultiPolygon",
  "Geometry",
];

function modelTransformation(input) {
  if (
    input.data &&
    input.data.dataModel &&
    input.data.dataModel.entities &&
    input.data.forms &&
    input.data.lists
  )
    return input;
  let ret = {};
  ret.features = input.features;
  ret.data = {};
  ret.data.basicData = basicData(input.basicData);
  if (input.data?.dataModel) {
    // Generado por gisdsl, que ya incluye el modelo de datos en CLI
    ret.data.dataModel = input.data.dataModel;
  } else {
    // Hecho con la interfaz gráfica - spec GUI
    ret.data.dataModel = dataModel(input.dataModel);
  }
  ret.data.forms = [];
  ret.data.gui = gui(ret.features, input.basicData.name);
  ret.data.lists = [];
  ret.data.menus = menus(ret, input);
  ret.data.mapViewer = input.mapViewer;
  ret.data.dataWarehouse = input.dataWarehouse;
  ret.data.statics = statics(input.statics);
  return ret;
}

function normalize(str, upper) {
  return str
    .normalize("NFKD")
    .replace(/[\u0300-\u036F]/g, "")
    .replace(/[-_]+/g, " ")
    .replace(/[^\w\s]/gi, "")
    .replace(/\s(.)/g, function ($1) {
      return $1.toUpperCase();
    })
    .replace(/\s/g, "")
    .replace(/^(.)/, function ($1) {
      return upper ? $1.toUpperCase() : $1.toLowerCase();
    });
}

function basicData(input) {
  return {
    index: {
      component: "STATIC",
      view: "welcome",
    },
    languages:
      input.languages && input.languages.length > 0
        ? input.languages
        : ["en", "es", "gl"],
    name: input.name,
    packageInfo: {
      artifact: normalize(input.name).toLowerCase(),
      group: "es.udc.lbd.gisspl",
    },
    database: input.database,
    extra: input.extra,
    SRID: input.SRID,
    version: input.version,
  };
}

function enums(input) {
  return input.map((e) => {
    return {
      name: normalize(e.name, true),
      values: e.values,
    };
  });
}

function entities(input) {
  return input.map((e) => {
    return {
      name: normalize(e.name, true),
      properties: properties(e),
      displayString: "$" + _getDisplayString(e, input),
      superclass: e.superclass?.name,
      abstract: e.abstract,
      traceable: e.traceable,
      // displayString: '$id'
    };
  });
}

function properties(entity) {
  const ret = [];

  entity.properties.forEach((p) => {
    if (p.name === "id") {
      ret.push({
        name: "id",
        class: "Long (autoinc)",
        pk: true,
        required: true,
        unique: true,
      });
    } else {
      ret.push({
        name: p.name,
        class: normalize(p.class, true),
        required: p.required,
        unique: p.unique,
        patternType: p.patternType,
        pattern: p.pattern,
        min: p.min,
        max: p.max,
      });
    }
  });

  return ret;
}

function relationships(ret, input) {
  const rels = input.cells.filter((o) => o.type === "custom.Association");
  let source, target;

  rels.forEach((r) => {
    source = findEntityByName(
      normalize(findEntityById(r.source.id, input.cells).name, true),
      ret
    );
    target = findEntityByName(
      normalize(findEntityById(r.target.id, input.cells).name, true),
      ret
    );

    source.properties.push({
      name: r.sourceOpts.label || normalize(target.name),
      class: target.name,
      owner: _getRelationshipOwner(r, r.source.id),
      bidirectional: r.targetOpts.label || normalize(source.name),
      multiple: multiple(r.sourceOpts.multiplicity),
      required: required(r.sourceOpts.multiplicity),
    });

    target.properties.push({
      name: r.targetOpts.label || normalize(source.name),
      class: source.name,
      owner: _getRelationshipOwner(r, r.target.id),
      bidirectional: r.sourceOpts.label || normalize(target.name),
      multiple: multiple(r.targetOpts.multiplicity),
      required: required(r.targetOpts.multiplicity),
    });
  });

  return ret;
}

function _getDisplayString(entity, entities) {
  const displayString = entity.properties.find((e) => e.displayString);

  if (!displayString && entity.superclass) {
    return _getDisplayString(
      entities.find((e) => e.name === entity.superclass.name),
      entities
    );
  } else if (displayString) {
    return displayString.name;
  } else {
    return "id";
  }
}

function _getRelationshipOwner(rel, entityId) {
  let isSource = rel.source.id === entityId;
  let relType =
    (multiple(rel.targetOpts.multiplicity) ? "Many" : "One") +
    "To" +
    (multiple(rel.sourceOpts.multiplicity) ? "Many" : "One");
  /*
   * in OneToMany/ManyToOne relationships Many side is owner
   * for rest of cases source side is owner by default
   */
  if (relType === "OneToMany") return !isSource;
  else return isSource;
}

function findEntityById(id, col) {
  return col.find((o) => o.id === id);
}

function findEntityByName(name, col) {
  return col.find((o) => o.name === name);
}

function multiple(multiplicity) {
  return (
    ["1", "0..1"].find(function (a) {
      return a === multiplicity;
    }) == null
  );
}

function required() {
  return false;
  // return [ '1' ].find(function(a) {
  //   return a == multiplicity;
  // }) != null;
}

function dataModel(input) {
  let ret = {};

  ret.enums = enums(input.cells.filter((o) => o.type === "custom.Enum"));
  ret.entities = entities(input.cells.filter((o) => o.type === "custom.Class"));
  relationships(ret.entities, input);

  return ret;
}

function statics(input) {
  return input;
}

function gui(features, name) {
  const ret = {
    settings: {
      font: {
        family: "Arial",
        size: "14px",
      },
      colorset: ["#fff", "#eee", "#777", "#555", "#577492", "#333", "#222"],
      header: {
        type: "Text",
        text: name,
      },
    },
  };

  if (features.indexOf("GUI_M_Top") !== -1) {
    ret.design = "1";
  } else if (features.indexOf("GUI_M_Left") !== -1) {
    ret.design = "5";
  } else if (features.indexOf("GUI_M_Right") !== -1) {
    ret.design = "6";
  } else {
    ret.design = "4";
  }
  if (
    features.indexOf("GUI_M_Top") !== -1 &&
    features.indexOf("GUI_M_Left") !== -1
  ) {
    ret.design = "2";
  }
  if (
    features.indexOf("GUI_M_Top") !== -1 &&
    features.indexOf("GUI_M_Right") !== -1
  ) {
    ret.design = "3";
  }

  return ret;
}

function menus(product) {
  const ret = [];

  switch (product.data.gui.design) {
    case "1":
      ret.push({ id: "top", elements: [] });
      break;
    case "2":
      ret.push({ id: "top", elements: [] });
      ret.push({ id: "left", elements: [] });
      break;
    case "3":
      ret.push({ id: "top", elements: [] });
      ret.push({ id: "right", elements: [] });
      break;
    case "4":
      return [];
    case "5":
      ret.push({ id: "left", elements: [] });
      break;
    case "6":
      ret.push({ id: "right", elements: [] });
      break;
  }

  ret[0].elements.push(listComponents(product.features));

  return ret;
}

function listComponents(features) {
  return {
    id: "Components",
    type: "MENU",
    elements: features
      .filter((f) => featureMenu(f, features))
      .map((f) => featureMenu(f, features)),
    access: {
      admin: true,
      logged: true,
    },
  };
}

function listMaps(entities, features) {
  return {
    id: "Maps",
    type: "MENU",
    elements: entities
      .filter((e) =>
        e.properties.find((p) => geographicClasses.indexOf(p.class) !== -1)
      )
      .map((e) => {
        return {
          id: e.name + " Map",
          type: "VIEW",
          view: {
            component: "MAP",
            view:
              "mapViewer({ layers: '" +
              e.properties
                .filter((p) => geographicClasses.indexOf(p.class) !== -1)
                .map((p) => normalize(e.name) + "-" + normalize(p.name)) +
              "'" + " })",
          },
        };
      }),
    access: {
      admin: true,
      logged: true,
    },
  };
}

function featureMenu(feature, features) {
  switch (feature) {
    case "DM_DI_DF_CSV":
      return view("STANDARD_DATA_IMPORT", "CSV Importer");
    case "MapViewer":
      var theView = "mapViewer";
      return {
        id: "Map viewer",
        type: "VIEW",
        view: {
          component: "MAP",
          view: theView,
        },
      };
  }
  return null;
}

function view(comp, view) {
  return {
    id: view,
    type: "VIEW",
    view: {
      component: comp,
      view: view,
    },
  };
}
