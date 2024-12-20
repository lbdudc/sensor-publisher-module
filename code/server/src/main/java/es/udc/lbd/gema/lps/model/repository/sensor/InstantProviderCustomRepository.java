/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.model.repository.sensor;

import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InstantProviderCustomRepository {

  Page<LocalDateTime> getInstantsByDate(
      String sensorId, LocalDateTime dateInit, LocalDateTime dateEnd, Pageable pageable);

  Page<Instant> getHourInstantsByDate(
      String sensorId, LocalDateTime dateInit, LocalDateTime dateEnd, Pageable pageable);

  Page<Instant> getDayInstantsByDate(
      String sensorId, LocalDateTime dateInit, LocalDateTime dateEnd, Pageable pageable);

  Page<Instant> getMonthInstantsByDate(
      String sensorId, LocalDateTime dateInit, LocalDateTime dateEnd, Pageable pageable);

  Page<Instant> getYearInstantsByDate(String sensorId, Pageable pageable);

  LocalDateTime getLastInstant(String sensorId);
}
/*% } %*/
