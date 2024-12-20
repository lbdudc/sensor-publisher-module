/*% if (checkAnyEntityContainsGalleryProperties(data)) { %*/
package es.udc.lbd.gema.lps.component.gallery;

import es.udc.lbd.gema.lps.component.gallery.model.domain.IGGallery;
import es.udc.lbd.gema.lps.component.gallery.model.domain.IGImage;
import es.udc.lbd.gema.lps.component.gallery.model.repository.IGGalleryRepository;
import es.udc.lbd.gema.lps.component.gallery.model.repository.IGImageRepository;
import es.udc.lbd.gema.lps.web.rest.custom.ValidationErrorUtils;
import es.udc.lbd.gema.lps.web.rest.util.specification_utils.SpecificationUtil;
import es.udc.lbd.gema.lps.web.rest.util.HeaderUtil;
import es.udc.lbd.gema.lps.web.rest.util.PaginationUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(IGImageResource.IGIMAGE_RESOURCE_URL)
public class IGImageResource {
    public final static String IGIMAGE_RESOURCE_URL = "/api/images";

    private static final Logger logger = LoggerFactory.getLogger(IGImageResource.class);

    @Inject
    private IGImageRepository iGImageRepository;

    @Inject
    private IGGalleryRepository iggalleryRepository;

    /**
     * Get entities in pages<br>
     *
     * If no <code>pageNum</code> is provided, then all results will be returned in one page
     *
     * @param pageable Contains all information about number page, items per page, order, ...
     * @param filters Static filters to apply
     * @param galleryId Id of the gallery from which you want to recover the images
     * @return Paginated entities
     */
    @GetMapping
    public ResponseEntity<Page<IGImage>> getAll(
      @PageableDefault(page = 0, size = 100000, sort = "id") Pageable pageable,
      @RequestParam(value = "filters", required = false) List<String> filters,
      @RequestParam(value = "search", required = false) String search,
      @Spec(path = "galeria.id", params = "galleryId", spec = Equal.class)
        Specification gallerySpec) {

      Page<IGImage> page;

      if (search == null || search.isEmpty()) {
        page =
          iGImageRepository.findAll(
            SpecificationUtil.getSpecificationFromFilters(filters, false).and(gallerySpec),
            pageable);
      } else {
        page =
          iGImageRepository.findAll(
            IGImageSpecification.searchAll(search).and(gallerySpec), pageable);
      }

      HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, IGIMAGE_RESOURCE_URL);
      return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<IGImage> get(@PathVariable Long id) {

        IGImage iGImage = iGImageRepository.getReferenceById(id);
        if (iGImage !=null){
            return new ResponseEntity<>(iGImage, HttpStatus.OK);
        } else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<?> create ( @Valid @RequestBody IGImage iGImage, Errors errors) throws URISyntaxException {

        if (iGImage.getId() != null){
            return ResponseEntity.badRequest().headers(HeaderUtil.createError("iGImage.error.id-exists", null)).body(null);
        }
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationErrorUtils.getValidationErrors(errors));
        }
        if (iGImage.getGaleria() == null) {
            IGGallery gallery = iggalleryRepository.save(new IGGallery());
            iGImage.setGaleria(gallery);
		}

        iGImage.setCreationDate(Instant.now());
        iGImage.setVersion(1);
        IGImage result = iGImageRepository.save(iGImage);
        return ResponseEntity.created(new URI(String.format("%s/%s", IGIMAGE_RESOURCE_URL, result.getId()))).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update ( @PathVariable Long id, @Valid @RequestBody IGImage iGImage, Errors errors){

        if (!id.equals(iGImage.getId())){
            return ResponseEntity.badRequest().headers(HeaderUtil.createError("iGImage.error.id-not-exists", null)).body(null);
        }

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationErrorUtils.getValidationErrors(errors));
        }

        iGImage.setVersion(iGImage.getVersion() + 1);

        IGImage result = iGImageRepository.save(iGImage);
        return ResponseEntity.ok().body(result);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            iGImageRepository.deleteById(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .headers(HeaderUtil.createError("psql.exception", null)).build();
        }

        return ResponseEntity.ok().build();
    }
}
/*% } %*/
