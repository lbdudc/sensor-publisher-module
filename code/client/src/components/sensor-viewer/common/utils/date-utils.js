/*% if (feature.SensorViewer) { %*/
function getNextDay(dateString) {
  const newDate = new Date(dateString);
  newDate.setDate(newDate.getDate() + 1);
  return newDate.toISOString().split("T")[0];
}

function getPrevDay(dateString) {
  const newDate = new Date(dateString);
  newDate.setDate(newDate.getDate() - 1);
  return newDate.toISOString().split("T")[0];
}

function getPrevMonth(dateString) {
  var [year, month] = dateString.split("-");
  month = parseInt(month) - 1;
  if (month === 0) {
    year--;
    month = 12;
  }
  month = month.toString().padStart(2, "0");
  return year + "-" + month;
}

function getNextMonth(dateString) {
  var [year, month] = dateString.split("-");
  month = parseInt(month) + 1;
  if (month === 13) {
    year++;
    month = 1;
  }
  month = month.toString().padStart(2, "0");
  return year + "-" + month;
}

function formatDateNowTimeZone() {
  const dateTimeZone = new Date().toLocaleString("en-GB", {
    timeZone: "Europe/Madrid",
  });
  return (
    dateTimeZone.substring(6, 10) +
    "-" +
    dateTimeZone.substring(3, 5) +
    "-" +
    dateTimeZone.substring(0, 2)
  );
}

export {
  getNextDay,
  getNextMonth,
  getPrevDay,
  getPrevMonth,
  formatDateNowTimeZone,
};
/*% } %*/
