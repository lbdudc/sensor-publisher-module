/*%@
  if (!feature.SV_D_DataInsertion || !feature.SV_D_DI_Producers) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + '.java',
          context: sen
      };
    });
%*/
/*%
    var entity = data.dataModel.entities.find(function(en) { return en.name == context.entity; });
 %*/
package es.udc.lbd.gema.lps.model.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class /*%= normalize(context.id, true) %*/ {

    /*% context.measureData.forEach(function(prop) { %*/
    /*%
      var normalizedProp = prop.name.toLowerCase();
    %*/
    private /*%= normalize(prop.type, true) %*/ /*%= normalize(normalizedProp) %*/;
    /*% }); %*/


    /*% if (entity) { %*/
    private Long /*%= normalize(context.entity, false) %*/Id;
    /*% } %*/

    private String date;


    /*%
      var dimensions = [];
      context.dimensions.forEach(function(dim) {
        dim.entities?.forEach(function(en) {
          if (!dimensions.find(function(d) { return d == en; })) {
            dimensions.push(en);
          }
        });
      });

      dimensions.forEach(function(dim) {
    %*/
      private Long /*%= normalize(dim, false) %*/Id;
    /*%
      });
    %*/

    public /*%= normalize(context.id, true) %*/() {
    }

    /*% context.measureData.forEach(function(prop) { %*/
    /*%
      var normalizedProp = prop.name.toLowerCase();
    %*/
    public /*%= normalize(prop.type, true) %*/ get/*%= normalize(normalizedProp, true) %*/() {
        return /*%= normalize(normalizedProp) %*/;
    }

    public void set/*%= normalize(normalizedProp, true) %*/(/*%= normalize(prop.type, true) %*/ /*%= normalize(normalizedProp) %*/) {
        this./*%= normalize(normalizedProp) %*/ = /*%= normalize(normalizedProp) %*/;
    }
    /*% }); %*/


     public Long get/*%= normalize(context.entity, true) %*/Id() {
      return /*%= normalize(context.entity, false) %*/Id;
    }

    public void set/*%= normalize(context.entity, true) %*/Id(Long /*%= normalize(context.entity, false) %*/Id) {
      this./*%= normalize(context.entity, false) %*/Id = /*%= normalize(context.entity, false) %*/Id;
    }

    public String getDate() {
      return date;
    }

    public void setDate(String date) {
      this.date = date;
    }

    /*%
      var dimensions = [];
      context.dimensions.forEach(function(dim) {
        dim.entities?.forEach(function(en) {
          if (!dimensions.find(function(d) { return d == en; })) {
            dimensions.push(en);
          }
        });
      });

      dimensions.forEach(function(dim) {
    %*/
      public Long get/*%= normalize(dim, true) %*/Id() {
        return /*%= normalize(dim, false) %*/Id;
      }

      public void set/*%= normalize(dim, true) %*/Id(Long /*%= normalize(dim, false) %*/Id) {
        this./*%= normalize(dim, false) %*/Id = /*%= normalize(dim, false) %*/Id;
      }
    /*%
      });
    %*/

    public String toJSON() {
      String res = "{";
      String dateAux = date.substring(0, 16) + ":00";

      /*% context.measureData.forEach(function(prop) { %*/
      /*%
        var normalizedProp = prop.name.toLowerCase();
      %*/
      res += "\"/*%= normalize(normalizedProp) %*/\": " + this./*%= normalize(normalizedProp) %*/ + ", ";
      /*% }); %*/

      /*% if (entity) { %*/
      res += "\"/*%= normalize(context.entity, false) %*/Id\": " + this./*%= normalize(context.entity, false) %*/Id + ", ";
      /*% } %*/

      res += "\"@timestamp\": \"" + dateAux + "\", ";

      /*%
        var dimensions = [];
        context.dimensions.forEach(function(dim) {
          dim.entities?.forEach(function(en) {
            if (!dimensions.find(function(d) { return d == en; })) {
              dimensions.push(en);
            }
          });
        });

        dimensions.forEach(function(dim) {
      %*/
        res += "\"/*%= normalize(dim, false) %*/Id\": " + this./*%= normalize(dim, false) %*/Id + ", ";
      /*%
        });
      %*/

      return res.substring(0, res.length() - 2) + "}";
    }
}
