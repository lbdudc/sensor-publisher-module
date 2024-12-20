/*%@
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + 'RepositoryImpl.java',
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
%*/
package es.udc.lbd.gema.lps.model.repository.sensor;

import es.udc.lbd.gema.lps.component.file_uploader.DownloadService;
import es.udc.lbd.gema.lps.model.domain.sensor.CalcAggregation;
import es.udc.lbd.gema.lps.model.domain.sensor./*%= normalize(context.id, true) %*/SpatialAggregation;
import es.udc.lbd.gema.lps.model.domain.sensor./*%= normalize(context.id, true) %*/SpatialFilter;
import es.udc.lbd.gema.lps.model.domain.sensor.TemporalAggregation;
import es.udc.lbd.gema.lps.model.service.dto.sensor.DataDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class /*%= normalize(context.id, true) %*/RepositoryImpl implements /*%= normalize(context.id, true) %*/Repository {

  @PersistenceContext private EntityManager entityManager;

  String entityName = "/*%= camelToSnakeCase(normalize(context.id, true)) %*/";

  private final Logger logger = LoggerFactory.getLogger(/*%= normalize(context.id, true) %*/RepositoryImpl.class);

  @Override
  public List<DataDTO> getData(
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
      String categoryAggregationString,
      String categoryFilter,
      String categoryFrom,
      String categoryTo,
      /*% } %*/
      Integer spatialFilterId) {

    String selectClause = buildSelectClause(spatialAggregation, temporalAggregation, calc, camelToSnake(field));
    String fromClause = buildFromClause(spatialAggregation, spatialFilter, temporalAggregation);
    String whereClause =
        buildWhereClause(
            id,
            start,
            end,
            temporalAggregation,
            spatialAggregation,
            spatialFilter,
            spatialFilterId
            /*% if (hasCategoricalDims) { %*/
            ,
            categoryAggregationString,
            categoryFilter,
            categoryFrom,
            categoryTo
            /*% } %*/
            );
    String groupByClause = buildGroupByClause(spatialAggregation);
    String sqlQueryString = selectClause + fromClause + whereClause + groupByClause;
    logger.debug("SQL QUERY: " + sqlQueryString);
    Query query = entityManager.createNativeQuery(sqlQueryString);
    List<Object[]> resultList = query.getResultList();

    return buildResult(resultList, field);
  }

  @Override
  public List<DataDTO> buildResult(
      List<Object[]> resultList,
      String field) {
    List<DataDTO> result = new ArrayList<>();
    for (Object[] row : resultList) {
      DataDTO dataDTO = new DataDTO();
      for (int i = 0; i < row.length; i++) {

        Map<String, Object> data = new HashMap<>();
        if (i == 0) {
          dataDTO.setId(row[i].toString());
        } else {
          data.put(field.toLowerCase(), row[i]);
          dataDTO.setData(data);
        }
      }
      result.add(dataDTO);
    }
    return result;
  }

  @Override
  public String buildSelectClause(
      /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
      TemporalAggregation temporalAggregation,
      CalcAggregation calc,
      String field) {

    String selectClause = "SELECT ";
    if (spatialAggregation != null) {
      selectClause = selectClause.concat("spatial_entity.id, ");
    } else {
      selectClause = selectClause.concat("sensor_id, ");
    }

    String op = getCalcOp(calc);

    if (temporalAggregation != null) {
      op = op + "(" + op.toLowerCase() + "_" + field + ") ";
    } else {
      op = op + "(" + field + ") ";
    }

    selectClause = selectClause.concat(op);
    return selectClause;
  }

  @Override
  public String buildFromClause(
      /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
      /*%= normalize(context.id, true) %*/SpatialFilter spatialFilter,
      TemporalAggregation temporalAggregation) {
    String fromClause = "FROM ";
    String viewName = getViewName(temporalAggregation);
    fromClause = fromClause.concat(viewName + "view_table ");

    if (spatialAggregation != null) {
      String sensorJoin =
          "JOIN t_" + entityName + "_entity sensor ON view_table.sensor_id = sensor.id ";
      String spatialEntityJoin =
          "JOIN t_"
              + spatialAggregation.toString().toLowerCase()
              + " spatial_entity ON ST_CONTAINS(spatial_entity.geometry, sensor.geometry) ";
      fromClause = fromClause.concat(sensorJoin).concat(spatialEntityJoin);
      if (spatialFilter != null
          && !spatialAggregation.toString().equals(spatialFilter.toString())) {
        String territorialUnitJoin =
            "JOIN t_"
                + spatialFilter.toString().toLowerCase()
                + " territorial_unit ON ST_CONTAINS(filter.geometry, spatial_entity.geometry) ";
        fromClause = fromClause.concat(territorialUnitJoin);
      }
    }
    return fromClause;
  }

  @Override
  public String buildWhereClause(
      Long id,
      LocalDateTime start,
      LocalDateTime end,
      TemporalAggregation temporalAggregation,
      /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
      /*%= normalize(context.id, true) %*/SpatialFilter spatialFilter,
      Integer spatialFilterId
      /*% if (hasCategoricalDims) { %*/
      ,
      String categoryAggregationString,
      String categoryFilter,
      String categoryFrom,
      String categoryTo
      /*% } %*/
      ) {

    String bucket = getTimeBucket(temporalAggregation);
    String whereClause = "WHERE ";
    if (temporalAggregation == null) {
      whereClause = whereClause.concat(bucket + "= '" + start.toString() + "' ");
    } else {
      whereClause =
          whereClause.concat(
              bucket
                  + ">= '"
                  + start.toString()
                  + "' AND "
                  + bucket
                  + "< '"
                  + end.toString()
                  + "' ");
    }

    /*% if (hasCategoricalDims) { %*/
    if (categoryFilter != null) {
      String categories =
          getCategories(categoryAggregationString, categoryFilter, categoryFrom, categoryTo);
      whereClause = whereClause.concat("AND category IN " + categories + " ");
    }
    /*% } %*/

    if (spatialAggregation != null) {
      String spatialFilterQuery = "AND ";

      if (spatialFilter != null && id == null) {
        String entityId =
            spatialAggregation.toString().equals(spatialFilter.toString())
                ? "spatial_entity.id"
                : "territorial_unit.id";
        spatialFilterQuery =
            spatialFilterQuery.concat(entityId + " = " + spatialFilterId.toString() + " ");
        whereClause = whereClause.concat(spatialFilterQuery);
      } else if (id != null) {
        whereClause = whereClause.concat("AND spatial_entity.id = " + id + " ");
      }
    } else if (id != null) {
      whereClause = whereClause.concat("AND sensor_id = " + id + " ");
    }
    return whereClause;
  }

  @Override
  public String buildGroupByClause(
      /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation) {

    String groupByClause = "GROUP BY ";
    if (spatialAggregation != null) {
      groupByClause = groupByClause.concat("spatial_entity.id");
    } else {
      groupByClause = groupByClause.concat("sensor_id ");
    }
    return groupByClause;
  }

  @Override
  public List<DataDTO> getHistogramDataBySensorId(
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
      Integer spatialFilterId) {

    String fieldsAsString = getHistogramFields(fieldsToQuery, spatialAggregation, calc);
    String bucketName =
        "bucket_" + getHistogramTemporalAgg(temporalAggregation).toString().toLowerCase();
    String selectClause = "SELECT " + bucketName + ", " + fieldsAsString;
    String fromClause =
        buildFromClause(
            spatialAggregation, spatialFilter, getHistogramTemporalAgg(temporalAggregation));
    String whereClause =
        buildWhereClause(
            id,
            start,
            end,
            getHistogramTemporalAgg(temporalAggregation),
            spatialAggregation,
            spatialFilter,
            spatialFilterId
            /*% if (hasCategoricalDims) { %*/
            ,
            categoryAggregatioString,
            categoryFilter,
            categoryFrom,
            categoryTo
            /*% } %*/);

    String histogramQuery = selectClause.concat(fromClause).concat(whereClause);

    String groupByClause =
        "GROUP BY bucket_" + getHistogramTemporalAgg(temporalAggregation).toString().toLowerCase();
    histogramQuery = histogramQuery.concat(groupByClause);
    logger.debug("SQL HISTOGRAM QUERY: " + histogramQuery);

    Query query = entityManager.createNativeQuery(histogramQuery);
    List<Object[]> resultList = query.getResultList();
    List<DataDTO> result = buildHistogram(resultList, fieldsToQuery);
    return result;
  }

  /*PRIVATE METHODS */
  /*% if (hasCategoricalDims) { %*/
  private String getCategories(
      String categoryAggregationString,
      String categoryFilter,
      String categoryFrom,
      String categoryTo) {

    String categoryQuery =
        "SELECT id FROM t_category_" + entityName + " WHERE " + categoryAggregationString + " = ";

    if (categoryFrom != null && categoryTo != null) {
      String subquery =
          "( SELECT id FROM t_"
              + categoryAggregationString
              + "_range_"
              + entityName
              + " WHERE \"from\" = "
              + categoryFrom
              + " and \"to\" = "
              + categoryTo
              + ")";
      categoryQuery = categoryQuery.concat(subquery);
    } else {
      categoryQuery = categoryQuery.concat("'" + categoryFilter + "'");
    }

    Query query = entityManager.createNativeQuery(categoryQuery);
    List<Object[]> resultList = query.getResultList();
    String categories = resultList.toString().replace('[', '(').replace(']', ')');

    if (resultList.size() > 0) {
      return categories;
    } else {
      return null;
    }
  }
  /*% } %*/

  private String getCalcOp(CalcAggregation calc) {
    String op = "";
    if (calc == null) {
      op = "AVG";
    } else if (calc.equals(CalcAggregation.AVERAGE)) {
      op = "AVG";
    } else if (calc.equals(CalcAggregation.MAX)) {
      op = "MAX";
    } else if (calc.equals(CalcAggregation.MIN)) {
      op = "MIN";
    }
    return op;
  }

  private String getViewName(TemporalAggregation temporalAggregation) {
    String viewName = "";
    if (temporalAggregation == null) {
      viewName = "t_" + entityName + "_measurement";
    } else if (temporalAggregation.equals(TemporalAggregation.SECOND)) {
      viewName = "agg_second_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.MINUTE)) {
      viewName = "agg_minute_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.HOUR)) {
      viewName = "agg_hour_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.DAY)) {
      viewName = "agg_day_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.WEEK)) {
      viewName = "agg_week_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.MONTH)) {
      viewName = "agg_month_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.YEAR)) {
      viewName = "agg_year_".concat(entityName);
    } else {
      viewName = "t_" + entityName + "_measurement";
    }
    return viewName.concat(" ");
  }

  private String getTimeBucket(TemporalAggregation temporalAggregation) {
    String bucket = "";
    if (temporalAggregation == null) {
      bucket = "date ";
    } else if (temporalAggregation.equals(TemporalAggregation.SECOND)) {
      bucket = "bucket_second ";
    } else if (temporalAggregation.equals(TemporalAggregation.MINUTE)) {
      bucket = "bucket_minute ";
    } else if (temporalAggregation.equals(TemporalAggregation.HOUR)) {
      bucket = "bucket_hour ";
    } else if (temporalAggregation.equals(TemporalAggregation.DAY)) {
      bucket = "bucket_day ";
    } else if (temporalAggregation.equals(TemporalAggregation.WEEK)) {
      bucket = "bucket_week ";
    } else if (temporalAggregation.equals(TemporalAggregation.MONTH)) {
      bucket = "bucket_month ";
    } else if (temporalAggregation.equals(TemporalAggregation.YEAR)) {
      bucket = "bucket_year ";
    } else {
      bucket = "date ";
    }
    return bucket;
  }

  private TemporalAggregation getHistogramTemporalAgg(TemporalAggregation temporalAggregation) {
    TemporalAggregation agg = null;
    if (temporalAggregation.equals(TemporalAggregation.YEAR)) {
      agg = TemporalAggregation.MONTH;
    } else if (temporalAggregation.equals(TemporalAggregation.MONTH)) {
      agg = TemporalAggregation.DAY;
    } else if (temporalAggregation.equals(TemporalAggregation.DAY)) {
      agg = TemporalAggregation.HOUR;
    } else {
      agg = TemporalAggregation.MINUTE;
    }
    return agg;
  }

  private String getHistogramFields(
      List<String> fieldsToQuery,
      /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
      CalcAggregation calc) {
    String fieldsAsString = "";
    for (int i = 0; i < fieldsToQuery.size(); i++) {
      String field = getCalcOp(calc).toLowerCase() + "_" + camelToSnake(fieldsToQuery.get(i));
      field = "AVG(" + field + ") as " + field + " ";
      fieldsAsString = fieldsAsString.concat(field);
      if (i < fieldsToQuery.size() - 1) {
        fieldsAsString = fieldsAsString.concat(", ");
      } else {
        fieldsAsString = fieldsAsString.concat(" ");
      }
    }
    return fieldsAsString;
  }

  private List<DataDTO> buildHistogram(List<Object[]> resultList, List<String> fieldsToQuery) {
    List<DataDTO> result = new ArrayList<>();
    for (Object[] row : resultList) {
      DataDTO dataDTO = new DataDTO();
      Map<String, Object> data = new HashMap<>();
      for (int i = 0; i < row.length; i++) {
        if (i == 0) {
          dataDTO.setId(row[i].toString());
        } else {
          data.put(fieldsToQuery.get(i - 1).toLowerCase(), row[i]);
          dataDTO.setData(data);
        }
      }
      dataDTO.setData(data);
      result.add(dataDTO);
    }

    return result;
  }

  private String camelToSnake(String camelCaseString) {
    if (camelCaseString == null || camelCaseString.isEmpty()) {
      return camelCaseString;
    }
    String snakeCaseString = camelCaseString.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    return snakeCaseString;
  }
}
