package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
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

    @FXML private TextField mailInput;
    @FXML private TextArea keyInput;

    @FXML private ImageView temperatusImage;

    @FXML private Button activateButton;
    @FXML private Button buyButton;
    @FXML private Button trialButton;

    private static Logger logger = LoggerFactory.getLogger(ActivationController.class.getName());

    private boolean isMailEmpty = true;
    private boolean isKeyEmpty = true;

    private static final String PROJECT_WEB = "http://albertoqa.github.io/temperatusWeb/";
    private static final String CHECKOUT_IMAGE = "/images/icon/checkout.png";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Set image only to buy icon to make it more striking
        ImageView imageView = new ImageView(CHECKOUT_IMAGE);
        imageView.setFitHeight(Constants.ICON_SIZE);
        imageView.setFitWidth(Constants.ICON_SIZE);
        buyButton.setGraphic(imageView);

        // Only allow to activate when both inputs have some character
        mailInput.textProperty().addListener((observable, oldValue, mailValue) -> {
            isMailEmpty = mailValue.length() <= 0;
            updateActivateButtonState();
        });

        keyInput.textProperty().addListener((observable, oldValue, keyValue) -> {
            isKeyEmpty = keyValue.length() <= 0;
            updateActivateButtonState();
        });
    }

    /**
     * Check if is valid to try to activate the program
     */
    private void updateActivateButtonState() {
        if (!isMailEmpty && !isKeyEmpty) {
            activateButton.setDisable(false);
        } else {
            activateButton.setDisable(true);
        }
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
     * Open the default browser and go to the project address
     *
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    @FXML
    private void buy() throws MalformedURLException, URISyntaxException {
        logger.info("Redirecting to buy web-page... YUUUHUU");
        URL url = new URL(PROJECT_WEB);
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
     *
     * @param uri page to open
     */
    private static void openWebPage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                logger.error("Error opening default browser... " + e.getMessage());
            }
        }
    }

}
