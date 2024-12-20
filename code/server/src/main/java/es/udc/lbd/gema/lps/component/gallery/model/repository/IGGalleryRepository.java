/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
package es.udc.lbd.gema.lps.component.gallery.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import es.udc.lbd.gema.lps.component.gallery.model.domain.IGGallery;

public interface IGGalleryRepository extends JpaRepository<IGGallery, Long> {

    IGGallery save(IGGallery gallery);

    IGGallery getReferenceById(Long pk);

}
/*% } %*/
