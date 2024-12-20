/*% if (feature.DM_DI_DF_CSV) { %*/
package es.udc.lbd.gema.lps.component.standard_data_importer.exceptions.client;

import es.udc.lbd.gema.lps.component.standard_data_importer.exceptions.ImportException;

import org.springframework.http.HttpStatus;

public class ImportWrongColumnSizeException extends ImportException {

    public ImportWrongColumnSizeException(String param) {
        super("import.wrong_column_size", param, HttpStatus.BAD_REQUEST);
        logger.warn("Import: Column '{}' has invalid number of columns.", param);
    }
}
/*% } %*/
