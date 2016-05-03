package temperatus.controller;

import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.lang.Language;
import temperatus.listener.DatabaseThreadFactory;
import temperatus.util.VistaNavigator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract class for all controllers
 * <p>
 * Created by alberto on 27/1/16.
 */
public interface AbstractController {

    Language language = Language.getInstance(); // Only instance of language used in the application

    /* Executor is used to perform long operations in a different thread than the UI elements
        in this case, is used to load elements from the DB. ThreadPool is set to 1 to ensure that
        only one database operation is performed at a time*/
    ExecutorService databaseExecutor = Executors.newFixedThreadPool(1, new DatabaseThreadFactory());    // executes database operations concurrent to JavaFX operations.

    Logger history = LoggerFactory.getLogger("HISTORY");    // write the history of use of the application to a file

    /**
     * Reload a given object
     *
     * @param object object to reload
     */
    default void reload(Object object) {
    }

    /**
     * Translate all labels, buttons, titles... for a view
     */
    void translate();

    /**
     * Show a new alert to the user
     *
     * @param alertType type of alert
     * @param message   message to show
     */
    default void showAlert(Alert.AlertType alertType, String message) {
        VistaNavigator.showAlert(alertType, message);
    }
}
