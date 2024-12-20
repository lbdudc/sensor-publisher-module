/*% if (feature.SensorViewer) { %*/
import genericSpec from "./generic-spec.json";

/**
 * Returns the spec for the map viewer
 * @param {Array} spec The spec passed in
 * @param {Boolean} override If true, the generic spec is not used, return the spec as is
 * @returns {Array} The spec for the map viewer, containing the generic spec and the spec passed in
 */
const getSpec = (spec = null, override = null) => {
  if (spec === null) return genericSpec;

  if (override === true) return spec;

  return genericSpec.concat(spec);
};

export default getSpec;
/*% } %*/
