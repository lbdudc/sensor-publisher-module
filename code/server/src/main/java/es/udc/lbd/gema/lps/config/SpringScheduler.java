package es.udc.lbd.gema.lps.config;

import org.springframework.context.annotation.Configuration;
/*% if (feature.T_FileUploader ||
    feature.MV_MS_GJ_Cached) { %*/
import es.udc.lbd.gema.lps.component.scheduled_jobs.*;
/*% } %*/
/*% if (feature.T_Quartz) { %*/
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.scheduling.quartz.*;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
/*% } else { %*/
import jakarta.inject.Inject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
/*% } %*/

@Configuration
/*% if (feature.T_Quartz) { %*/
@EnableAutoConfiguration
/*% } else { %*/
@EnableScheduling
/*% } %*/
public class SpringScheduler {

  /*% if (feature.T_Quartz) { %*/
  @Autowired private QuartzProperties quartzProperties;
  @Autowired private ApplicationContext applicationContext;
      /*% if (feature.T_FileUploader) { %*/
  private final String DAILY_AT_SIX = "0 0 6 ? * * *";
      /*% } %*/


  @Bean
  public SpringBeanJobFactory springBeanJobFactory() {
    AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();

    jobFactory.setApplicationContext(applicationContext);
    return jobFactory;
  }

  @Bean
  public SchedulerFactoryBean scheduler(DataSource quartzDataSource) {

    SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

    schedulerFactory.setJobFactory(springBeanJobFactory());
    schedulerFactory.setSchedulerName("qrtz_scheduler");
    schedulerFactory.setQuartzProperties(asProperties(quartzProperties.getProperties()));

    // All jobs to schedule
    Trigger[] triggers = {

        /*% if (feature.T_FileUploader) { %*/
      removeOldTemporaryFilesTrigger().getObject(),
        /*% } %*/

        /*% if (feature.MV_MS_GJ_Cached) { %*/
      generateSpatialFilesTrigger().getObject(),
        /*% } %*/
    };
    schedulerFactory.setTriggers(triggers);
    schedulerFactory.setDataSource(quartzDataSource);

    return schedulerFactory;
  }

  /*
   * Jobs & triggers
   */


      /*% if (feature.T_FileUploader) { %*/
  @Bean
  public JobDetailFactoryBean removeOldTemporaryFilesJobDetail() {
    JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
    jobDetailFactory.setJobClass(RemoveOldTemporaryFilesJob.class);
    jobDetailFactory.setDurability(true);
    jobDetailFactory.setGroup("remove-files");
    return jobDetailFactory;
  }

  @Bean
  public CronTriggerFactoryBean removeOldTemporaryFilesTrigger() {
    CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
    trigger.setJobDetail(removeOldTemporaryFilesJobDetail().getObject());
    trigger.setGroup("remove-files");
    trigger.setCronExpression(DAILY_AT_SIX);
    return trigger;
  }
      /*% } %*/

        /*% if (feature.MV_MS_GJ_Cached) { %*/
  @Bean
  public JobDetailFactoryBean generateSpatialFilesJobDetail() {
    JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
    jobDetailFactory.setJobClass(GenerateSpatialFilesJob.class);
    jobDetailFactory.setDurability(true);
    jobDetailFactory.setGroup("initial-data");
    return jobDetailFactory;
  }
  @Bean
  public SimpleTriggerFactoryBean generateSpatialFilesTrigger() {
    SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
    trigger.setJobDetail(generateSpatialFilesJobDetail().getObject());
    trigger.setGroup("initial-data");
    trigger.setRepeatCount(0);
    return trigger;
  }
        /*% } %*/

  /*
   * Util methods
   */

  private java.util.Properties asProperties(Map source) {
    java.util.Properties properties = new java.util.Properties();
    properties.putAll(source);
    return properties;
  }
  /*% } else { %*/

      /*% if (feature.T_FileUploader) { %*/
  private final String DAILY_AT_SIX = "0 0 6 * * *";
  @Inject private RemoveOldTemporaryFilesJob removeOldTemporaryFilesJob;

      /*% } %*/

      /*% if (feature.MV_MS_GJ_Cached) { %*/
  @Inject private GenerateSpatialFilesJob generateSpatialFilesJob;

      /*% } %*/


      /*% if (feature.T_FileUploader) { %*/
  @Scheduled(cron = DAILY_AT_SIX)
  public void removeOldTemporaryFiles() {
    removeOldTemporaryFilesJob.execute();
  }
      /*% } %*/


      /*% if (feature.MV_MS_GJ_Cached) { %*/
  @EventListener(ApplicationReadyEvent.class)
  public void generateSpatialFiles() {
    generateSpatialFilesJob.execute();
  }
      /*% } %*/
  /*% } %*/
}
