/*%@ return data.dataModel.entities.map(function(en) {
    return {
        fileName: normalize(en.name, true) + 'FullDTO.java',
        context: en
    };
})
%*/
package es.udc.lbd.gema.lps.model.service.dto;

import es.udc.lbd.gema.lps.model.domain.*;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
/*% if (feature.T_GIS) { %*/
import jakarta.persistence.Column;

import es.udc.lbd.gema.lps.model.util.jackson.CustomGeometrySerializer;
import es.udc.lbd.gema.lps.model.util.jackson.CustomGeometryDeserializer;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
/*% } %*/
/*% if(propertiesHasGallery(context.properties)) { %*/

import es.udc.lbd.gema.lps.component.gallery.dto.IGGalleryDTO;
/*% } %*/
public class /*%= normalize(context.name, true) %*/FullDTO /*% if (context.superclass) { %*/extends /*%= normalize(context.superclass, true) %*/FullDTO/*%  } %*/ {
  /*% context.properties.forEach(function(prop) { %*/
    /*%
        var propertyClass = prop.class;
            if (propertyClass == 'Date') propertyClass = 'LocalDate';
            if (propertyClass == 'DateTime') propertyClass = 'LocalDateTime';
            if (propertyClass == 'IGGallery') propertyClass = 'IGGalleryDTO';
            var propertyIsText = false;
            if (propertyClass == 'Text') { propertyClass = 'String'; propertyIsText = true; }
        propertyClass = propertyClass.split(' ')[0];
        var propertyIsEntity =
                        data.dataModel.entities
                            .map(function(en) { return en.name; })
                            .indexOf(propertyClass) != -1;
        if (!propertyIsEntity || prop.owner) {
          if (propertyIsEntity) {
            propertyClass = normalize(propertyClass, true) + 'DTO';
          }
          if (prop.multiple) {
            propertyClass = 'List<' + normalize(propertyClass, true) + '>';
          } else {
            propertyClass = normalize(propertyClass, true);
          }
    %*/
    /*% if (isGeographicProperty(propertyClass)) { %*/
  @JsonSerialize(using = CustomGeometrySerializer.class)
  @JsonDeserialize(using = CustomGeometryDeserializer.class)
    /*% } %*/
  private /*%= propertyClass %*/ /*%= normalize(prop.name) %*/;
  /*%
        }
      });
  %*/

  public /*%= normalize(context.name, true) %*/FullDTO() {
    /*% if (context.superclass) { %*/ super(); /*% } %*/
  }

  public /*%= normalize(context.name, true) %*/FullDTO(/*%= normalize(context.name, true) %*/ /*%=normalize(context.name) %*/) {
    /*% if (context.superclass) { %*/super((/*%= normalize(context.superclass, true) %*/ ) /*%=normalize(context.name) %*/); /*% } %*/
    /*% context.properties.forEach(function(prop) { %*/
    /*%
        const propertyIsEntity =
                       data.dataModel.entities.find(function(en) { return en.name == prop.class; }) != null;
    %*/
    /*% if (!propertyIsEntity || prop.owner) { %*/
    /*% if (propertyIsEntity) { %*/
    if (/*%= normalize(context.name) %*/.get/*%=normalize(prop.name, true) %*/() != null) {
    /*% if (prop.multiple) { %*/
    this./*%= normalize(prop.name) %*/ = /*%= normalize(context.name) %*/.get/*%=normalize(prop.name, true) %*/().stream().map(/*%= prop.class.split(' ')[0] %*/DTO::new).collect(Collectors.toList());
    /*% } else { %*/
    this./*%= normalize(prop.name) %*/ = new /*%= normalize(prop.class.split(' ')[0],  true) %*/DTO(/*%= normalize(context.name) %*/.get/*%=normalize(prop.name, true) %*/());
    /*% } %*/
    }
    /*% } else { %*/
      /*% if (prop.class == 'IGGallery') { %*/
    if (/*%= normalize(context.name) %*/.get/*%=normalize(prop.name, true) %*/() != null) {
      this./*%= normalize(prop.name) %*/ = new /*%= normalize(prop.class.split(' ')[0],  true) %*/DTO(/*%= normalize(context.name) %*/.get/*%=normalize(prop.name, true) %*/());
    }
    /*% } else { %*/
    this./*%= normalize(prop.name) %*/ = /*%= normalize(context.name) %*/.get/*%=normalize(prop.name, true) %*/();
    /*% } %*/
    /*% } %*/
    /*% } %*/
    /*% }); %*/
  }

  /*% context.properties.forEach(function(prop) { %*/
      /*%
          var propertyClass = prop.class.split(' ')[0];
          if (propertyClass == 'Date') propertyClass = 'LocalDate';
          if (propertyClass == 'DateTime') propertyClass = 'LocalDateTime';
          if (propertyClass == 'IGGallery') propertyClass = 'IGGalleryDTO';
          if (propertyClass == 'Text') propertyClass = 'String';
          var propertyIsEntity =
                      data.dataModel.entities
                          .map(function(en) { return en.name; })
                          .indexOf(prop.class) != -1;
          if (!propertyIsEntity || prop.owner) {
            if (propertyIsEntity) {
              propertyClass = normalize(propertyClass, true) + 'DTO';
            }
            if (prop.multiple) {
              propertyClass = 'List<' + normalize(propertyClass, true) + '>';
            } else {
              propertyClass = normalize(propertyClass, true);
            }
      %*/
  public /*%= propertyClass %*/ get/*%= normalize(prop.name, true) %*/() {
    return /*%= normalize(prop.name) %*/;
  }

  public void set/*%= normalize(prop.name, true) %*/(/*%= propertyClass %*/ /*%= normalize(prop.name) %*/) {
    this./*%= normalize(prop.name) %*/ = /*%= normalize(prop.name) %*/;
  }

  /*%
        }
      });
  %*/
  /*% if (!context.abstract) { %*/
  public /*%= normalize(context.name, true) %*/ to/*%= normalize(context.name, true) %*/() {
    /*%= normalize(context.name, true) %*/ /*%=normalize(context.name) %*/ = new /*%= normalize(context.name, true) %*/();
    /*% const properties = getProperties(context, data);
        properties.forEach(function(prop) { %*/
      /*%
        var propertyIsEntity = data.dataModel.entities.find(function(en) { return en.name == prop.class; }) != null;
      %*/
    /*% if (!propertyIsEntity || prop.owner) { %*/
    /*% if (propertyIsEntity) { %*/
    if (this.get/*%= normalize(prop.name, true) %*/() != null) {
    /*% if (prop.multiple) { %*/
      /*%= normalize(context.name) %*/.set/*%= normalize(prop.name, true) %*/(this.get/*%= normalize(prop.name, true) %*/().stream().map(/*%= prop.class.split(' ')[0] %*/DTO::to/*%= prop.class %*/).collect(Collectors.toList()));
    /*% } else { %*/
      /*%= normalize(context.name) %*/.set/*%= normalize(prop.name, true) %*/(this.get/*%= normalize(prop.name, true) %*/().to/*%= prop.class %*/());
    /*% } %*/
    }
    /*% } else { %*/
      /*% if (prop.class == 'IGGallery') { %*/
    if (this.get/*%=normalize(prop.name, true) %*/() != null) {
      /*%= normalize(context.name) %*/.set/*%= normalize(prop.name, true) %*/(this.get/*%= normalize(prop.name, true) %*/().to/*%= prop.class %*/());
    }
      /*% } else { %*/
    /*%= normalize(context.name) %*/.set/*%= normalize(prop.name, true) %*/(this.get/*%= normalize(prop.name, true) %*/());
      /*% } %*/
    /*% } %*/
    /*% } %*/
    /*% }); %*/
    return /*%=normalize(context.name) %*/;
  }
  /*% } %*/
}
