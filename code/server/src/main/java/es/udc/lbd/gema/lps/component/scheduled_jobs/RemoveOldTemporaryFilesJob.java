/*% if (feature.T_FileUploader) { %*/
package es.udc.lbd.gema.lps.component.scheduled_jobs;

import java.io.IOException;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.udc.lbd.gema.lps.component.file_uploader.CronTasks;
  /*% if (feature.T_Quartz) { %*/
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
  /*% } else { %*/
import org.springframework.stereotype.Component;
  /*% } %*/

/*% if (!feature.T_Quartz) { %*/@Component/*% } %*/
public class RemoveOldTemporaryFilesJob /*% if (feature.T_Quartz) { %*/ implements Job/*% } %*/ {

  @Inject private CronTasks cronTasks;

  private final Logger log = LoggerFactory.getLogger(RemoveOldTemporaryFilesJob.class);

/*% if (feature.T_Quartz) { %*/
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
  /*% } else { %*/
  public void execute() {
  /*% } %*/
    try {
      cronTasks.removeOldTemporaryFiles();
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }
}
/*% } %*/
