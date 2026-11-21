package eventforge.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Starts EventForge (Design §7). Wires DatabaseManager, the DAOs and the services by constructor injection,
 * then shows the Login screen. Run with: mvn javafx:run
 */
public class App extends Application {
    /** Database file created next to the application on first run. */
    public static final String DATABASE_URL = "jdbc:sqlite:eventforge.db";

    @Override
    public void start(Stage stage) {
        // TODO P8: build the services, call createSchema(), load login.fxml into a BorderPane, show the stage.
        throw new UnsupportedOperationException("Not implemented");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
