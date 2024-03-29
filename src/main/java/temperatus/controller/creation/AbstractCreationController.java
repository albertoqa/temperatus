package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import temperatus.controller.AbstractController;
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

    /**
     * Save the entity to database
     */
    abstract void save();

    /**
     * Cancel the creation of the element and close the window
     */
    @FXML
    private void cancel() {
        VistaNavigator.closeModal(titledPane);
    }

}
