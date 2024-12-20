/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
package es.udc.lbd.gema.lps.component.gallery.model.repository;

import es.udc.lbd.gema.lps.component.gallery.model.domain.IGImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface IGImageRepository
  extends JpaRepository<IGImage, Long>,
  JpaSpecificationExecutor<IGImage>,
  PagingAndSortingRepository<IGImage, Long> {

  IGImage save(IGImage image);

  IGImage getReferenceById(Long pk);
}
/*% } %*/
