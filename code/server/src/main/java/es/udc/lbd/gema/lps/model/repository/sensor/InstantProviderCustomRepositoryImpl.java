/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.model.repository.sensor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class InstantProviderCustomRepositoryImpl implements InstantProviderCustomRepository {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public Page<LocalDateTime> getInstantsByDate(
      String sensorId, LocalDateTime dateInit, LocalDateTime dateEnd, Pageable pageable) {

    String instantsByDateQuery =
        "SELECT DISTINCT(bucket_minute) from agg_minute_"
            + sensorId
            + " where bucket_minute > '"
            + dateInit
            + "' and bucket_minute < '"
            + dateEnd
            + "' order by bucket_minute DESC";

    Query query = entityManager.createNativeQuery(instantsByDateQuery);
    List<Instant> resultList = query.getResultList();
    if (resultList.size() > 0) {
      // Convert each Timestamp to LocalDateTime
      List<LocalDateTime> localDateTimeList =
          resultList.stream()
              .map((Instant instant) -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
              .collect(Collectors.toList());
      // Use PagedListHolder to manage pagination
      PagedListHolder<LocalDateTime> page = new PagedListHolder<>(localDateTimeList);
      page.setPageSize(pageable.getPageSize()); // number of items per page
      page.setPage(pageable.getPageNumber()); // current page

      // Return a new PageImpl from the PagedListHolder
      return new PageImpl<>(page.getPageList(), pageable, page.getNrOfElements());
    } else {
      return Page.empty();
    }
  }

  @Override
  public Page<Instant> getHourInstantsByDate(
      String sensorId, LocalDateTime dateInit, LocalDateTime dateEnd, Pageable pageable) {

    String hoursByDayQuery =
        "SELECT DISTINCT(bucket_hour) from agg_hour_"
            + sensorId
            + " where bucket_hour > '"
            + dateInit
            + "' and bucket_hour < '"
            + dateEnd
            + "' order by bucket_hour DESC";

    Query query = entityManager.createNativeQuery(hoursByDayQuery);
    List<Instant> resultList = query.getResultList();
    return transformTimestampListToPage(resultList, pageable);
  }

  @Override
  public Page<Instant> getDayInstantsByDate(
      String sensorId, LocalDateTime dateInit, LocalDateTime dateEnd, Pageable pageable) {

    String daysByMonthQuery =
        "SELECT DISTINCT(bucket_day) from agg_day_"
            + sensorId
            + " where bucket_day > '"
            + dateInit
            + "' and bucket_day < '"
            + dateEnd
            + "' order by bucket_day DESC";

    Query query = entityManager.createNativeQuery(daysByMonthQuery);
    List<Instant> resultList = query.getResultList();
    return transformTimestampListToPage(resultList, pageable);
  }

  @Override
  public Page<Instant> getMonthInstantsByDate(
      String sensorId, LocalDateTime dateInit, LocalDateTime dateEnd, Pageable pageable) {

    String monthsByYearQuery =
        "SELECT DISTINCT(bucket_month) from agg_month_"
            + sensorId
            + " where bucket_month > '"
            + dateInit
            + "' and bucket_month < '"
            + dateEnd
            + "' order by bucket_month DESC";

    Query query = entityManager.createNativeQuery(monthsByYearQuery);
    List<Instant> resultList = query.getResultList();
    return transformTimestampListToPage(resultList, pageable);
  }

  @Override
  public Page<Instant> getYearInstantsByDate(String sensorId, Pageable pageable) {
    String allYearsQuery =
        " SELECT DISTINCT(bucket_year) from agg_year_" + sensorId + " ORDER BY bucket_year DESC";
    Query query = entityManager.createNativeQuery(allYearsQuery);
    List<Instant> resultList = query.getResultList();
    return transformTimestampListToPage(resultList, pageable);
  }

  @Override
  public LocalDateTime getLastInstant(String sensorId) {

    String lastInstantQuery = "SELECT MIN(bucket_day) from agg_day_" + sensorId;
    Query query = entityManager.createNativeQuery(lastInstantQuery);
    Instant result = (Instant) query.getSingleResult();
    if (result != null) {
	    return LocalDateTime.ofInstant(result, ZoneId.systemDefault());
    }
    return null;
  }

  // ------------ PRIVATE METHODS ------------

  private Page<Instant> transformTimestampListToPage(List<Instant> list, Pageable pageable) {
    if (pageable.isPaged()) {
      int start = (int) pageable.getOffset();
      int end = Math.min((start + pageable.getPageSize()), list.size());
      return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }
    return new PageImpl<>(list);
  }
}
/*% } %*/
