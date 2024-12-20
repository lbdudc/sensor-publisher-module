/*% if (feature.DM_DI_DF_CSV) { %*/
package es.udc.lbd.gema.lps.component.standard_data_importer.custom;

import java.net.URL;
import org.apache.commons.lang3.StringUtils;

public class FormatCsv extends FormatCommon {
    private ImportColumnJSON[] format;
    private String temporaryFileName;
    private String fileName;
    private URL url;
    private String urlString;
    private String encoding;
    private char separator;
    private char quote;
    private boolean skipFirstLine;
    private String entityClazz;
    private int ncolumns;

    public String getEntityClazz() {
        return entityClazz;
    }

    public void setEntityClazz(String entityClazz) {
        this.entityClazz = entityClazz;
    }

    public ImportColumnJSON[] getFormat() {
        return format;
    }

    public void setFormat(ImportColumnJSON[] format) {
        this.format = format;
    }

    public boolean isSkipFirstLine() {
        return skipFirstLine;
    }

    public void setSkipFirstLine(boolean skipFirstLine) {
        this.skipFirstLine = skipFirstLine;
    }

    public String getTemporaryFileName() {
        return temporaryFileName;
    }

    public void setTemporaryFileName(String temporaryFileName) {
        this.temporaryFileName = temporaryFileName;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
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

    public int getNcolumns() {
        return ncolumns;
    }

    public void setNcolumns(int ncolumns) {
        this.ncolumns = ncolumns;
    }

    /**
     * @return File extension obtained from file or from url
     */
    @Override
    public String getExtension() {
        if (temporaryFileName != null) {
            return StringUtils.substringAfterLast(temporaryFileName, ".");
        }
        if (urlString != null) {
            return StringUtils.substringAfterLast(urlString, ".");
        }
        return null;
    }
}
/*% } %*/
