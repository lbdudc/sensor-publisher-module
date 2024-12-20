/*%@
  if (!feature.SensorViewer) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, true) + 'ServiceImpl.java',
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
/*%
  spatialDims = [];
  data.dataWarehouse.sensors
    .filter(sen => sen.id === context.id)
    .forEach(function(sensor) {
      const dims = sensor.dimensions;
      dims
        .filter(dim => dim.type === "SPATIAL")
        .forEach(dim => {
          spatialDims = spatialDims.concat(dim.entities)
        });
  });
  spatialDims = [...new Set(spatialDims)];
%*/
package es.udc.lbd.gema.lps.model.service.sensor;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import es.udc.lbd.gema.lps.model.domain.sensor.CalcAggregation;
import es.udc.lbd.gema.lps.model.domain.sensor.TemporalAggregation;
import es.udc.lbd.gema.lps.model.repository.sensor./*%= normalize(context.id, true) %*/Repository;

/*% if (feature.SV_P_SensorInfo) { %*/
import es.udc.lbd.gema.lps.model.service./*%= normalize(context.id, true) %*/EntityService;
/*% } %*/
import es.udc.lbd.gema.lps.model.service.dto./*%= normalize(context.id, true) %*/EntityFullDTO;

import es.udc.lbd.gema.lps.model.repository.sensor./*%= normalize(context.id, true) %*/Repository;
import es.udc.lbd.gema.lps.model.service.dto.sensor.DataDTO;
import es.udc.lbd.gema.lps.model.service.dto.sensor./*%= normalize(context.id, true) %*/StateRequestDto;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/*%if (feature.SV_P_SensorInfo){ %*/
import es.udc.lbd.gema.lps.model.service.exceptions.NotFoundException;
/*% } %*/
/*% spatialDims.forEach(function(dim){ %*/
import es.udc.lbd.gema.lps.model.service.dto./*%= dim %*/FullDTO;
import es.udc.lbd.gema.lps.model.service./*%= dim %*/Service;
/*% }); %*/
import es.udc.lbd.gema.lps.model.domain.sensor./*%= normalize(context.id, true) %*/SpatialAggregation;

@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class /*%= normalize(context.id, true) %*/ServiceImpl implements /*%= normalize(context.id, true) %*/Service {

  @Inject private /*%= normalize(context.id, true) %*/Repository /*%= normalize(context.id, true) %*/Repository;

  /*% if (feature.SV_P_SensorInfo) { %*/
  @Inject private /*%= normalize(context.id, true) %*/EntityService /*%= normalize(context.id, true) %*/EntityService;
  /*% } %*/

  /*% spatialDims.forEach(function(dim){ %*/
  @Inject private /*%= dim %*/Service /*%= dim.toLowerCase() %*/Service;

  /*% }); %*/
  @Override
  public List<DataDTO> getData(/*%= normalize(context.id, true) %*/StateRequestDto params) {
    return /*%= normalize(context.id, true) %*/Repository.getData(
      null,
      getStart(params),
      getEnd(params),
      params.getTemporalAggregation(),
      params.getSpatialAggregation(),
      params.getCalcAggregation(),
      params.getPropertyAggregation(),
      params.getProperties(),
      params.getSpatialFilter(),
      /*% if (hasCategoricalDims) { %*/
      params.getCategoryAggregation(),
      params.getCategoryFilter(),
      params.getCategoryFrom(),
      params.getCategoryTo(),
      /*% } %*/
      params.getSpatialFilterId());
  }

  @Override
  public DataDTO getData(Long id, /*%= normalize(context.id, true) %*/StateRequestDto params)/*% if (feature.SV_P_SensorInfo) { %*/ throws NotFoundException /*% } %*/ {

    List<DataDTO> measurementList = /*%= normalize(context.id, true) %*/Repository.getData(
      id,
      getStart(params),
      getEnd(params),
      params.getTemporalAggregation(),
      params.getSpatialAggregation(),
      params.getCalcAggregation(),
      params.getPropertyAggregation(),
      params.getProperties(),
      params.getSpatialFilter(),
      /*% if (hasCategoricalDims) { %*/
      params.getCategoryAggregation(),
      params.getCategoryFilter(),
      params.getCategoryFrom(),
      params.getCategoryTo(),
      /*% } %*/
      params.getSpatialFilterId());

    DataDTO result = new DataDTO();
    result.setId(id.toString());
    Map<String, Object> data = new HashMap<>();

    /*% if (feature.SV_P_SensorInfo) { %*/
    if (params.getSpatialAggregation() != null) {
      /*% spatialDims.forEach(function(dim, idx) { %*/
      if (params.getSpatialAggregation().equals(/*%= normalize(context.id, true) %*/SpatialAggregation./*%= dim %*/)) {
        /*%= dim %*/FullDTO /*%= dim.toLowerCase() %*/FullDTO = /*%= dim.toLowerCase() %*/Service.get(id);
        /*%= dim.toLowerCase() %*/FullDTO.setGeometry(null);
        data.put("sensorInfo", /*%= dim.toLowerCase() %*/FullDTO);
      }/*% if(idx < spatialDims.length-1){ %*/ else /*% } %*/
      /*% }); %*/
    } else {
      /*%= normalize(context.id, true) %*/EntityFullDTO /*%= normalize(context.id, true) %*/Entity =
      /*%= normalize(context.id, true) %*/EntityService.get(id);
      data.put("sensorInfo", /*%= normalize(context.id, true) %*/Entity);
    }
    /*% } %*/
    if (measurementList.size() > 0) {
      DataDTO measurement = measurementList.get(0);
      data.put("measurements", measurement.getData());
    } else {
      data.put("measurements", null);
    }
    result.setData(data);
    return result;
  }

  @Override
  public List<DataDTO> getDataHistogram(Long id, /*%= normalize(context.id, true) %*/StateRequestDto params) {

    return /*%= normalize(context.id, true) %*/Repository.getHistogramDataBySensorId(
      id,
      getStart(params),
      getEnd(params),
      params.getTemporalAggregation(),
      params.getSpatialAggregation(),
      params.getCalcAggregation(),
      params.getProperties(),
      params.getSpatialFilter(),
      /*% if (hasCategoricalDims) { %*/
      params.getCategoryAggregation(),
      params.getCategoryFilter(),
      params.getCategoryFrom(),
      params.getCategoryTo(),
      /*% } %*/
      params.getSpatialFilterId());
  }

  /** PRIVATE METHODS * */
  private LocalDateTime getStart(/*%= normalize(context.id, true) %*/StateRequestDto params) {
    if (params.getTemporalAggregation() == TemporalAggregation.MONTH)
      return LocalDate.now()
          .withYear(params.getStart().getYear())
          .withMonth(params.getStart().getMonthValue())
          .withDayOfMonth(1)
          .atStartOfDay();
    else if (params.getTemporalAggregation() == TemporalAggregation.YEAR)
      return LocalDate.now()
          .withYear(params.getStart().getYear())
          .withMonth(1)
          .withDayOfMonth(1)
          .atStartOfDay();
    else return params.getStart();
  }

  private LocalDateTime getEnd(/*%= normalize(context.id, true) %*/StateRequestDto params) {
    if (params.getTemporalAggregation() == TemporalAggregation.MONTH)
      return LocalDate.now()
          .withYear(params.getEnd().getYear())
          .withMonth(params.getEnd().getMonthValue())
          .with(lastDayOfMonth())
          .atTime(LocalTime.MAX);
    else if (params.getTemporalAggregation() == TemporalAggregation.YEAR)
      return LocalDate.now()
          .withYear(params.getEnd().getYear())
          .withMonth(12)
          .with(lastDayOfMonth())
          .atTime(LocalTime.MAX);
    else return params.getEnd();
  }
}
