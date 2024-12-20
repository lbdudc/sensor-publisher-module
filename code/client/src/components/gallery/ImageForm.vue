/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
<template>
  <v-dialog persistent :value="dialog" scrollable>
    <v-card>
      <v-card-title>
        <v-row align="center" no-gutters class="white--text">
          <v-col class="text-center">
            <span class="headline">
              {{ $t("gallery.image_form.form") }}
            </span>
          </v-col>
        </v-row>
      </v-card-title>
      <v-card-text class="mt-4 pb-0">
        <v-form v-model="validForm">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field
                dense
                v-model="formImage.title"
                type="text"
                :label="$t('gallery.image_form.title')"
              ></v-text-field>

              <v-text-field
                dense
                v-model="formImage.author"
                type="text"
                :label="$t('gallery.image_form.author')"
              ></v-text-field>

              <number-field
                v-model="formImage.year"
                :label="$t('gallery.image_form.year')"
              ></number-field>

              <v-textarea
                v-model="formImage.description"
                class="pt-2"
                :label="$t('gallery.image_form.description')"
              ></v-textarea>
            </v-col>

            <v-col cols="12" md="6">
              <v-row no-gutters>
                <v-col cols="12">
                  <b>{{ $t("gallery.image_form.file") }}:</b>
                </v-col>
                <v-col cols="12">
                  <v-file-input
                    v-model="formImage.file"
                    dense
                    accept=".jpg,.jpeg,.png,.gif"
                    prepend-icon="mdi-camera"
                    chips
                    @change="onFileChange"
                    :rules="[
                      v =>
                        !!(v || formImage.path) ||
                        $t('gallery.image_form.required')
                    ]"
                  ></v-file-input>
                </v-col>
                <v-col cols="12">
                  <custom-image 
                    v-if="formImage.path" 
                    :img="formImage"
                    :gallery="gallery"
                    contain
                    max-height="300"
                  ></custom-image>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-row align="center" justify="space-between" no-gutters>
          <v-col class="text-right">
            <v-btn @click="$emit('cancel')">
              {{ $t("cancel") }}
            </v-btn>
            <v-btn class="mx-4 success" @click="save()" :disabled="!validForm">
              {{ $t("save") }}
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import NumberField from "@/components/number-field/NumberField.vue";
import CustomImage from "./CustomImage";

export default {
  props: {
    dialog: {
      type: Boolean,
      required: true
    },
    name: {
      type: String,
      required: false
    },
    image: {
      required: false,
      default: undefined
    },
    create: {
      type: Boolean,
      required: false,
      default: false
    },
    gallery: {
      type: Object,
      required: false,
      default: null
    }
  },
  data() {
    return {
      validForm: false,
      formImage: {}
    };
  },
  components: { NumberField, CustomImage },
  computed: {
  },
  watch: {
    image: function(newVal) {
      this.formImage = JSON.parse(JSON.stringify(this.image));
      if (newVal.file) this.formImage.file = newVal.file;
    }
  },
  created() {
    if (!this.create && this.image) {
      this.formImage = JSON.parse(JSON.stringify(this.image));
      if (this.image.file) this.formImage.file = this.image.file;
    }
  },
  methods: {
    save() {
      this.$emit("submit", this.formImage);
    },
    onFileChange(file) {
      if (file) this.formImage.path = URL.createObjectURL(file);
      else this.formImage.path = null;
    }
  }
};
</script>

<style scoped>
.v-card__title {
  background-color: #1976d2;
}
.control-box {
  width: 100%;
}
</style>
/*% } %*/
