package temperatus.controller.button;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Show information about how to configure a mission on the device
 * <p>
 * Created by alberto on 2/5/16.
 */
@Controller
public class StartDeviceMissionHelpController implements Initializable, AbstractController {

    @FXML private StackPane stackPane;
    @FXML private Label headerLabel;
    @FXML private Button backButton;

    private static Logger logger = LoggerFactory.getLogger(StartDeviceMissionHelpController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
    }

    /**
     * Close the window and go back to the previous screen
     */
    @FXML
    private void back() {
        VistaNavigator.closeModal(stackPane);
    }

    @Override
    public void translate() {

    }
}
