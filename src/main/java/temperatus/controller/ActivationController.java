package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.util.Constants;
import temperatus.util.KeyValidator;
import temperatus.util.VistaNavigator;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Allow the user to activate the application, continue the trial version or buy a license key
 * <p>
 * Created by alberto on 26/4/16.
 */
public class ActivationController implements Initializable {

    @FXML private Label mailLabel;
    @FXML private Label keyLabel;

    @FXML private TextField mailInput;
    @FXML private TextArea keyInput;

    @FXML private ImageView temperatusImage;

    @FXML private Button activateButton;
    @FXML private Button buyButton;
    @FXML private Button trialButton;

    private static Logger logger = LoggerFactory.getLogger(ActivationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void activate() {
        logger.info("Activating application...");
        if (KeyValidator.validate(mailInput.getText(), keyInput.getText())) {
            logger.info("Application activated!");
            Constants.prefs.putBoolean(Constants.ACTIVATED, true);  // save activated state :D
            // TODO show thank for buy Temperatus note
            startApplication();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid mail-key. Check values and try again.");
            alert.show();
        }
    }

    @FXML
    private void trial() {
        logger.info("Continuing trial...");
        startApplication();
    }

    @FXML
    private void buy() throws MalformedURLException, URISyntaxException {
        logger.info("Redirecting to buy web-page... YUUUHUU");
        URL url = new URL("http://albertoqa.github.io/temperatusWeb/");
        openWebPage(url.toURI());
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

    /**
     * Open a web-page on the default browser
     * @param uri page to open
     */
    private static void openWebPage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
