/*%@ return data.dataModel.entities
      .filter(function(context){
        return !context.abstract;
      })
      .map(function(en) {
        return {
            fileName: normalize(en.name, true) + 'Service.java',
            context: en
        };
      });
%*/
/*%
    var geographicPropertyNames = [];
    if (feature.MapViewer) {
        geographicPropertyNames = getEntityPropertyNamesOfGeographicTypes(context);
    }

    var hasGallery = propertiesHasGallery(context.properties);
    var validGeom = (!checkEntityContainsPropertiesOfTypes(context, ["Point", "MultiPoint"]) && geographicPropertyNames.length != 0);
%*/
/*%
    var pkClass = getEntityProperty(context, 'id').class;
    pkClass = normalize(pkClass.split(' ')[0], true);
    var pkName = normalize(getEntityProperty(context, 'id').name);
%*/
package es.udc.lbd.gema.lps.model.service;

import java.util.List;
import es.udc.lbd.gema.lps.model.service.dto./*%= normalize(context.name, true) %*/DTO;
import es.udc.lbd.gema.lps.model.service.dto./*%= normalize(context.name, true) %*/FullDTO;
import org.springframework.core.io.FileSystemResource;
/*% if (geographicPropertyNames.length > 0) { %*/
import es.udc.lbd.gema.lps.web.rest.custom.FeatureCollectionJSON;
import es.udc.lbd.gema.lps.web.rest.custom.FeatureJSON;
import java.util.HashMap;
/*% } %*/
/*% if (hasGallery) { %*/
import es.udc.lbd.gema.lps.component.gallery.model.domain.IGImage;
/*% } %*/
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.udc.lbd.gema.lps.model.service.exceptions.OperationNotAllowedException;
import es.udc.lbd.gema.lps.model.service.exceptions.NotFoundException;
/*% if (feature.MV_MS_GeoServer) { %*/
import es.udc.lbd.gema.lps.model.service.util.GeoServerUtil;
/*% } %*/
import java.io.IOException;

public interface /*%= normalize(context.name, true) %*/Service {

  /*% getOtherRelationshipsNotMultipleAndOwning(data, context).forEach(function(r) { %*/
  List</*%= normalize(context.name, true) %*/DTO> getAllWithout/*%= normalize(r.property.bidirectional, true) %*/();
  /*% }); %*/
  Page</*%= normalize(context.name, true) %*/DTO> getAll(Pageable pageable, List<String> filters, String search);
  /*% geographicPropertyNames.forEach(function(geoPropertyName) {
        geoPropertyName = normalize(geoPropertyName);
    %*/
  FeatureCollectionJSON get/*%= normalize(geoPropertyName, true) %*/(
    /*% if (feature.MV_MS_GJ_Paginated) { %*/Double xmin, Double xmax, Double ymin, Double ymax);/*% } else { %*/Boolean properties, List<String> filters);/*% } %*/
  /*% }); %*/
  /*%= normalize(context.name, true) %*/FullDTO get(/*%= pkClass %*/ /*%= pkName %*/) throws NotFoundException;
  /*%= normalize(context.name, true) %*/FullDTO create(/*%= normalize(context.name, true) %*/FullDTO /*%= normalize(context.name) %*/) throws OperationNotAllowedException;
  /*%= normalize(context.name, true) %*/FullDTO update(/*%= pkClass %*/ /*%= pkName %*/, /*%= normalize(context.name, true) %*/FullDTO /*%= normalize(context.name) %*/) throws OperationNotAllowedException;
  /*% if (hasGallery) { %*/
  /*%= normalize(context.name, true) %*/FullDTO updateGallery(Long id, IGImage image) throws NotFoundException ;
  /*% } %*/
  void delete(/*%= pkClass %*/ /*%= pkName %*/);
  /*% if ((feature.MV_MS_GeoServer && !feature.MV_MS_GeoJSON) || (feature.MV_MS_GeoServer && validGeom)) { %*/
  void restartGeom() throws IllegalArgumentException;
  /*% } %*/
}
