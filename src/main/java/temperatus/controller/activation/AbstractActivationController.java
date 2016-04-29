package temperatus.controller.activation;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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

/**
 * Common variables and methods for the activation windows
 * <p>
 * Created by alberto on 29/4/16.
 */
abstract class AbstractActivationController {

    @FXML AnchorPane anchorPane;

    @FXML private TextField mailInput;
    @FXML private TextArea keyInput;

    @FXML private Label activationInfoLabel;

    @FXML ImageView temperatusImage;

    @FXML private Button activateButton;
    @FXML private Button buyButton;
    @FXML private Button trialButton;

    private boolean isMailEmpty = true;
    private boolean isKeyEmpty = true;

    private static final String CHECKOUT_IMAGE = "/images/icon/checkout.png";
    //private static final String TRIAL_IMAGE = "/images/icon/trial.png";
    private static final String PROJECT_WEB = "http://albertoqa.github.io/temperatusWeb/";

    private static Logger logger = LoggerFactory.getLogger(AbstractActivationController.class.getName());

    void init() {
        translate();

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

    /**
     * Check if input activation values are valid. If so, save a preference for the activation.
     */
    @FXML
    private void activate() {
        logger.info("Activating application...");
        if (KeyValidator.validate(mailInput.getText(), keyInput.getText())) {
            logger.info("Application activated!");
            Constants.prefs.putBoolean(Constants.ACTIVATED, true);  // save activated state :D
            showTanks();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, Language.getInstance().get(Lang.ERROR_INVALID_CREDENTIALS));
            alert.show();
        }
    }

    /**
     * Show thank screen
     */
    private void showTanks() {
        Stage stage = new Stage();
        Pane pane = VistaNavigator.loader.load(getClass().getResource(Constants.THANKS));
        ThanksController thanksController = VistaNavigator.loader.getController();
        Scene scene = new Scene(pane);
        stage.setScene(scene);

        if (temperatusImage != null) {
            Stage currentStage = (Stage) temperatusImage.getScene().getWindow();    // close current stage
            currentStage.close();
            thanksController.setLoadSplash(true);
            stage.setOnCloseRequest(we -> startApplication() );
        } else {
            VistaNavigator.closeModal(anchorPane);
            thanksController.setLoadSplash(false);
        }

        stage.show();
    }

    /**
     * Close this stage and load the splash screen window
     */
    void startApplication() {
        ((Stage) temperatusImage.getScene().getWindow()).close();    // close current stage
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.setScene(new Scene(VistaNavigator.loader.load(getClass().getResource(Constants.SPLASH))));
        stage.show();
    }

    private void translate() {
        activationInfoLabel.setText(Language.getInstance().get(Lang.ACTIVATION_INFO_LABEL));
        activateButton.setText(Language.getInstance().get(Lang.ACTIVATE_BUTTON));
        trialButton.setText(Language.getInstance().get(Lang.TRIAL_BUTTON));
        buyButton.setText(Language.getInstance().get(Lang.BUY_BUTTON));
    }

}
