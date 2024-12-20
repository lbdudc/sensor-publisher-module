import Vue from "vue";
import VueI18n from "vue-i18n";
import * as lang from "vuetify/lib/locale";

Vue.use(VueI18n);

/*%
  data.basicData.languages.forEach(function(lang) {
%*/
const locale/*%= normalize(lang, true) %*/ = require.context("../locale//*%= normalize(lang) %*/", true, /\.json$/);
/*%
  });
%*/

const messages = {
/*%
  data.basicData.languages.forEach(function(lang) {
%*/
  /*%= lang.toLocaleUpperCase() %*/: {},
/*%
  });
%*/
};

// Set translations from local files
/*%
  data.basicData.languages.forEach(function(lang) {
%*/
locale/*%= normalize(lang, true) %*/.keys().forEach(filename => {
  Object.keys(locale/*%= normalize(lang, true) %*/(filename)).forEach(key => {
    messages./*%= lang.toLocaleUpperCase() %*/[key] = locale/*%= normalize(lang, true) %*/(filename)[key];
  });
});
/*%
  });
%*/

function deepMergeTranslations(target, source) {
  Object.keys(source).forEach((key) => {
    if (typeof source[key] === "object") {
      if (!target[key]) target[key] = {};
      deepMergeTranslations(target[key], source[key]);
    } else {
      target[key] = target[key] ? target[key] : source[key];
    }
  });
}

// Set default Vuetify i18n value if not translated
Object.keys(messages).forEach((locale) => {
  if (lang[locale.toLowerCase()]) {
    deepMergeTranslations(
      messages[locale].$vuetify,
      lang[locale.toLowerCase()]
    );
  }
});


const i18n = new VueI18n({
  locale: localStorage.getItem("lang") || "/*%= data.basicData.languages[0].toLocaleUpperCase() %*/", // set locale
  fallbackLocale: "/*%= data.basicData.languages[0].toLocaleUpperCase() %*/", // set fallback locale
  messages // set locale messages
});

export default i18n;
