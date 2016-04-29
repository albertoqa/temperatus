package temperatus.controller.activation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * When user try to export data and the application has not been activated, show this screen
 * <p>
 * Created by alberto on 29/4/16.
 */
@Controller
public class BuyCompleteAlertController extends AbstractActivationController implements Initializable, AbstractController {

    @FXML private Label activationWarnLabel;

    private static Logger logger = LoggerFactory.getLogger(BuyCompleteAlertController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        init();
        translate();
    }

    /**
     * Unlimited time trial, but cannot export any data.
     */
    @FXML
    private void trial() {
        logger.info("Continuing with trial version...");
        VistaNavigator.closeModal(anchorPane);
    }

    @Override
    public void translate() {
        activationWarnLabel.setText(language.get(Lang.ACTIVATION_WARN_LABEL));
    }
}
