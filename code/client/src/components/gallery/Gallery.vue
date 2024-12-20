/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
<template>
  <v-container class="image-container">
    <v-row>
      <v-col cols="6">
        <span v-if="images.length < 1">
          {{ $t("gallery.empty") }}
        </span>
      </v-col>
      <v-col v-if="isEdit" cols="6" class="text-right">
        <v-btn small @click="openCreateDialog">
          {{ $t("gallery.add") }}
        </v-btn>
      </v-col>
    </v-row>
    <v-row
      v-if="images.length >= 1"
      class="gallery-main d-flex child-flex"
      @mouseover="isShowingOverlayCard = true"
      @mouseleave="isShowingOverlayCard = false"
    >
      <v-col cols="3">
        <div
          class="gallery-header-info"
          v-if="
            images[imageSelectedIndex].title ||
              images[imageSelectedIndex].description
          "
        >
          <div class="title" v-if="images[imageSelectedIndex].title">
            {{ images[imageSelectedIndex].title }}
          </div>
          <div
            class="description"
            v-if="images[imageSelectedIndex].description"
          >
            {{ images[imageSelectedIndex].description }}
          </div>
        </div>
      </v-col>
      <v-col cols="6" class="pt-0 pr-0 pl-0 pb-0">
        <custom-image
          @click="openDialog"
          :img="imageSelected"
          :gallery="gallery"
          class="black lighten-2 gallery-main-img"
          contain
          max-height="300px"
        >
        </custom-image>
      </v-col>
      <v-col cols="3">
        <div
          class="gallery-footer-info"
          v-if="
            images[imageSelectedIndex].author || images[imageSelectedIndex].year
          "
        >
          <span>{{ getImageFooter(images[imageSelectedIndex]) }}</span>
        </div>
      </v-col>
      <div
        class="overlay-card"
        v-if="isEdit"
        v-show="isShowingOverlayCard && imageSelected && !imageSelected.deleted"
      >
        <div class="buttons">
          <v-btn class="warning overlay-card-button" @click="openEditDialog">
            {{ $t("gallery.update") }}
          </v-btn>
          <v-btn class="error overlay-card-button" @click="deleteItem()">
            {{ $t("gallery.delete") }}
          </v-btn>
        </div>
      </div>
      <div
        class="overlay-card"
        v-if="isEdit"
        v-show="isShowingOverlayCard && imageSelected && imageSelected.deleted"
      >
        <div class="buttons">
          <v-chip color="red" class="mb-2 white--text" small>
            <v-icon dark>mdi-delete</v-icon>
            <span class="white--text">
              {{ $t("gallery.image-to-delete") }}
            </span>
          </v-chip>
          <v-btn class="warning overlay-card-button" @click="undoDelete">
            {{ $t("gallery.undo") }}
          </v-btn>
        </div>
      </div>
    </v-row>
    <v-row v-if="images.length >= 1">
      <v-col cols="2" md="1" class="gallery-arrow text-center">
        <v-btn icon v-show="imageSelectedIndex > 0" @click="prev">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
      </v-col>
      <v-col cols="8" md="10" class="gallery-mini-container text-center">
        <v-col
          cols="6"
          sm="4"
          md="3"
          lg="2"
          v-for="img in images"
          :key="img.id"
          class="gallery-mini"
        >
          <custom-image
            :img="img"
            :gallery="gallery"
            aspect-ratio="1.7778"
            class="white lighten-2 gallery-mini-img"
            height="70px"
            contain
            @click="selectImage(img)"
          >
          </custom-image>
        </v-col>
      </v-col>
      <v-col cols="2" md="1" class="gallery-arrow text-center">
        <v-btn
          icon
          v-show="imageSelectedIndex < images.length - 1"
          @click="next"
        >
          <v-icon>mdi-arrow-right</v-icon>
        </v-btn>
      </v-col>
    </v-row>
    <image-form
      v-if="isEdit && dialog"
      :dialog="dialog"
      :image="imageSelected"
      :create="creation"
      :gallery="gallery"
      @submit="manageEdition"
      @cancel="dialog = false"
    ></image-form>
    <image-detail
      v-if="!isEdit && dialog"
      :dialog="dialog"
      :images="images"
      :index="imageSelectedIndex"
      @exit="closeDetailDialog"
    ></image-detail>
  </v-container>
</template>

<script>
import ImageDetail from "./ImageDetail";
import ImageForm from "./ImageForm";
import CustomImage from "./CustomImage";
import RepositoryFactory from "@/repositories/RepositoryFactory.js";
const ImageGalleryRepository = RepositoryFactory.get("ImageGalleryRepository");

export default {
  props: {
    gallery: {
      type: Object,
      required: false
    },
    isEdit: {
      type: Boolean,
      required: false,
      default: false
    },
    galleryName: {
      type: String,
      required: false,
      default: undefined
    }
  },
  components: { ImageDetail, ImageForm, CustomImage },
  data() {
    return {
      images: [],
      imageSelectedIndex: 0,
      dialog: false,
      isShowingOverlayCard: false,
      creation: false
    };
  },
  computed: {
    imageSelected() {
      return this.images[this.imageSelectedIndex];
    }
  },
  mounted() {
    if (this.gallery)
      ImageGalleryRepository.getGalleryImages(this.gallery.id)
        .then(data => this.images = data.content);
  },
  methods: {
    closeDialog() {
      this.dialog = false;
    },
    closeDetailDialog(index) {
      this.imageSelectedIndex = index;
      this.dialog = false;
    },
    openDialog() {
      this.dialog = true;
      this.isShowingOverlayCard = false;
    },
    prev() {
      this.imageSelectedIndex--;
    },
    next() {
      this.imageSelectedIndex++;
    },
    getImageFooter(image) {
      if (image.author && image.year) return image.author + ", " + image.year;
      return image.author || image.year;
    },
    selectImage(image) {
      this.imageSelectedIndex = this.images.indexOf(image);
    },
    openCreateDialog() {
      this.creation = true;
      this.openDialog();
    },
    openEditDialog() {
      this.creation = false;
      this.openDialog();
    },
    manageEdition(newImage) {
      this.dialog = false;
      if (this.creation) {
        newImage.created = true;
        this.images.push(newImage);
        this.imageSelectedIndex = this.images.length - 1;
        this.creation = false;
      } else {
        if (!newImage.created) {
          newImage.modified = true;
        }
        this.images.splice(this.imageSelectedIndex, 1, newImage);
      }
      this.$emit("changed", {
        images: this.images,
        gallery: this.galleryName
      });
    },
    deleteItem() {
      if (this.images[this.imageSelectedIndex].created) {
        this.images.splice(this.imageSelectedIndex, 1);
        this.isShowingOverlayCard = false;
        this.$emit("changed", {
          images: this.images,
          gallery: this.galleryName
        });
      } else {
        this.images[this.imageSelectedIndex].deleted = true;
        this.isShowingOverlayCard = false;
        this.$emit("changed", {
          images: this.images,
          gallery: this.galleryName
        });
      }
    },
    undoDelete() {
      this.images[this.imageSelectedIndex].deleted = false;
      this.isShowingOverlayCard = false;
      this.$emit("changed", {
        images: this.images,
        gallery: this.galleryName
      });
    }
  }
};
</script>

<style scoped>
.image-container {
  height: 100%;
}

.gallery-footer-info {
  position: absolute;
  background-color: black;
  opacity: 0.7;
  right: 25px;
  bottom: 25px;
  padding-left: 10px;
  padding-right: 10px;
  border: solid 1px white;
}

.gallery-footer-info {
  color: white;
}

.gallery-header-info {
  background-color: black;
  opacity: 0.7;
  border: solid 1px white;
  text-align: left;
  overflow-y: auto;
}

.gallery-header-info .title {
  font-size: larger;
}

.gallery-header-info .description {
  font-size: smaller;
}

.gallery-header-info {
  color: white;
  padding: inherit;
}

.gallery-main {
  position: relative;
  background-color: black;
  text-align: center;
  height: 300px;
  margin-top: 20px;
  margin-bottom: 20px;
  padding-left: 0px;
  padding-right: 0px;
}

.gallery-main-img {
  display: block;
  margin: auto;
}

.gallery-arrow {
  font-size: 24px;
  padding: 0px;
  text-align: center;
}

@media only screen and (max-width: 576px) {
  .gallery-arrow i {
    vertical-align: top;
  }
}

.gallery-mini-container {
  padding: 0px;
  overflow-x: auto;
}

.gallery-mini {
  padding: 0px;
  display: inline-flex;
}

.gallery-mini-img {
  height: 70px;
  width: 100%;
  object-fit: cover;
}

@media only screen and (max-width: 576px) {
  .gallery-mini-img {
    height: 40px;
  }
}

.gallery-mini-img:hover {
  cursor: pointer;
}

.gallery-mini >>> .v-responsive__sizer {
  padding-bottom: 0% !important;
}

.overlay-card {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(54, 54, 54, 0.4);
  z-index: 1000;
}

.overlay-card .buttons {
  margin-top: 100px;
}

.overlay-card-button {
  display: block;
  width: 100px;
  margin-bottom: 10px;
  margin-left: auto;
  margin-right: auto;
}

.deleted-message {
  color: white;
}

.v-image:hover {
  cursor: pointer;
}
</style>
/*% } %*/
