package eventforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for the CSV report export (UC-8 step 5). */
class CsvExporterTest {
    private final CsvExporter csv = new CsvExporter();

    @Test
    @DisplayName("[UC-8] Values with commas or quotes are quoted so columns stay aligned in Excel")
    void uc8_quotingRules() {
        assertEquals("plain,\"Lee, Sam\",\"say \"\"hi\"\"\",",
                csv.formatLine(List.of("plain", "Lee, Sam", "say \"hi\"", "")));
    }

    @Test
    @DisplayName("[UC-8] Export writes a header line and one line per row")
    void uc8_writesFile(@TempDir Path folder) throws Exception {
        Path file = folder.resolve("report.csv");
        csv.write(file, List.of("Event", "Confirmed"),
                List.of(List.of("Prop Making 101", "1"), List.of("Cosplay Photoshoot", "8")));
        assertEquals(List.of("Event,Confirmed", "Prop Making 101,1", "Cosplay Photoshoot,8"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }
}
