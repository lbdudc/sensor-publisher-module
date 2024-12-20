/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
package es.udc.lbd.gema.lps.component.gallery.dto;

import es.udc.lbd.gema.lps.component.gallery.model.domain.IGGallery;

public class IGGalleryDTO {

  private Long id;

  public IGGalleryDTO() {}

  public IGGalleryDTO(IGGallery igGallery) {
    this.id = igGallery.getId();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public IGGallery toIGGallery() {
    IGGallery igGallery = new IGGallery();
    igGallery.setId(this.getId());
    return igGallery;
  }
}
/*% } %*/