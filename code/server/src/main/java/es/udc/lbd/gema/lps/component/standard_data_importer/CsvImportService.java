/*% if (feature.DM_DI_DF_CSV) { %*/
package es.udc.lbd.gema.lps.component.standard_data_importer;

import es.udc.lbd.gema.lps.component.standard_data_importer.custom.FormatCsv;
import es.udc.lbd.gema.lps.component.standard_data_importer.custom.ImportFileJSON;
import es.udc.lbd.gema.lps.component.standard_data_importer.custom.ImportParamsCsv;
import es.udc.lbd.gema.lps.component.standard_data_importer.custom.ParseFormatJSON;
import es.udc.lbd.gema.lps.component.standard_data_importer.exceptions.FileNotSupportedException;
import es.udc.lbd.gema.lps.component.standard_data_importer.exceptions.ImportException;
import es.udc.lbd.gema.lps.component.standard_data_importer.exceptions.TypeNotSupportedException;
import es.udc.lbd.gema.lps.component.standard_data_importer.exceptions.client.ImportEncodingNotProvidedException;
import es.udc.lbd.gema.lps.component.standard_data_importer.exceptions.client.ImportFileAndUrlNotProvidedException;
import es.udc.lbd.gema.lps.component.standard_data_importer.exceptions.client.ImportFileUnreadableException;
import es.udc.lbd.gema.lps.component.standard_data_importer.exceptions.client.ImportWrongColumnSizeException;
import es.udc.lbd.gema.lps.model.service.exceptions.AppRuntimeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.RFC4180Parser;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.CSVReaderBuilder;

@Service
public class CsvImportService extends StandardDataImportService {

    private static final Logger logger = LoggerFactory.getLogger(CsvImportService.class);

    private final String IMPORT_TYPE = "csv";
    private final String CSV = "csv";

    /**
     * If import type is valid: Stores the file in a temporary file and reads the header of the file
     * @param request The request
     * @return JSON
     * @throws ImportException If any import exception
     * @throws FileNotSupportedException If file is not valid
     * @throws TypeNotSupportedException If import type is not valid
     */
    @Override
    ImportFileJSON uploadAndProcessHeader(MultipartHttpServletRequest request)
        throws ImportException, FileNotSupportedException, TypeNotSupportedException {

        // Obtain parameters from request
        ImportParamsCsv p = getParamsHeader(request);

        ImportFileJSON result = new ImportFileJSON();

        if (p.getFile() != null) {
            // Store the uploaded file in a temporary folder
            // This file will be deleted after a successful import or next day
            // via a cron job
            File file = fileUploadImport.saveTemporaryFile(p.getFile());
            result.setValues(readHeaderFromCsv(file, null, p.getSeparator(), p.getQuote(), p.getEncoding()));
            result.setTemporaryFile(file.getName());
        } else if (p.getUrl() != null) {
            URL url;
            try {
                url = new URL(p.getUrl());
            } catch (MalformedURLException e) {
                throw new AppRuntimeException("URL: " + p.getUrl() + " is not valid", e);
            }
            result.setValues(readHeaderFromCsv(null, url, p.getSeparator(), p.getQuote(), p.getEncoding()));
            result.setUrl(p.getUrl());
        } else {
            throw new ImportFileAndUrlNotProvidedException();
        }

        checkNoValuesRepeated(result.getValues());
        result.setNcolumns(result.getValues().length);
        return result;
    }

    /**
     * Reads the first line of the CVS file
     * @param file Provided file (Optional)
     * @param url Url of the file (Optional)
     * @param separator Separator char
     * @param quote Quote char
     * @param encoding Encoding
     * @return Header a JSON String
     * @throws ImportException If any controlled exception happened
     */
    private String[] readHeaderFromCsv(File file, URL url, char separator, char quote, String encoding)
        throws ImportException {
        String[] result = null;

        // CSV Reader
        try(BufferedReader br = getBufferedReader(file, url, encoding)) {
            // We must create the reader from the specified source

            // Do not skip any line
            RFC4180Parser customCsvParser = new RFC4180ParserBuilder()
                .withQuoteChar(quote)
                .withSeparator(separator)
                .build();

            try (CSVReader reader = new CSVReaderBuilder(br).withCSVParser(customCsvParser).build()) {

                // This time we only need to read the first row
                boolean exit = false;
                try {
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null && !exit) {
                        result = nextLine;
                        exit = true;
                    }
                } catch (IOException e) {
                    throw new AppRuntimeException("Unable to read next line", e);
                }
            }
        } catch (IOException e) {
            throw new AppRuntimeException("Error closing reader", e);
        }

        return result;
    }

    /**
     * Obtains parameters from request.
     *
     * @param request the request
     * @return Parameters obtained from request
     * @throws ImportException
     *             No file or url were provided
     * @throws FileNotSupportedException
     *             File format not supported
     */
    private ImportParamsCsv getParamsHeader(MultipartHttpServletRequest request)
        throws ImportException, FileNotSupportedException, TypeNotSupportedException {

        // Get params form request
        ImportParamsCsv p = new ImportParamsCsv();

        // Check type
        String type = request.getParameter("type");
        if (!type.equals(IMPORT_TYPE)) {
            throw new TypeNotSupportedException();
        }

        Set<String> validExtensions = new HashSet<String>(Collections.singletonList(CSV));

        // Check File or URL provided
        if (request.getParameter("url") != null) {
            p.setUrl(checkAndGetUrl(request.getParameter("url"), validExtensions));
        } else {
            p.setFile(checkAndGetFile(request.getFile("file"), validExtensions));
        }

        // Check if encoding is provided
        if (StringUtils.isBlank(request.getParameter("encoding"))) {
            throw new ImportEncodingNotProvidedException();
        }
        p.setEncoding(request.getParameter("encoding"));

        String quote = request.getParameter("quote");
        char quotechar = StringUtils.isNotBlank(quote) ? quote.charAt(0) : CSVParser.NULL_CHARACTER;
        p.setQuote(quotechar);

        String separator = request.getParameter("separator");
        char separatorchar = separator.charAt(0);
        p.setSeparator(separatorchar);

        return p;
    }

    /**
     * Imports the file content into DB
     *
     * @return Temporary file used
     */
    @Override
    public File processFile(ParseFormatJSON f) throws ImportException, FileNotSupportedException, TypeNotSupportedException {

        FormatCsv params = getParamsBody(f);

        // Validate format
        validateFormat(params.getFormat());

        // Obtain target object class and instance
        Class<?> clazzObject;
        try {
            clazzObject = Class.forName(params.getEntityClazz());
        } catch (ClassNotFoundException e) {
            throw new AppRuntimeException("Unable to obtain class for " + params.getEntityClazz(), e);
        }

        // Obtain repository class and save method
        String clazzRepositoryPackage = clazzObject.getPackage().getName().replace("domain", "repository");
        String clazzRepositoryName = clazzObject.getSimpleName() + "Repository";
        Class<?> clazzRepository;
        try {
            clazzRepository = Class.forName(clazzRepositoryPackage + "." + clazzRepositoryName);
        } catch (ClassNotFoundException e) {
            throw new AppRuntimeException("Unable to obtain class for " + clazzRepositoryName, e);
        }

        // WARNING: We need to declare a .save method in every Repository class!
        // => Try to avoid this
        Method saveMethod;
        try {
            saveMethod = clazzRepository.getMethod("save", clazzObject);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new AppRuntimeException("Unable to invoke 'save' method", e);
        }

        this.importFromCsvFile(params, clazzObject, clazzRepositoryName, saveMethod);

        return params.getFile();
    }

    /**
     * Check format values
     *
     * @param format the format
     * @return Parameters
     * @throws ImportException
     *             No file or url were provided
     * @throws FileNotSupportedException
     *             File format not supported
     */
    private FormatCsv getParamsBody(ParseFormatJSON format) throws ImportException, FileNotSupportedException, TypeNotSupportedException {
        // Check type
        if (!format.getType().equals(IMPORT_TYPE)) {
            throw new TypeNotSupportedException();
        }

        FormatCsv p = new FormatCsv();

        Set<String> validExtensions = new HashSet<String>(Collections.singletonList(CSV));

        if (format.getUrl() != null) {
            p.setUrlString(checkAndGetUrl(format.getUrl(), validExtensions));
        } else {
            if (format.getStoredFile() != null && format.getStoredFile()) {
              p.setFileName(checkAndGetFile(format.getFile(), validExtensions));
            } else {
              p.setTemporaryFileName(checkAndGetFile(format.getFile(), validExtensions));
            }
        }

        // Get rest of parameters
        p.setEntityClazz(format.getEntityName());
        p.setFormat(format.getColumns());
        p.setSkipFirstLine(format.isSkipFirstLine());
        p.setSeparator(format.getSeparator());
        p.setNcolumns(format.getNcolumns());

        String quote = format.getQuote();
        char quotechar = StringUtils.isNotBlank(quote) ? quote.charAt(0) : CSVParser.NULL_CHARACTER;
        p.setQuote(quotechar);

        if (p.getFileName() != null) {
          File file = new File(p.getFileName());
          if (file.isAbsolute()) {
            p.setFile(file);
          } else {
            URL url = getClass().getResource("/" + p.getFileName());
            p.setFile(new File(url.getPath()));
          }
        } else if (p.getTemporaryFileName() != null) {
            p.setFile(fileUploadImport.getTemporaryFile(p.getTemporaryFileName()));
        } else if (StringUtils.isNotBlank(p.getUrlString())) {
            try {
                p.setUrl(new URL(p.getUrlString()));
            } catch (MalformedURLException e) {
                throw new AppRuntimeException("URL: " + p.getUrlString() + " is not valid", e);
            }
        }

        // Check if encoding is provided
        if (StringUtils.isBlank(format.getEncoding())) {
            throw new ImportEncodingNotProvidedException();
        }
        p.setEncoding(format.getEncoding());

        return p;

    }

    private void importFromCsvFile(FormatCsv p, Class<?> clazzObject, String clazzRepositoryName, Method saveMethod)
        throws ImportException {



        try (BufferedReader br = getBufferedReader(p.getFile(), p.getUrl(), p.getEncoding())){
            // We must to create the reader from the specified source
            // Read rows one by one to be able to work with big files.
            // Need to skip the first line?
            RFC4180Parser customCsvParser = new RFC4180ParserBuilder()
                .withQuoteChar(p.getQuote())
                .withSeparator(p.getSeparator())
                .build();

            String[] line;
            try (CSVReader reader = new CSVReaderBuilder(br).withSkipLines(p.isSkipFirstLine() ? 1 : 0).withCSVParser(customCsvParser).build()) {
                while ((line = reader.readNext()) != null) {

                    if (line.length != p.getNcolumns()) {
                        throw new ImportWrongColumnSizeException(Arrays.toString(line));
                    }
                    // Populate entity fields
                    Object instanceObject = fillObjectWithData(p.getFormat(), line, clazzObject);
                    // Save entity
                    saveEntity(clazzRepositoryName, saveMethod, instanceObject);
                }
            } catch (IOException e) {
                throw new AppRuntimeException("Unable to read line", e);
            }
        } catch (IOException e) {
            throw new AppRuntimeException("Error closing reader", e);
        }
    }

    private BufferedReader getBufferedReader(File file, URL url, String encoding) throws ImportException {
        try {
            if (file != null) {
                return new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            } else if (url != null) {
                return new BufferedReader(new InputStreamReader(url.openStream(), encoding));
            } else {
                throw new AppRuntimeException("Neither file or url provided");
            }
        } catch (IOException e) {
            throw new ImportFileUnreadableException();
        }
    }

}
/*% } %*/
