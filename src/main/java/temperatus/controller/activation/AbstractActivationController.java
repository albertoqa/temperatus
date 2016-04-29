package temperatus.controller.activation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.util.Constants;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by alberto on 29/4/16.
 */
abstract class AbstractActivationController {

    @FXML AnchorPane anchorPane;

    @FXML TextField mailInput;
    @FXML TextArea keyInput;

    @FXML ImageView temperatusImage;

    @FXML private Button activateButton;
    @FXML private Button buyButton;
    @FXML private Button trialButton;

    private boolean isMailEmpty = true;
    private boolean isKeyEmpty = true;

    private static final String CHECKOUT_IMAGE = "/images/icon/checkout.png";
    private static final String PROJECT_WEB = "http://albertoqa.github.io/temperatusWeb/";

    private static Logger logger = LoggerFactory.getLogger(AbstractActivationController.class.getName());

    void init() {
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

}
