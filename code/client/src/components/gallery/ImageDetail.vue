/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
<template>
  <v-dialog :value="dialog" @click:outside="close" scrollable>
    <v-card>
      <v-card-title>
        <v-row align="center" no-gutters class="white--text">
          <v-col cols="9">
            <span class="headline">{{ img.title }}</span>
          </v-col>
          <v-col cols="3" class="text-right">
            <v-btn text @click="close" class="white--text"
              ><v-icon>mdi-close</v-icon></v-btn
            >
          </v-col>
        </v-row>
      </v-card-title>
      <v-card-text>
        <v-row align="center" dense>
          <v-col cols="1" align="center">
            <v-btn :disabled="selectedIndex <= 0" @click="prev()">
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
          </v-col>
          <v-col cols="10" align="center">
            <custom-image
              :img="img"
              :gallery="img.galeria"
              max-height="600px"
              contain
              class="white lighten-2 image-container"
            >
            </custom-image>
          </v-col>
          <v-col cols="1" align="center">
            <v-btn
              :disabled="selectedIndex >= images.length - 1"
              @click="next()"
            >
              <v-icon>mdi-arrow-right</v-icon>
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
import CustomImage from "./CustomImage";

export default {
  props: {
    dialog: {
      type: Boolean,
      required: true
    },
    images: {
      type: Array,
      required: true
    },
    index: {
      type: Number,
      required: false,
      default: 0
    }
  },
  components: { CustomImage },
  data() {
    return {
      selectedIndex: null
    };
  },
  computed: {
    img() {
      return this.images[this.selectedIndex];
    }
  },
  created() {
    this.selectedIndex = parseInt(this.index.toString());
    window.addEventListener("keydown", this.keyPressHandler);
  },
  destroyed() {
    window.removeEventListener("keydown", this.keyPressHandler);
  },
  methods: {
    next() {
      if (this.selectedIndex < this.images.length - 1) this.selectedIndex++;
    },
    prev() {
      if (this.selectedIndex > 0) this.selectedIndex--;
    },
    keyPressHandler(event) {
      switch (event.key) {
        case "ArrowLeft":
          this.prev();
          break;
        case "ArrowRight":
          this.next();
          break;
      }
    },
    close() {
      this.$emit("exit", this.selectedIndex);
    }
  }
};
</script>

<style scoped>
.v-card__title {
  background-color: #1976d2;
}
.v-card__actions {
  background-color: #1976d2;
}
</style>
/*% } %*/
