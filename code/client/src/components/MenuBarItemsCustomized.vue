/*% if ( feature.GUI_M_Custom && feature.GUI_M_Top) { %*/
<template>
  <v-container>
  </v-container>
</template>

<script>
export default {
  name: "MenuBarItems",
  data() {
    return {}
  },
  mounted() {
    window.addEventListener("load", () => this.$emit("bar-items-loaded"));
  },
  beforeDestroy() {
    window.removeEventListener("load", this.resizeHandler);
  },
  methods: {
  }
};
</script>
 /*% } %*/
