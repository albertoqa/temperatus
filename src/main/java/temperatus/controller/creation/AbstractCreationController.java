package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import temperatus.controller.AbstractController;
import temperatus.util.Animation;
import temperatus.util.VistaNavigator;

/**
 * Creation controllers (for example: newGame, newSubject...) have save/cancel button and a titledPane
 * they must implement save/cancel
 * <p>
 * Created by alberto on 27/1/16.
 */
public abstract class AbstractCreationController implements AbstractController {

    @FXML TitledPane titledPane;
    @FXML Button saveButton;
    @FXML Button cancelButton;

    abstract void save();

    @FXML
    private void cancel() {
        Animation.fadeInOutClose(titledPane);
        VistaNavigator.baseController.selectBase();
    }

    protected void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message);
        alert.show();
    }

}
