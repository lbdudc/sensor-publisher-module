/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.web.rest.sensor;

import es.udc.lbd.gema.lps.model.service.sensor.InstantProviderServiceImpl;
import jakarta.inject.Inject;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(InstantProviderResource.INSTANT_PROVIDER_RESOURCE_URL)
public class InstantProviderResource {

  public static final String INSTANT_PROVIDER_RESOURCE_URL = "/api/sensors/instants";

  @Inject private InstantProviderServiceImpl instantProviderService;

  @GetMapping("/{sensorId}/none")
  public @ResponseBody ResponseEntity<?> findInstantsByDate(
      @PathVariable("sensorId") String sensorId,
      @PageableDefault(size = 5) Pageable pageable,
      @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    return new ResponseEntity<>(
        instantProviderService.findInstantsByDate(sensorId, date, pageable), HttpStatus.OK);
  }

  @GetMapping("/{sensorId}/hour")
  public @ResponseBody ResponseEntity<?> findHourInstantsByDate(
      @PathVariable("sensorId") String sensorId,
      @PageableDefault(size = 5) Pageable pageable,
      @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    return new ResponseEntity<>(
        instantProviderService.findHourInstantsByDate(sensorId, date, pageable), HttpStatus.OK);
  }

  @GetMapping("/{sensorId}/day")
  public @ResponseBody ResponseEntity<?> findDayInstantsByDate(
      @PathVariable("sensorId") String sensorId,
      @PageableDefault(size = 5) Pageable pageable,
      @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    return new ResponseEntity<>(
        instantProviderService.findDayInstantsByDate(sensorId, date, pageable), HttpStatus.OK);
  }

  @GetMapping("/{sensorId}/month")
  public @ResponseBody ResponseEntity<?> findMonthInstantsByDate(
      @PathVariable("sensorId") String sensorId,
      @PageableDefault(size = 5) Pageable pageable,
      @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    return new ResponseEntity<>(
        instantProviderService.findMonthInstantsByDate(sensorId, date, pageable), HttpStatus.OK);
  }

  @GetMapping("/{sensorId}/year")
  public @ResponseBody ResponseEntity<?> findYearInstants(
      @PathVariable("sensorId") String sensorId,
      @PageableDefault(size = 5) Pageable pageable) {
    return new ResponseEntity<>(
        instantProviderService.findYearInstants(sensorId, pageable), HttpStatus.OK);
  }

  @GetMapping("/{sensorId}/all-years")
  public @ResponseBody ResponseEntity<?> findAllYearInstants(
      @PathVariable("sensorId") String sensorId) {
    return new ResponseEntity<>(
        instantProviderService.findAllYearInstants(sensorId), HttpStatus.OK);
  }

  @GetMapping("/{sensorId}/last-instant")
  public @ResponseBody ResponseEntity<?> findLastInstant(
      @PathVariable("sensorId") String sensorId
  ) {
    return new ResponseEntity<>(instantProviderService.findLastInstant(sensorId), HttpStatus.OK);
  }
}
/*% } %*/
