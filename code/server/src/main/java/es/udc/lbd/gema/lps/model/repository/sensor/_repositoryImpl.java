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
  const hasMovingSensors = data.dataWarehouse.sensors?.find(function(sensor) {
    return sensor.isMoving === true; });
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
/*% if (hasMovingSensors) { %*/
import es.udc.lbd.gema.lps.web.rest.custom.FeatureCollectionJSON;
import es.udc.lbd.gema.lps.web.rest.custom.FeatureJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
/*% } %*/

@Repository
public class /*%= normalize(context.id, true) %*/RepositoryImpl implements /*%= normalize(context.id, true) %*/Repository {

  @PersistenceContext private EntityManager entityManager;

  String entityName = "/*%= camelToSnakeCase(normalize(context.id, true)) %*/";

  private final Logger logger = LoggerFactory.getLogger(/*%= normalize(context.id, true) %*/RepositoryImpl.class);

  /*% if (hasMovingSensors) { %*/
  @Override
  public FeatureCollectionJSON getData(
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
      Integer spatialFilterId,
      String spatialOperation) {

    String selectClause =
        buildSelectClause(spatialAggregation, temporalAggregation, calc, camelToSnake(field));
    TemporalAggregation aggLevelBelow = getLevelBelowTemporalAgg(temporalAggregation);
    String fromClause = buildFromClause(spatialAggregation, spatialFilter, aggLevelBelow);
    String whereClause =
        buildWhereClause(
            id,
            start,
            end,
            aggLevelBelow,
            spatialAggregation,
            spatialFilter,
            spatialFilterId
            /*% if (hasCategoricalDims) { %*/
            ,
            categoryAggregationString,
            categoryFilter,
            categoryFrom,
            categoryTo,
            /*% } %*/
            spatialOperation
            );

    String sqlQueryString = "";
    if (spatialAggregation == null) {
      sqlQueryString =
          selectClause + fromClause + whereClause + buildOrderByClause(id, aggLevelBelow);
    } else {
      sqlQueryString =
          selectClause + fromClause + whereClause + buildGroupByClause(spatialAggregation);
    }

    logger.debug("SQL QUERY: " + sqlQueryString);
    Query query = entityManager.createNativeQuery(sqlQueryString);
    List<Object[]> resultList = query.getResultList();
    return buildResult(resultList, field);
  }

  @Override
  public FeatureCollectionJSON buildResult(List<Object[]> resultList, String field) {
    List<FeatureJSON> featureList = new ArrayList<>();
    for (int index = 0; index < resultList.size(); index++) {
      Object[] row = resultList.get(index);
      FeatureJSON featureJSON = new FeatureJSON();
      featureJSON.setId((long) index);
      featureJSON.setGeometry(getGeometry(row[1].toString()));
      featureJSON.setType("Feature");
      featureJSON.setProperties(
          Map.of(
              "displayString",
              row[0].toString(),
              "sensor_id",
              row[0].toString(),
              field.toLowerCase(),
              row[2].toString()));
      featureList.add(featureJSON);
    }
    FeatureCollectionJSON featureCollectionJSON = new FeatureCollectionJSON();
    featureCollectionJSON.setType("FeatureCollection");
    featureCollectionJSON.setFeatures(featureList);
    return featureCollectionJSON;
  }

  @Override
  public String buildSelectClause(
      /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
      TemporalAggregation temporalAggregation,
      CalcAggregation calc,
      String field) {

    String selectClause = "SELECT ";
    String op = getCalcOp(calc);
    if (spatialAggregation == null) {
      selectClause = selectClause.concat("sensor_id, geometry, ");
      op = op.toLowerCase() + "_" + field + ", ";
      TemporalAggregation aggLevelBelow = getLevelBelowTemporalAgg(temporalAggregation);
      selectClause = selectClause.concat(op).concat(getTimeBucket(aggLevelBelow)).concat(" ");
    } else {
      String spatialAgg = spatialAggregation.toString().toLowerCase();
      selectClause =
          selectClause.concat(
              spatialAgg
                  + ".id, "
                  + spatialAgg
                  + ".geometry, "
                  + op
                  + "("
                  + op.toLowerCase()
                  + "_"
                  + field
                  + ") ");
    }
    return selectClause;
  }

  @Override
  public String buildFromClause(
      /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation,
      /*%= normalize(context.id, true) %*/SpatialFilter spatialFilter,
      TemporalAggregation temporalAggregation) {
    String fromClause = "FROM ";
    if (spatialAggregation == null) {
      String viewName = getViewName(temporalAggregation);
      fromClause = fromClause.concat("agg" + viewName + "view_table ");
    } else {
      String spatialAgg = spatialAggregation.toString().toLowerCase();
      if (temporalAggregation != null) {
        String viewName = getViewName(temporalAggregation);
        fromClause =
            fromClause.concat(
                "agg_"
                    + spatialAgg
                    + viewName
                    + "aggregation join t_"
                    + spatialAgg
                    + " "
                    + spatialAgg
                    + " on aggregation."
                    + spatialAgg
                    + "_id = "
                    + spatialAgg
                    + ".id ");
      } else {
        String viewName = getViewName(TemporalAggregation.MINUTE);
        fromClause =
            fromClause.concat(
                "agg_"
                    + spatialAgg
                    + viewName
                    + "aggregation join t_"
                    + spatialAgg
                    + " "
                    + spatialAgg
                    + " on aggregation."
                    + spatialAgg
                    + "_id = "
                    + spatialAgg
                    + ".id ");
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
      String categoryTo,
      /*% } %*/
      String spatialOperation
      ) {

    String whereClause = "WHERE ";
    if (spatialAggregation == null) {
      String bucket = getTimeBucket(temporalAggregation);
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
      if (id != null) {
        whereClause = whereClause.concat("AND sensor_id = " + id + " ");
      }
    } else {
      String spatialAgg = spatialAggregation.toString().toLowerCase();
      if (temporalAggregation == null) {
        String bucket = getTimeBucket(TemporalAggregation.MINUTE);
        whereClause =
            "WHERE "
                + bucket
                + " >= '"
                + start.toString()
                + "' AND "
                + bucket
                + " < '"
                + end.toString()
                + "' ";

      } else {
        String bucket = getTimeBucket(temporalAggregation);
        whereClause =
            "WHERE "
                + bucket
                + " >= '"
                + start.toString()
                + "' AND "
                + bucket
                + " < '"
                + end.toString()
                + "' ";
      }
      if (spatialFilterId != null) {
        if (spatialOperation != null) {
          whereClause =
              whereClause.concat(
                  "AND aggregation."
                      + spatialAgg
                      + "_id IN (SELECT spatial_agg.id FROM t_"
                      + spatialAgg
                      + " spatial_agg JOIN t_"
                      + spatialFilter
                      + " spatial_filter ON "
                      + getPostgisOperation(spatialOperation)
                      + "(spatial_filter.geometry, spatial_agg.geometry) WHERE spatial_filter.id = "
                      + spatialFilterId
                      + ") ");

        } else if (spatialAggregation.toString().equals(spatialFilter.toString())) {
          whereClause = whereClause.concat("AND " + spatialAgg + "_id = " + spatialFilterId + " ");
        }
      }
    }
    /*% if (hasCategoricalDims) { %*/
    if (categoryFilter != null) {
      String categories =
          getCategories(categoryAggregationString, categoryFilter, categoryFrom, categoryTo);
      whereClause = whereClause.concat("AND category IN " + categories + " ");
    }
    /*% } %*/
    return whereClause;
  }

  @Override
  public String buildGroupByClause(
      /*%= normalize(context.id, true) %*/SpatialAggregation spatialAggregation) {
    String groupByClause = "GROUP BY ";
    if (spatialAggregation == null) {
      groupByClause = groupByClause.concat("sensor_id ");
    } else {
      String spatialAgg = spatialAggregation.toString().toLowerCase();
      groupByClause = groupByClause.concat(spatialAgg + ".geometry, " + spatialAgg + ".id ");
    }
    return groupByClause;
  }

  @Override
  public String buildOrderByClause(Long id, TemporalAggregation temporalAggregation) {
    String orderByClause = "ORDER BY ";
    String timeBucket = getTimeBucket(temporalAggregation);
    orderByClause = orderByClause.concat("sensor_id, ").concat(timeBucket);
    return orderByClause;
  }

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

  private String getViewName(TemporalAggregation temporalAggregation) {
    String viewName = "";
    if (temporalAggregation == null) {
      viewName = "_interval_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.SECOND)) {
      viewName = "_second_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.MINUTE)) {
      viewName = "_minute_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.HOUR)) {
      viewName = "_hour_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.DAY)) {
      viewName = "_day_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.WEEK)) {
      viewName = "_week_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.MONTH)) {
      viewName = "_month_".concat(entityName);
    } else if (temporalAggregation.equals(TemporalAggregation.YEAR)) {
      viewName = "_year_".concat(entityName);
    } else {
      viewName = "_interval_".concat(entityName);
    }
    return viewName.concat(" ");
  }

  private TemporalAggregation getLevelBelowTemporalAgg(TemporalAggregation temporalAggregation) {
    TemporalAggregation agg = null;
    if (temporalAggregation == null) {
      agg = temporalAggregation;
    } else if (temporalAggregation.equals(TemporalAggregation.YEAR)) {
      agg = TemporalAggregation.MONTH;
    } else if (temporalAggregation.equals(TemporalAggregation.MONTH)) {
      agg = TemporalAggregation.WEEK;
    } else if (temporalAggregation.equals(TemporalAggregation.WEEK)) {
      agg = TemporalAggregation.DAY;
    } else if (temporalAggregation.equals(TemporalAggregation.DAY)) {
      agg = TemporalAggregation.HOUR;
    } else if (temporalAggregation.equals(TemporalAggregation.HOUR)) {
      agg = TemporalAggregation.MINUTE;
    } else {
      agg = temporalAggregation;
    }
    return agg;
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

  private Geometry getGeometry(String geomAsString) {
    try {
      geomAsString = geomAsString.substring(geomAsString.indexOf(";") + 1);
      WKTReader reader = new WKTReader(new GeometryFactory());
      return reader.read(geomAsString);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  private String getPostgisOperation(String spatialOperation) {
    switch (spatialOperation) {
      case "CONTAINS":
        return "ST_Contains";
      case "INTERSECTS":
        return "ST_Intersects";
      default:
        return null;
    }
  }

  /*% } else { %*/
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
    op = op + "(" + op.toLowerCase() + "_" + field + ") ";
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

  private String getViewName(TemporalAggregation temporalAggregation) {
    String viewName = "";
    if (temporalAggregation == null) {
      viewName = "agg_interval_".concat(entityName);
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
      viewName = "agg_interval_".concat(entityName);
    }
    return viewName.concat(" ");
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
  /*% } %*/

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
            /*% } %*/
            /*% if (hasMovingSensors) { %*/
            ,null
            /*% } %*/
            );
    String histogramQuery = selectClause.concat(fromClause).concat(whereClause);
    String groupByClause =
        "GROUP BY bucket_" + getHistogramTemporalAgg(temporalAggregation).toString().toLowerCase() +" ";
    histogramQuery = histogramQuery.concat(groupByClause);

    String orderByClause =
        "ORDER BY bucket_" + getHistogramTemporalAgg(temporalAggregation).toString().toLowerCase();
    histogramQuery = histogramQuery.concat(orderByClause);

    logger.debug("SQL HISTOGRAM QUERY: " + histogramQuery);

    Query query = entityManager.createNativeQuery(histogramQuery);
    List<Object[]> resultList = query.getResultList();
    List<DataDTO> result = buildHistogram(resultList, fieldsToQuery);
    return result;
  }

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

  private String getTimeBucket(TemporalAggregation temporalAggregation) {
    String bucket = "";
    if (temporalAggregation == null) {
      bucket = "bucket_interval ";
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
      bucket = "bucket_interval ";
    }
    return bucket;
  }

  private TemporalAggregation getHistogramTemporalAgg(TemporalAggregation temporalAggregation) {
    TemporalAggregation agg = null;
    if (temporalAggregation.equals(TemporalAggregation.YEAR)) {
      agg = TemporalAggregation.MONTH;
    } else if (temporalAggregation.equals(TemporalAggregation.MONTH)) {
      agg = TemporalAggregation.DAY;
    } else if (temporalAggregation.equals(TemporalAggregation.WEEK)) {
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

  private String camelToSnake(String camelCaseString) {
    if (camelCaseString == null || camelCaseString.isEmpty()) {
      return camelCaseString;
    }
    String snakeCaseString = camelCaseString.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    return snakeCaseString;
  }

}
