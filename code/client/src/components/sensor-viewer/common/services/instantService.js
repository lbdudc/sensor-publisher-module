/*% if (feature.SensorViewer) { %*/
import RepositoryFactory from "@/repositories/RepositoryFactory";
import {
  dateArrayToDate,
  formatDateInverse,
  dateStringToDate,
  dateToDateArray,
} from "@/common/conversion-utils";

const InstantRepository = RepositoryFactory.get("InstantProviderRepository");

const PAGE_SIZE = 5;

let controller = new AbortController();

async function getPages() {
  return [{}];
}

async function getInstants(params, url, totalInstantsPages, pageSize) {
  let pageFilter = params.PAGE_FILTER;
  !pageSize ? (pageSize = PAGE_SIZE) : (pageFilter = null);

  if (!!params.DATE_FILTER || !!params.YEAR_FILTER) {
    const options = {
      params: {
        page: pageFilter,
        size: pageSize,
      },
    };
    let date = params.DATE_FILTER
      ? dateStringToDate(params.DATE_FILTER + "")
      : new Date();
    date = formatDateInverse(date);
    if (
      params.TEMPORAL_AGGREGATION === "NONE" ||
      params.TEMPORAL_AGGREGATION === "DAY" ||
      params.TEMPORAL_AGGREGATION === "HOUR"
    ) {
      options.params.date = date; 
    } else if (params.TEMPORAL_AGGREGATION === "MONTH") {
      options.params.date = params.YEAR_FILTER + "-01-01";
    }

    const res = await InstantRepository.getInstants(
      options,
      params.TEMPORAL_AGGREGATION.toLowerCase(),
      url
    );

    totalInstantsPages.data = res.totalPages;
    if (params.TEMPORAL_AGGREGATION === "DAY") {
      res.content.forEach((el) => {
        el.params = { dayAsNumber: el.label };
        el.label = "calendar.dayOfWeek." + dateArrayToDate(el.value).getDay();
      });
    }
    return res.content.reverse();
  } else return [];
}

async function getYearItems(url) {
  controller.abort();
  // Enable cancel for request
  controller = new AbortController();
  const options = {
    signal: controller.signal,
  };

  return await InstantRepository.getYearItems(url, options);
}

async function getLastInstant(url) {
  return await InstantRepository.getLastInstant(url);
}

function getInstantsDefaultValue(items, store) {
  const isYearAgg = store["TEMPORAL_AGGREGATION"] == "YEAR";
  if (!isYearAgg || store["REAL_TIME_FLAG"] == true) {
    return items[items.length - 1]?.value;
  } else {
    if (items[items.length - 1]?.value) {
      const date = items[items.length - 1]?.value;
      date[3] = 0;
      date[4] = 0;
      return date;
    }
    const aux = dateToDateArray(dateStringToDate(store["DATE_FILTER"]));
    aux[3] = 0;
    aux[4] = 0;
    return aux;
  }
}
export default {
  PAGE_SIZE,
  getPages,
  getInstants,
  getYearItems,
  getLastInstant,
  getInstantsDefaultValue,
};
/*% } %*/
