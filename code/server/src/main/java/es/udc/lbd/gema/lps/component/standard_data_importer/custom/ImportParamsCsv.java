/*% if (feature.DM_DI_DF_CSV) { %*/
package es.udc.lbd.gema.lps.component.standard_data_importer.custom;

import org.apache.commons.io.FilenameUtils;

public class ImportParamsCsv extends ImportParamsCommon {

    private String url;
    private char separator;
    private char quote;
    private String encoding;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public char getSeparator() {
        return separator;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    public char getQuote() {
        return quote;
    }

    public void setQuote(char quote) {
        this.quote = quote;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return File extension obtained from file or from url
     */
    @Override
    public String getExtension() {
        if (file != null) {
            return FilenameUtils.getExtension(file.getOriginalFilename());
        }
        if (url != null) {
            return FilenameUtils.getExtension(url);
        }
        return null;
    }

}
/*% } %*/
