/*% if (feature.SensorViewer) { %*/
import { TEMPORAL } from "./types";
/*% } %*/

function compareLocalDates(localDate1, localDate2) {
  let date1Timestamp = new Date(localDate1).getTime(),
    date2Timestamp = new Date(localDate2).getTime();
  if (date1Timestamp > date2Timestamp) {
    return 1;
  }
  if (date1Timestamp < date2Timestamp) {
    return -1;
  }
  return 0;
}

/*% if (feature.SensorViewer) { %*/
function compareAggregationDates(date1, date2, aggregation) {
  switch (aggregation) {
    case TEMPORAL.YEAR:
      return date1.getFullYear() == date2.getFullYear();
    case TEMPORAL.DAY:
      return (
        date1.getMonth() == date2.getMonth() &&
        date1.getFullYear() == date2.getFullYear()
      );
    case TEMPORAL.NONE:
      return (
        date1.getDate() == date2.getDate() &&
        date1.getMonth() == date2.getMonth() &&
        date1.getFullYear() == date2.getFullYear()
      );
  }
}

function compareDatesByAggregation(date1, date2, aggregation) {
  switch (aggregation) {
    case TEMPORAL.YEAR:
      return date1?.getFullYear() == date2?.getFullYear();
    case TEMPORAL.MONTH:
      return (
        date1?.getMonth() == date2?.getMonth() &&
        date1?.getFullYear() == date2?.getFullYear()
      );
    default:
      return (
        date1?.getDate() == date2?.getDate() &&
        date1?.getMonth() == date2?.getMonth() &&
        date1?.getFullYear() == date2?.getFullYear()
      );
  }
}
/*% } %*/

export {
  compareLocalDates,
  /*% if (feature.SensorViewer) { %*/
  compareAggregationDates, compareDatesByAggregation
  /*% } %*/
};
