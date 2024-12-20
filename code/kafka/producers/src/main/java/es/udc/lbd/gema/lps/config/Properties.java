/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Producers) { %*/
package es.udc.lbd.gema.lps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "properties", ignoreUnknownFields = false)
public class Properties {
  private String clientHost;

  public String getClientHost() {
    return clientHost;
  }

  public void setClientHost(String clientHost) {
    this.clientHost = clientHost;
  }

  private final Gis gis = new Gis();

  public Gis getGis() {
    return gis;
  }

  public static class Gis {
    @Value("${properties.gis.defaultSrid}")
    private int defaultSrid;

    public int getDefaultSrid() {
      return defaultSrid;
    }

    public void setDefaultSrid(int defaultSrid) {
      this.defaultSrid = defaultSrid;
    }

  }
}
/*% } %*/
