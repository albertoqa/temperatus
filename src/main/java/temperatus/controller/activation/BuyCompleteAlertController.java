package temperatus.controller.activation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.util.Constants;
import temperatus.util.KeyValidator;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 29/4/16.
 */
@Controller
public class BuyCompleteAlertController extends AbstractActivationController implements Initializable, AbstractController {

    private static Logger logger = LoggerFactory.getLogger(BuyCompleteAlertController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        init();
    }

    /**
     * Check if input activation values are valid. If so, save a preference for the activation.
     */
    @FXML
    private void activate() {
        logger.info("Activating application...");
        if (KeyValidator.validate(mailInput.getText(), keyInput.getText())) {
            logger.info("Application activated!");
            Constants.prefs.putBoolean(Constants.ACTIVATED, true);  // save activated state :D
            // TODO show thank alert
            VistaNavigator.closeModal(anchorPane);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, Language.getInstance().get(Lang.ERROR_INVALID_CREDENTIALS));
            alert.show();
        }
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

    }
}
