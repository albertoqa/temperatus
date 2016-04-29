package temperatus.controller.activation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Allow the user to activate the application, continue the trial version or buy a license key
 * <p>
 * Created by alberto on 26/4/16.
 */
public class ActivationController extends AbstractActivationController implements Initializable, AbstractController {

    @FXML private Label welcomeLabel;

    private static Logger logger = LoggerFactory.getLogger(ActivationController.class.getName());

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
        logger.info("Continuing trial...");
        startApplication();
    }

    @Override
    public void translate() {
        welcomeLabel.setText(language.get(Lang.WELCOME_LABEL));
    }
}
