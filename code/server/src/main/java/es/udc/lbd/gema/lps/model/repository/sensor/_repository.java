/*%@
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + 'Repository.java',
          context: sen
      };
    });
%*/
/*%
  const dimensions = [];
  data.dataWarehouse.sensors
    .filter(sen => sen.id === context.id)
    .forEach(function(sensor) {
      const dims = sensor.dimensions;
      dims
        .filter(dim => dim.type === "CATEGORICAL")
        .forEach(dim => {
          dimensions.push(dim);
        });
  });
  var hasCategoricalDims = dimensions.length > 0;
  const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
    return sensor.isMoving === true; });
%*/
package es.udc.lbd.gema.lps.model.repository.sensor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import es.udc.lbd.gema.lps.model.domain.sensor.CalcAggregation;
import es.udc.lbd.gema.lps.model.domain.sensor.TemporalAggregation;
import es.udc.lbd.gema.lps.model.service.dto.sensor.DataDTO;
import es.udc.lbd.gema.lps.model.domain.sensor./*%= normalize(context.id, true) %*/SpatialAggregation;
import es.udc.lbd.gema.lps.model.domain.sensor./*%= normalize(context.id, true) %*/SpatialFilter;
/*%if (feature.SV_P_SensorInfo){ %*/
import es.udc.lbd.gema.lps.model.service.exceptions.NotFoundException;
/*% } %*/
/*% if (hasMovingSensors) { %*/
import es.udc.lbd.gema.lps.web.rest.custom.FeatureCollectionJSON;
/*% } %*/

public interface /*%= normalize(context.id, true) %*/Repository {
  /*% if (hasMovingSensors) { %*/ FeatureCollectionJSON /*% } else { %*/ List<DataDTO> /*% } %*/ getData(
    Long id,
    LocalDateTime start,
    LocalDateTime end,
    TemporalAggregation temporalAggregation,
    /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
    CalcAggregation calc,
    String field,
    List<String> fieldsToQuery,
    /*%= normalize(context.id, true) %*/SpatialFilter spatialFilter,
    /*% if (hasCategoricalDims) { %*/
    String categoryAggregatioString,
    String categoryFilter,
    String categoryFrom,
    String categoryTo,
    /*% } %*/
    Integer spatialFilterId
    /*% if (hasMovingSensors) { %*/
    ,String spatialOperation
    /*% } %*/
    );

  /*% if (hasMovingSensors){ %*/ FeatureCollectionJSON /*% } else { %*/ List<DataDTO> /*% } %*/ buildResult(
    List<Object[]> resultList,
    String field);

  String buildSelectClause(
    /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
    TemporalAggregation temporalAggregation,
    CalcAggregation calc,
    String field);

  String buildFromClause(
    /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
    /*%= normalize(context.id, true) %*/SpatialFilter spatialFilter,
    TemporalAggregation temporalAggregation);

  String buildWhereClause(
    Long id,
    LocalDateTime start,
    LocalDateTime end,
    TemporalAggregation temporalAggregation,
    /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
    /*%= normalize(context.id, true) %*/SpatialFilter spatialFilter,
    Integer spatialFilterId
    /*% if (hasCategoricalDims) { %*/
    ,
    String categoryAggregatioString,
    String categoryFilter,
    String categoryFrom,
    String categoryTo
    /*% } %*/
    /*% if (hasMovingSensors) { %*/
    ,String spatialOperation
    /*% } %*/
    );

  String buildGroupByClause(
    /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation);

  /*% if (hasMovingSensors) { %*/
  String buildOrderByClause(Long id, TemporalAggregation temporalAggregation);

  /*% } %*/
  List<DataDTO> getHistogramDataBySensorId(
    Long id,
    LocalDateTime start,
    LocalDateTime end,
    TemporalAggregation temporalAggregation,
    /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
    CalcAggregation calc,
    List<String> fieldsToQuery,
    /*%= normalize(context.id, true) %*/SpatialFilter spatialFilter,
    /*% if (hasCategoricalDims) { %*/
    String categoryAggregatioString,
    String categoryFilter,
    String categoryFrom,
    String categoryTo,
    /*% } %*/
    Integer spatialFilterId);

}
