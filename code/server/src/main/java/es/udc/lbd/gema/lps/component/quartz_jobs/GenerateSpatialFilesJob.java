/*% if (feature.SensorViewer) { %*/
package es.udc.lbd.gema.lps.component.quartz_jobs;

import es.udc.lbd.gema.lps.model.service.FileService;
import java.io.IOException;
import jakarta.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateSpatialFilesJob implements Job {

  @Inject FileService fileService;

  private static final Logger logger = LoggerFactory.getLogger(GenerateSpatialFilesJob.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      fileService.writeSpatialFiles();
    } catch (IOException e) {
      logger.error("Error creating spatial files: {}", e.getMessage());
    }
  }
}
/*% } %*/
