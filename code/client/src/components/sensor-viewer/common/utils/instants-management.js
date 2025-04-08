/*% if (feature.SensorViewer) {
const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
  return sensor.isMoving === true; }); %*/
  import {
    localDateToISOString,
    dateToDateArray,
    dateStringToDate,
  } from "@/common/conversion-utils";
  import { formatDateInverse } from "@/common/conversion-utils";
  import {
    AGGREGATIONS,
    FILTERS,
    TIME_INTERVALS,
  } from "@/components/sensor-viewer/common/utils/const.js";

  function fromInstantFilterToDate(filter) {
    if (typeof filter == "string") {
      return localDateToISOString(
        filter.split("-").map((val) => parseInt(val, 10))
      );
    } else if (Array.isArray(filter)) {
      return localDateToISOString(filter.map((val) => parseInt(val, 10)));
    } else return null;
  }

  function calculateStartEnd(store) {
    let start, end;
    /*% if(hasMovingSensors){ %*/
    if (
      store[AGGREGATIONS.TEMPORAL] == TIME_INTERVALS.RANGE &&
      store.INIT_DATE_FILTER &&
      store.END_DATE_FILTER
    ) {
      start = store.INIT_DATE_FILTER + "T00:00:00";
      end = store.END_DATE_FILTER + "T00:00:00";
      return {
        start: start,
        end: end,
      };
    }
    /*% } %*/
    const instantAux =
      store.INSTANT_FILTER != null
        ? store.INSTANT_FILTER
        : dateToDateArray(dateStringToDate(store.DATE_FILTER));
    switch (store[AGGREGATIONS.TEMPORAL]) {
      case TIME_INTERVALS.YEAR:
        start = _instantToYear(instantAux) + "-01-01T00:00:00";
        end = _instantToYear(instantAux) + "-12-31T23:59:59";
        break;
      case TIME_INTERVALS.MONTH:
        start = _instantToMonth(instantAux) + "-01T00:00:00";
        end =
          _instantToMonth(instantAux) +
          "-" +
          _getLastDayOfMonth(instantAux) +
          "T23:59:59";
        break;
      case TIME_INTERVALS.WEEK:
        start = _instantToDate(instantAux) + "T00:00:00";
        let newDate = new Date(_instantToDate(instantAux));
        newDate.setDate(newDate.getDate() + 7);
        newDate.toISOString().split("T")[0];
        end = newDate.toISOString().split("T")[0] + "T00:00:00";
        break;
      case TIME_INTERVALS.DAY:
        start = _instantToDate(instantAux) + "T00:00:00";
        end = _instantToDate(instantAux) + "T23:59:59";
        break;
      case TIME_INTERVALS.HOUR:
        const hour = instantAux[3] != null ? instantAux[3] : "00";
        start = _instantToDate(instantAux) + `T${hour}:00:00`;
        end = _instantToDate(instantAux) + `T${hour}:59:59`;
        break;
      case TIME_INTERVALS.NONE:
        start = fromInstantFilterToDate(instantAux);
        end = new Date(start);
        end.setTime(end.getTime() + 60 * 1000);
        end = end.toISOString();
        break;
      default:
        start = null;
        end = null;
        break;
    }
    return {
      start: start,
      end: end,
    };
  }

  function _instantToDateString(dateArray, position) {
    if (dateArray == null) return "";
    if (!Array.isArray(dateArray)) {
      dateArray = dateArray.split(",");
    }
    return [...dateArray]
      .slice(0, position)
      .map((el) => el.toString().padStart(2, "0"))
      .join("-");
  }

  function _instantToDate(dateArray) {
    return _instantToDateString(dateArray, 3);
  }

  function _instantToMonth(dateArray) {
    return _instantToDateString(dateArray, 2);
  }

  function _instantToYear(dateArray) {
    return _instantToDateString(dateArray, 1);
  }

  function _getLastDayOfMonth(dateArray) {
    if (!dateArray) return;
    return new Date(dateArray[0], dateArray[1], 0).getDate();
  }

  async function triggerInstantGetter(store) {
    return store.setSelector(
      FILTERS.DATE,
      formatDateInverse(new Date()),
      false,
      true
    );
  }

  export { calculateStartEnd, triggerInstantGetter, fromInstantFilterToDate };
  /*% } %*/
