package eventforge.testsupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Test scaffolding: runs a .sql file from the classpath one statement at a time. Not part of the application.
 * Splits on ';' at the end of a line, except inside CREATE TRIGGER ... END; blocks. Skips "--" comment lines.
 */
public final class SqlScript {

    private SqlScript() { }

    /** Runs every statement in the classpath resource on the connection. */
    public static void run(Connection connection, String resource) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String sql : statements(read(resource))) {
                statement.execute(sql);
            }
        }
    }

    /** Splits script text into statements. */
    static List<String> statements(String script) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inTrigger = false;
        for (String line : script.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            current.append(line).append('\n');
            if (trimmed.toUpperCase().startsWith("CREATE TRIGGER")) {
                inTrigger = true;
            }
            boolean ends = inTrigger ? trimmed.equalsIgnoreCase("END;") : trimmed.endsWith(";");
            if (ends) {
                result.add(current.toString());
                current.setLength(0);
                inTrigger = false;
            }
        }
        if (!current.toString().isBlank()) {
            result.add(current.toString());
        }
        return result;
    }

    private static String read(String resource) {
        try (InputStream in = SqlScript.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
