/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
package es.udc.lbd.gema.lps.component.gallery.model.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ig_gallery")
public class IGGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "galleryGen")
    @SequenceGenerator(name = "galleryGen", sequenceName = "ig_gallery_id_seq", allocationSize = 1)
    private Long id;

    public IGGallery() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
/*% } %*/
