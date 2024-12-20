/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
import { HTTP } from "@/common/http-common";
import Logger from "js-logger";

const logger = Logger.get("logger");
const RESOURCE_NAME = "images";
const UPLOAD_RESOURCE_NAME = "upload/gallery";
const DOWNLOAD_RESOURCE_NAME = "download/gallery";

export default {
  async getGalleryImages(galleryId) {
    try {
      return (await HTTP.get(RESOURCE_NAME, {
        params: {
          galleryId: galleryId,
        },
      })).data;
    } catch (err) {
      logger.error("Error getting gallery images", galleryId);
      throw err;
    }
  },

  async save(image) {
    if (image.id) {
      try {
        return (await HTTP.put(`${RESOURCE_NAME}/${image.id}`, image)).data;
      } catch (err) {
        logger.error("Error updating image", image);
        throw err;
      }
    } else {
      try {
        return (await HTTP.post(`${RESOURCE_NAME}`, image)).data;
      } catch (err) {
        logger.error("Error saving image", image);
        throw err;
      }
    }
  },

  async delete(id) {
    try {
      return (await HTTP.delete(`${RESOURCE_NAME}/${id}`));
    } catch (err) {
      logger.error("Error deleting image with id " + id);
      throw err;
    }
  },

  async downloadFile(galleryId, imgId, version) {
    try {
      return (await HTTP.get(
        `${DOWNLOAD_RESOURCE_NAME}/${galleryId}/${imgId}?version=${version}`,
        {
          cache: true,
          responseType: "blob",
        }
      )).data;
    } catch (err) {
      logger.error(
        `Error downloading file from gallery ${galleryId}, imageId ${imgId} and version ${version}`
      );
      throw err;
    }
  },

  async uploadFile(galleryId, imageId, formData) {
    try {
      return (await HTTP.post(
        `${UPLOAD_RESOURCE_NAME}/${galleryId}/${imageId}`,
        formData
      )).data;
    } catch (err) {
      logger.error(
        `Error uploading file for gallery ${galleryId}, imageId ${imageId}`,
        formData
      );
      throw err;
    }
  },

  async replaceFile(galleryId, imageId, formData) {
    try {
      return (await HTTP.put(
        `${UPLOAD_RESOURCE_NAME}/${galleryId}/${imageId}`,
        formData
      )).data;
    } catch (err) {
      logger.error(
        `Error replacing file for gallery ${galleryId}, imageId ${imageId}`,
        formData
      );
      throw err;
    }
  },
};
/*% } %*/
