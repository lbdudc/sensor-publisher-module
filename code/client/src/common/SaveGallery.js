/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
import RepositoryFactory from "@/repositories/RepositoryFactory";
const ImageGalleryRepository = RepositoryFactory.get("ImageGalleryRepository");
const validExtensions = ".jpg,.jpeg,.png,.gif";

export default async function (images, gallery) {
  var promisesArray = [];
  for (const img of images) {
    var igImage = {};

    igImage.galeria = gallery;
    igImage.title = img.title;
    igImage.description = img.description;
    igImage.author = img.author;
    igImage.year = img.year;
    igImage.version = img.version;

    if (img.created) {
      igImage.path = img.file.name;

      let formData = new FormData();

      formData.append("file", img.file);
      formData.append("formats", validExtensions);
      var imageUploadPromise;
      await ImageGalleryRepository.save(igImage)
        .then(data => {
          imageUploadPromise = ImageGalleryRepository.uploadFile(
            gallery.id, data.id, formData
          );
        })
      promisesArray.push(imageUploadPromise);
    } else if (img.deleted) {
      promisesArray.push(ImageGalleryRepository.delete(img.id));
    } else if (img.modified) {
      //Assign inmutable properties
      igImage.id = img.id;
      if (img.file) {
        igImage.path = img.file.name;

        let formData = new FormData();

        formData.append("file", img.file);
        formData.append("formats", validExtensions);

        promisesArray.push(Promise.all([
          ImageGalleryRepository.replaceFile(gallery.id, img.id, formData),
          ImageGalleryRepository.save(igImage),
        ]));
      } else {
        igImage.path = img.path;
        promisesArray.push(ImageGalleryRepository.save(igImage));
      }
    }
  }
  return promisesArray;
}
/*% } %*/
