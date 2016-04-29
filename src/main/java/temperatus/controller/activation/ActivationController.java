package temperatus.controller.activation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.util.Constants;
import temperatus.util.KeyValidator;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Allow the user to activate the application, continue the trial version or buy a license key
 * <p>
 * Created by alberto on 26/4/16.
 */
public class ActivationController extends AbstractActivationController implements Initializable {

    private static Logger logger = LoggerFactory.getLogger(ActivationController.class.getName());

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
            startApplication();
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
        logger.info("Continuing trial...");
        startApplication();
    }

    /**
     * Close this stage and load the splash screen window
     */
    private void startApplication() {
        Stage currentStage = (Stage) temperatusImage.getScene().getWindow();    // close current stage
        currentStage.close();

        Stage stage = new Stage();
        Pane pane = VistaNavigator.loader.load(getClass().getResource(Constants.SPLASH));
        Scene scene = new Scene(pane);
        stage.initStyle(StageStyle.UNDECORATED); // remove borders
        stage.setScene(scene);
        stage.show();
    }

}
