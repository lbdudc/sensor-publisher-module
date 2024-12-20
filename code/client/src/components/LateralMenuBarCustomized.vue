/*% if (feature.GUI_M_Custom) { %*/
<template>
    <v-navigation-drawer v-model="localDrawer" app>
    </v-navigation-drawer>
</template>

<script>
export default {
  name: "LateralMenuBar",
  props: {
    drawer: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {}
  },
  methods: {  
  },
  computed: {
    localDrawer: {
      get() {
        return this.drawer;
      },
      set(val) {
        this.$emit("drawer_changed", val);
      },
    },
  },
};
</script>
 /*% } %*/