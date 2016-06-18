package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.lang.Lang;
import temperatus.lang.Language;
import temperatus.util.Browser;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * About view controller.
 * <p>
 * Created by alberto on 14/2/16.
 */
@Controller
public class AboutController implements Initializable, AbstractController {

    @FXML private Button buyButton;
    @FXML private Button activateButton;
    @FXML private AnchorPane buyPane;
    @FXML private Label statusLabel;
    @FXML private Hyperlink linkToWeb;
    @FXML private Label contactLabel;

    @FXML private Button openManual;

    @FXML private Hyperlink icons8;
    @FXML private Hyperlink human;
    @FXML private Hyperlink license;

    private static final String CHECKOUT_IMAGE = "/images/icon/checkout.png";

    private static Logger logger = LoggerFactory.getLogger(AboutController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        ImageView imageView = new ImageView(CHECKOUT_IMAGE);
        imageView.setFitHeight(Constants.ICON_SIZE);
        imageView.setFitWidth(Constants.ICON_SIZE);
        buyButton.setGraphic(imageView);

        // Check if the software has been activated
        if(Constants.prefs.getBoolean(Constants.ACTIVATED, false)) {
            buyButton.setVisible(false);
            activateButton.setVisible(false);
            statusLabel.setText(language.get(Lang.STATUS_ACTIVATED));
        } else {
            buyButton.setVisible(true);
            activateButton.setVisible(true);
            statusLabel.setText(language.get(Lang.STATUS_INACTIVE));
        }

        Browser.openWebPage(linkToWeb, Constants.PROJECT_WEB);
        Browser.openWebPage(icons8, "https://icons8.com");
        Browser.openWebPage(human, "https://clara.io/view/d49ee603-8e6c-4720-bd20-9e3d7b13978a/image");
        Browser.openWebPage(license, "http://creativecommons.org/licenses/by/3.0/");
    }

    /**
     * Open the default browser and go to the project address
     */
    @FXML
    private void buy() {
        logger.info("Redirecting to buy web-page... YUUUHUU");
        URL url;
        try {
            url = new URL(Constants.PROJECT_WEB);
            Browser.openWebPage(url.toURI());
        } catch (MalformedURLException | URISyntaxException e) {
            logger.error("Error opening browser to buy the program...");
            VistaNavigator.showAlert(Alert.AlertType.ERROR, Language.getInstance().get(Lang.ERROR_BROWSER));
        }
    }

    /**
     * Activate the application
     */
    @FXML
    private void activate() {
        Stage stage = new Stage();
        Pane pane = VistaNavigator.loader.load(getClass().getResource(Constants.BUY_COMPLETE));
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.getIcons().setAll(new Image(Constants.ICON_BAR));

        Stage currentStage = (Stage) buyButton.getScene().getWindow();    // close current stage
        stage.initOwner(currentStage);
        stage.initModality(Modality.WINDOW_MODAL);

        stage.show();
    }

    /**
     * Open the user manual of the application
     */
    @FXML
    private void openUserManual() {
        // TODO
    }

    @Override
    public void translate() {
        buyButton.setText(language.get(Lang.BUY_BUTTON));
        activateButton.setText(language.get(Lang.ACTIVATE_BUTTON));
        contactLabel.setText(language.get(Lang.CONTACT_LABEL));
    }

}
