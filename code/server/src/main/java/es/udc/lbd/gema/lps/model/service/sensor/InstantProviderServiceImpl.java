/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.model.service.sensor;

import es.udc.lbd.gema.lps.model.repository.sensor.GenericRepository;
import es.udc.lbd.gema.lps.model.service.dto.sensor.InstantDTO;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InstantProviderServiceImpl implements InstantProviderService {
  @Inject
  private GenericRepository instantProviderRepository;

  @Override
  public Page<InstantDTO> findInstantsByDate(String sensorId, LocalDate date, Pageable pageable) {
    return instantProviderRepository
        .getInstantsByDate(
            sensorId,
            date.atStartOfDay(),
            date.atTime(LocalTime.MAX),
            pageable)
        .map(d -> new InstantDTO<>(d, "H:mm"));
  }

  @Override
  public Page<InstantDTO> findHourInstantsByDate(
      String sensorId, LocalDate date, Pageable pageable) {
    return instantProviderRepository
        .getHourInstantsByDate(
            sensorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), pageable)
        .map(
            d -> {
              LocalDateTime localDate = d.atZone(ZoneId.systemDefault()).toLocalDateTime();
              return new InstantDTO<>(localDate, String.valueOf(localDate.getHour()));
            });
  }

  @Override
  public Page<InstantDTO> findDayInstantsByDate(
      String sensorId, LocalDate date, Pageable pageable) {
    return instantProviderRepository
        .getDayInstantsByDate(
            sensorId, date.atStartOfDay(), date.plusMonths(1).atStartOfDay(), pageable)
        .map(
            d -> {
              LocalDateTime localDate = d.atZone(ZoneId.systemDefault()).toLocalDateTime();
              return new InstantDTO<>(localDate, String.valueOf(localDate.getDayOfMonth()));
            });
  }

  @Override
  public Page<InstantDTO> findMonthInstantsByDate(
      String sensorId, LocalDate date, Pageable pageable) {
    return instantProviderRepository
        .getMonthInstantsByDate(
            sensorId, date.atStartOfDay(), date.plusYears(1).atStartOfDay(), pageable)
        .map(
            d -> {
              LocalDateTime localDate = d.atZone(ZoneId.systemDefault()).toLocalDateTime();
              return new InstantDTO<>(localDate, String.valueOf(localDate.getMonthValue()));
            });
  }

  @Override
  public Page<InstantDTO> findYearInstants(String sensorId, Pageable pageable) {
    return instantProviderRepository
        .getYearInstantsByDate(sensorId, pageable)
        .map(
            d -> {
              LocalDateTime localDate = d.atZone(ZoneId.systemDefault()).toLocalDateTime();
              return new InstantDTO<>(localDate, String.valueOf(localDate.getYear()));
            });
  }

  @Override
  public List<InstantDTO<Object>> findAllYearInstants(String sensorId) {
      return instantProviderRepository
          .getYearInstantsByDate(sensorId, Pageable.unpaged())
          .stream() 
          .map(d -> new InstantDTO<>(
              DateTimeFormatter.ofPattern("yyyy")
                               .format(d.atZone(ZoneId.systemDefault()).toLocalDateTime())
          ))
          .collect(Collectors.toList());
  }

  @Override
  public String findLastInstant(String sensorId) {
    return instantProviderRepository
        .getLastInstant(sensorId)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }
}
/*% } %*/
