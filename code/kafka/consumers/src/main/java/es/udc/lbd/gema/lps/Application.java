/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Consumers) { %*/
package es.udc.lbd.gema.lps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application implements WebMvcConfigurer {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
/*% } %*/
