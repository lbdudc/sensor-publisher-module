/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
<template>
  <v-img
    :src="link"
    :aspect-ratio="aspectRatio"
    :height="height"
    :max-height="maxHeight"
    :contain="contain"
    @click="$emit('click')"
  >
    <template v-slot:placeholder>
      <v-row
        class="fill-height ma-0"
        align="center"
        justify="center"
        v-if="!error"
      >
        <v-progress-circular
          indeterminate
          color="lighten-5"
        ></v-progress-circular>
      </v-row>
      <v-row
        class="fill-height ma-0"
        align="center"
        justify="center"
        v-if="error"
      >
        <v-icon> error </v-icon>
      </v-row>
    </template>
  </v-img>
</template>

<script>
import RepositoryFactory from "@/repositories/RepositoryFactory.js";
const ImageGalleryRepository = RepositoryFactory.get("ImageGalleryRepository");
export default {
  props: {
    img: {
      required: true
    },
    gallery: {
      required: true
    },
    aspectRatio: {},
    height: {},
    maxHeight: {},
    contain: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      link: '',
      error: false
    }
  },
  watch: {
    img: {
      handler: "src",
      deep: true
    }
  },
  created() {
    this.src();
  },
  methods: {
    async src() {
      if (this.img.file) {
        this.link = this.img.path;
      } else {
        this.link = await ImageGalleryRepository.downloadFile(
          this.gallery.id,
          this.img.id,
          this.img.version
        )
          .then((data) => {
            return URL.createObjectURL(data);
          })
          .catch(() => {
            this.error = true;
            return null;
          });
      }
    }
  }
}
</script>
/*% } %*/
