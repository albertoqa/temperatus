package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import temperatus.exception.ControlledTemperatusException;
import temperatus.util.Animation;

/**
 * Created by alberto on 27/1/16.
 */
public abstract class AbstractCreation {

    @FXML TitledPane titledPane;
    @FXML Button saveButton;
    @FXML Button cancelButton;

    abstract void save() throws ControlledTemperatusException;
    abstract void translate();

    @FXML
    private void cancel() {
        Animation.fadeInOutClose(titledPane);
    }

    protected void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message);
        alert.show();
    }

}
