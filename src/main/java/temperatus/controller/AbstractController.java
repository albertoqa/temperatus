package temperatus.controller;

import javafx.scene.control.Alert;
import temperatus.lang.Language;

/**
 * Abstract class for all controllers
 * <p>
 * Created by alberto on 27/1/16.
 */
public interface AbstractController {

    Language language = Language.getInstance(); // Only instance of language used in the application

    /**
     * Reload a given object
     *
     * @param object object to reload
     */
    default void reload(Object object) {
        System.out.println("Nothing to reload...");
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
        Alert alert = new Alert(alertType, message);
        alert.show();
    }
}
