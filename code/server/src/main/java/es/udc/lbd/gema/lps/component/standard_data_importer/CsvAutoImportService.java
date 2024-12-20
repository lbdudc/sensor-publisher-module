/*% if (feature.DM_DI_DF_CSV) { %*/
package es.udc.lbd.gema.lps.component.standard_data_importer;

import es.udc.lbd.gema.lps.component.standard_data_importer.custom.ImportColumnJSON;
import es.udc.lbd.gema.lps.component.standard_data_importer.custom.ParseFormatJSON;
import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.apache.commons.io.input.BOMInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
public class CsvAutoImportService {
  // Default values
  private static final String DEFAULT_ENCODING = "UTF-8";
  private static final Character DEFAULT_SEPARATOR = ';';
  private static final String DEFAULT_QUOTE = "\"";
  private static final String DEFAULT_LOCAL_DATE_FORMAT = "yyyyMMdd";
  private static final String DEFAULT_LOCAL_TIME_FORMAT = "HHmmss";
  private static final String DEFAULT_LOCAL_DATETIME_FORMAT = "yyyyMMddHHmmss";
  private static final Integer DEFAULT_SRID = 4326;

  // ImportColumnJSON parameters
  private String encoding = DEFAULT_ENCODING;
  private String quote = DEFAULT_QUOTE;
  private Character separator = DEFAULT_SEPARATOR;
  private static final Boolean STORED_FILE = true;
  private static final Boolean SKIP_FIRST_LINE = true;

  // Files parameters
  private static final String FILE_TYPE = "csv";
  private static final String CSV_FOLDER = "csvs";
  private static final String EXPORT_DATA_FILE = "csvsSpec.json";
  private String packageName = "es.udc.lbd.gema.lps.model";
  private static final String RESOURCE_FILE_SEPARATOR = "/";

  // Json Parameters
  private static final String JSON_FILE_ATTR = "file";
  private static final String JSON_ENTITY_ATTR = "entity";
  private static final String JSON_MAPPING_ATTR = "mapping";
  private static final String JSON_PACKAGE_ATTR = "package";
  private static final String JSON_PROPERTIES_ATTR = "properties";
  private static final String JSON_PROPERTIES_ENCODING_ATTR = "encoding";
  private static final String JSON_PROPERTIES_SEPARATOR_ATTR = "separator";
  private static final String JSON_PROPERTIES_QUOTE_ATTR = "quote";
  private static final String JSON_PROPERTIES_DATE_FORMAT_ATTR = "date_format";
  private static final String JSON_PROPERTIES_TIME_FORMAT_ATTR = "time_format";
  private static final String JSON_PROPERTIES_DATE_TIME_FORMAT_ATTR = "date_time_format";
  private static final String JSON_PROPERTIES_SRID_ATTR = "srid";

  // generateImportColumnJSONArray configuration
  private Integer srid = DEFAULT_SRID;
  private String local_date_format = DEFAULT_LOCAL_DATE_FORMAT;
  private String local_time_format = DEFAULT_LOCAL_TIME_FORMAT;
  private String local_date_time_format = DEFAULT_LOCAL_DATETIME_FORMAT;
  private static final String REGEX_CUSTOM_SUBFIX = "Custom\\..*";

  private final Logger log = LoggerFactory.getLogger(CsvAutoImportService.class);

  @Autowired ResourceLoader resourceLoader;

  @Inject private CsvImportService csvImportService;

  public void execute() {
    log.debug("*** Starting CSV auto-import service ***");

    try {
      JSONParser parser = new JSONParser();
      Resource resource =
        resourceLoader.getResource(
          ResourceUtils.CLASSPATH_URL_PREFIX
            + CSV_FOLDER
            + RESOURCE_FILE_SEPARATOR
            + EXPORT_DATA_FILE);
      JSONArray csvs = (JSONArray) parser.parse(new FileReader(resource.getFile()));
      for (Object csvObject : csvs) {
        JSONObject csv = (JSONObject) csvObject;
        try {
          if (csv.get(JSON_PACKAGE_ATTR) != null) {
            packageName = (String) csv.get(JSON_PACKAGE_ATTR);
          }
          Class<?> entity = Class.forName(packageName + ".domain." + csv.get(JSON_ENTITY_ATTR));
          log.debug("Inserting data to entity " + entity.getSimpleName());
          String fileName = (String) csv.get(JSON_FILE_ATTR);
          JSONObject attrMapping =
            csv.containsKey(JSON_MAPPING_ATTR)
              ? (JSONObject) csv.get(JSON_MAPPING_ATTR)
              : new JSONObject();
          JSONObject propertiesJson =
            csv.containsKey(JSON_PROPERTIES_ATTR)
              ? (JSONObject) csv.get(JSON_PROPERTIES_ATTR)
              : null;
          getPropertiesFromSpec(propertiesJson);
          ImportColumnJSON[] columns =
            generateImportColumnJSONArray(getFirstLine(fileName), entity, attrMapping);
          try {
            csvImportService.processFile(setParseFormatJSONParams(fileName, columns, entity));
          } catch (Exception e) {
            log.error(e.toString());
            log.error("Error adding data entity " + entity.getSimpleName());
          }
        } catch (ClassNotFoundException e) {
          log.error("Entity \"" + csv.get(JSON_ENTITY_ATTR) + "\" was not found.");
        }
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    log.debug("*** Finished CSV auto-import ***");
  }

  private void getPropertiesFromSpec(JSONObject propertiesJson) {
    if (propertiesJson != null) {
      encoding =
        (propertiesJson.get(JSON_PROPERTIES_ENCODING_ATTR) != null)
          ? (String) propertiesJson.get(JSON_PROPERTIES_ENCODING_ATTR)
          : DEFAULT_ENCODING;
      separator =
        (propertiesJson.get(JSON_PROPERTIES_SEPARATOR_ATTR) != null)
          ? ((String) propertiesJson.get(JSON_PROPERTIES_SEPARATOR_ATTR)).charAt(0)
          : DEFAULT_SEPARATOR;
      quote =
        (propertiesJson.get(JSON_PROPERTIES_QUOTE_ATTR) != null)
          ? (String) propertiesJson.get(JSON_PROPERTIES_QUOTE_ATTR)
          : DEFAULT_QUOTE;
      local_date_format =
        (propertiesJson.get(JSON_PROPERTIES_DATE_FORMAT_ATTR) != null)
          ? (String) propertiesJson.get(JSON_PROPERTIES_DATE_FORMAT_ATTR)
          : DEFAULT_LOCAL_DATE_FORMAT;
      local_time_format =
        (propertiesJson.get(JSON_PROPERTIES_TIME_FORMAT_ATTR) != null)
          ? (String) propertiesJson.get(JSON_PROPERTIES_TIME_FORMAT_ATTR)
          : DEFAULT_LOCAL_TIME_FORMAT;
      local_date_time_format =
        (propertiesJson.get(JSON_PROPERTIES_DATE_TIME_FORMAT_ATTR) != null)
          ? (String) propertiesJson.get(JSON_PROPERTIES_DATE_TIME_FORMAT_ATTR)
          : DEFAULT_LOCAL_DATETIME_FORMAT;
      srid =
        (propertiesJson.get(JSON_PROPERTIES_DATE_TIME_FORMAT_ATTR) != null)
          ? ((Long) propertiesJson.get(JSON_PROPERTIES_SRID_ATTR)).intValue()
          : DEFAULT_SRID;
    }
  }

  private ImportColumnJSON[] generateImportColumnJSONArray(
    String header, Class<?> object, JSONObject attrMapping) {
    List<String> columnNames =
      Arrays.stream(header.split(separator.toString()))
        .map(c -> c.trim().replace(quote, ""))
        .collect(Collectors.toList());
    List<ImportColumnJSON> columns = new ArrayList<>();
    for (String columnName : columnNames) {
      if (!attrMapping.isEmpty() && attrMapping.containsKey(columnName))
        columns.add(getFieldFromObject((String) attrMapping.get(columnName), object));
      else columns.add(getFieldFromTableAnotations(columnName, object));
    }
    return columns.toArray(new ImportColumnJSON[0]);
  }

  private ImportColumnJSON getFieldFromObject(String name, Class object) {
    try {
      return createImportColumnJSON( // Para obtener el field se borra Custom del nombre
        object.getDeclaredField(name.replaceAll(REGEX_CUSTOM_SUBFIX, "")), name);
    } catch (NoSuchFieldException e) {
      log.error(
        name.replaceAll(REGEX_CUSTOM_SUBFIX, "")
          + " field is not declared in class "
          + object.getName());
    }
    return null;
  }

  private ImportColumnJSON getFieldFromTableAnotations(String columnName, Class object) {
    for (Field field : object.getDeclaredFields()) {
      if (isSimpleColumn(field, columnName) || isForeignKey(field, columnName)) {
        return createImportColumnJSON(field);
      }
    }
    log.error(columnName + " column is not declared in class " + object.getName());
    return null;
  }

  private boolean isSimpleColumn(Field field, String columnName) {
    return field.isAnnotationPresent(Column.class)
      && field.getAnnotation(Column.class).name().equals(columnName);
  }

  private boolean isForeignKey(Field field, String columnName) {
    return field.isAnnotationPresent(ManyToOne.class)
      && field.isAnnotationPresent(JoinColumn.class)
      && field.getAnnotation(JoinColumn.class).name().equals(columnName);
  }

  private ImportColumnJSON createImportColumnJSON(Field field) {
    return createImportColumnJSON(field, field.getName());
  }

  private ImportColumnJSON createImportColumnJSON(Field field, String columnJSONName) {
    ImportColumnJSON columnJSON = new ImportColumnJSON();
    columnJSON.setName(columnJSONName);
    columnJSON.setType(field.getType().getName());

    // Especial types config, missing MultiPolygon, Point and MultiLineString
    if (field.getType().getName().equals(LocalDate.class.getName()))
      columnJSON.setPattern(local_date_format);
    else if (field.getType().getName().equals(LocalTime.class.getName()))
      columnJSON.setPattern(local_time_format);
    else if (field.getType().getName().equals(LocalDateTime.class.getName()))
      columnJSON.setPattern(local_date_time_format);

    return columnJSON;
  }

  private String getFirstLine(String fileName) throws IOException {
    Resource resource =
      resourceLoader.getResource(
        ResourceUtils.CLASSPATH_URL_PREFIX + CSV_FOLDER + RESOURCE_FILE_SEPARATOR + fileName);
    BOMInputStream inputStream = new BOMInputStream(resource.getInputStream());
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      return br.readLine();
    }
  }

  private ParseFormatJSON setParseFormatJSONParams(
    String fileName, ImportColumnJSON[] columns, Class<?> object) {
    ParseFormatJSON params = new ParseFormatJSON();
    params.setColumns(columns);
    params.setEncoding(encoding);
    params.setEntityName(object.getName());
    params.setFile(CSV_FOLDER + RESOURCE_FILE_SEPARATOR + fileName);
    params.setStoredFile(STORED_FILE);
    params.setNcolumns(columns.length);
    params.setQuote(quote);
    params.setSeparator(separator);
    params.setSkipFirstLine(SKIP_FIRST_LINE);
    params.setType(FILE_TYPE);
    return params;
  }
}
/*% } %*/
