/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.model.service.sensor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import es.udc.lbd.gema.lps.model.service.dto.sensor.InstantDTO;

public interface InstantProviderService {
    Page<InstantDTO> findInstantsByDate(String sensorId, LocalDate date, Pageable pageable);

  Page<InstantDTO> findDayInstantsByDate(String sensorId, LocalDate date, Pageable pageable);

  Page<InstantDTO> findMonthInstantsByDate(String sensorId, LocalDate date, Pageable pageable);

  Page<InstantDTO> findYearInstants(String sensorId, Pageable pageable);

  List<InstantDTO<Object>> findAllYearInstants(String sensorId);

  String findLastInstant(String sensorId);

  Page<InstantDTO> findHourInstantsByDate(String sensorId, LocalDate date, Pageable pageable);
}
/*% } %*/
