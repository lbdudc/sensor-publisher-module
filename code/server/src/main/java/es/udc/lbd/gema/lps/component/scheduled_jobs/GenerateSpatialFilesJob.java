/*% if (feature.MV_MS_GJ_Cached) { %*/
package es.udc.lbd.gema.lps.component.scheduled_jobs;

import es.udc.lbd.gema.lps.model.service.FileService;
import java.io.IOException;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*% if (feature.T_Quartz) { %*/
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
/*% } else { %*/
import org.springframework.stereotype.Component;
/*% } %*/

/*% if (!feature.T_Quartz) { %*/@Component/*% } %*/
public class GenerateSpatialFilesJob /*% if (feature.T_Quartz) { %*/implements Job/*% } %*/ {
  @Inject FileService fileService;

  private static final Logger logger = LoggerFactory.getLogger(GenerateSpatialFilesJob.class);

  /*% if (feature.T_Quartz) { %*/
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    /*% } else { %*/
    public void execute() {
      /*% } %*/
    try {
      fileService.writeSpatialFiles();
    } catch (IOException e) {
      logger.error("Error creating spatial files: {}", e.getMessage());
    }
  }
}
/*% } %*/
