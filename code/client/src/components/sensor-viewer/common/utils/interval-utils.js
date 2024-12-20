/*% if (feature.SensorViewer) { %*/
/**
 * This function is used to get the intervals for the legend
 * @param {*} min
 * @param {*} max
 * @param {*} array
 */
export function getIntervals(min, max, array) {
  let jump = (max - min) / array.length;
  for (let i = 0; i < array.length; i++) {
    array[i].minValue = i == 0 ? min : array[i - 1].maxValue;
    array[i].maxValue = i == array.length - 1 ? max : array[i].minValue + jump;
  }
}
/*% } %*/
