module.exports = {
  devServer: {
    host: "localhost",
    public: process.env.VUE_APP_PUBLIC_URL,
  },
  // ...other vue-cli plugin options...
  pwa: {
    // configure the workbox plugin
    workboxPluginMode: "InjectManifest",
    workboxOptions: {
      // swSrc is required in InjectManifest mode.
      // we specify a custom service-worker in this route
      swSrc: "src/service-worker.js"
      // ...other Workbox options...
    }
  },
  /*% if (feature.MV_LM_ExternalLayer) { %*/
  configureWebpack: {
    /*% if (feature.MV_LM_ExternalLayer) { %*/
    externals: ["better-sqlite3"], //FIXME fix for optional dependency warning in geopackage
    /*% } %*/
  },
  /*% } %*/
  /*% if (feature.MapViewer) { %*/
  chainWebpack: (config) => {
    /*% if (feature.MapViewer) { %*/
    // Used to import .sld files when creating a custom SLD style
    config.module
      .rule("raw")
      .test(/\.sld$/)
      .use("raw-loader")
      .loader("raw-loader")
      .end();
    /*% } %*/
  },
  /*% } %*/
  parallel: false,
};
