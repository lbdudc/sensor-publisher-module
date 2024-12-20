/*% if (feature.SV_D_DataInsertion && feature.SV_D_DI_Consumers) { %*/
package es.udc.lbd.gema.lps.connectors;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PostgresWriter {
  private static final Logger logger = LoggerFactory.getLogger(PostgresWriter.class);

  @Value("${postgres.ipPort}")
  private String ipPort;

  @Value("${postgres.database}")
  private String database;

  @Value("${postgres.user}")
  private String user;

  @Value("${postgres.pass}")
  private String pass;

  private Connection conn = null;

  public PostgresWriter() {}

  private boolean connected() {
    return conn != null;
  }

  @PostConstruct
  public void connect() {
    try {
      Class.forName("org.postgresql.Driver");
      String url = String.format("jdbc:postgresql://%s/%s", ipPort, database);
      logger.info(url);
      conn = DriverManager.getConnection(url, user, pass);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(2);
    }
  }

  public Connection getConn() {
    return conn;
  }

  public void disconnect() {
    try {
      if (conn != null) {
        conn.close();
        conn = null;
      }
    } catch (SQLException ex) {
      logger.error("Error disconnecting postgres", ex);
    }
  }
}
/*% } %*/
