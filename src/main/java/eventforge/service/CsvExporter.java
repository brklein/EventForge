package eventforge.service;

import java.nio.file.Path;
import java.util.List;

/** Writes report rows as CSV with the standard library (Design §3.1, UC-8 export). UTF-8, comma separated. */
public class CsvExporter {

    /**
     * Writes a header line, then one line per row. A value containing a comma, quote or line break is wrapped in
     * double quotes, with inner quotes doubled (RFC 4180), so names like "Lee, Sam" stay in one column.
     */
    public void write(Path file, List<String> header, List<List<String>> rows) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** Formats one line of CSV, applying the quoting rule above. Does not add the line break. */
    public String formatLine(List<String> values) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
