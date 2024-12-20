/*%@
  if (!feature.SV_D_DataInsertion || !feature.SV_D_DI_Consumers) return [];
  return data.dataWarehouse.sensors
    .map(function(sen) {
      return {
          fileName: normalize(sen.id, false) + '/' + normalize(sen.id, true) + 'Writer.java',
          context: sen
      };
    });
%*/
package es.udc.lbd.gema.lps.model.service./*%= normalize(context.id, false) %*/;

import es.udc.lbd.gema.lps.connectors.PostgresWriter;
import es.udc.lbd.gema.lps.model.domain./*%= normalize(context.id, true) %*/;
import es.udc.lbd.gema.lps.model.service.conditional.ConditionalOnKafkaProperty;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnKafkaProperty(topic = "/*%= normalize(context.id).toLowerCase() %*/", datasource = "'postgres'")
public class /*%= normalize(context.id, true) %*/Writer {

  private static final Logger logger =
    LoggerFactory.getLogger(/*%= normalize(context.id, true) %*/Writer.class);

  @Autowired private PostgresWriter pgWriter;

  private long startTime = System.currentTimeMillis();
  private double auxTime;
  private long count = 0;

  public void insertMedidas(String tableName, List</*%= normalize(context.id, true) %*/> stream) throws SQLException {

    Connection connection = pgWriter.getConn();
    Statement st = null;
    try {
      st = connection.createStatement();
      int res = insertStatement(stream, tableName, st);
      count += res;
      auxTime = (System.currentTimeMillis() - startTime) / 1.0E03;
      logger.info(
          "PostgresWriter: Inserted {} events | Total: {} events in {} seconds | Events per second: {}",
          res,
          count,
          auxTime,
          (int) (count / auxTime));
    } catch (SQLException ex) {
      logger.error("Error querying postgres when inserting", ex);
      if (ex instanceof PSQLException) {
        pgWriter.connect();
      }
      throw ex;
    } finally {
      if (st != null) {
        try {
          st.close();
        } catch (SQLException ex) {
          logger.warn("Error closing statement when inserting", ex);
        }
      }
    }
  }



  private int insertStatement(List</*%= normalize(context.id, true) %*/> medidas, String tableName, Statement st)
      throws SQLException {
    return st.executeUpdate(
        "insert into "
            + tableName
            + "(id, "
            /*% context.measureData.forEach(function(prop) { %*/
              + "/*%= normalize(prop.name.toLowerCase()) %*/, "
              /*% }); %*/
            /*%
              var entity = data.dataModel.entities.find(function(en) { return en.name == context.entity; });
              if (entity) {
            %*/
              + "/*%= normalize(context.entity.toLowerCase()) %*/Id, "
            /*% } %*/
              + "date, "
             /*%
              var dimensions = [];
              context.dimensions.forEach(function(dim) {
                dim.entities?.forEach(function(en) {
                  if (!dimensions.find(function(d) { return d == en; })) {
                    dimensions.push(en);
                  }
                });
              });

              dimensions.forEach(function(dim, index) {
            %*/
              + "/*%= normalize(dim.toLowerCase()) %*/Id /*% if (index < dimensions.length - 1) { %*/, /*% } %*/"
            /*% }); %*/
            + ") values "
            + medidas.stream().map(r -> r.toSQL()).reduce((a, b) -> a.concat(", ".concat(b))).get()
    );
  }

}
