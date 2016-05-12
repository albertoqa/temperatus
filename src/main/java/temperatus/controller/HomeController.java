package temperatus.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.lang.Lang;
import temperatus.util.Browser;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Home view of the application, start the scan task
 * <p>
 * Created by alberto on 17/1/16.
 */
@Controller
public class HomeController implements Initializable, AbstractController {

    @FXML private Label optimizedTitle;
    @FXML private Label optimizedText;
    @FXML private Label dataText;
    @FXML private Label dataTitle;
    @FXML private Label performanceText;
    @FXML private Label performanceTitle;

    @FXML private Hyperlink linkToWeb;

    private static Logger logger = LoggerFactory.getLogger(HomeController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        linkToWeb.setOnAction((ActionEvent e) -> {
            try {
                Browser.openWebPage(new URL(Constants.PROJECT_WEB).toURI());
            } catch (URISyntaxException | MalformedURLException e1) {
                logger.warn("Malformed URL");
                VistaNavigator.showAlert(Alert.AlertType.ERROR, language.get(Lang.ERROR_BROWSER));
            }
        });

        //Constants.prefs.putBoolean(Constants.ACTIVATED, false);
    }

    /**
     * Check if first time application start. If so, ask for user default preferences.
     */
    @Deprecated
    private void askForUserDefaultPreferences() {
        boolean isFirstTime = Constants.prefs.getBoolean(Constants.FIRST_TIME, true);
        // Start a wizard asking for default prefs to the user
        if (isFirstTime) {
            logger.info("First time using the program!");
            Constants.prefs.putBoolean(Constants.FIRST_TIME, false);
        }
    }

    @Override
    public void translate() {
        linkToWeb.setText(Constants.WEB);
        optimizedText.setText(language.get(Lang.OPTIMIZED_TEXT));
        optimizedTitle.setText(language.get(Lang.OPTIMIZED_TITLE));
        dataText.setText(language.get(Lang.DATA_TEXT));
        dataTitle.setText(language.get(Lang.DATA_TITLE));
        performanceText.setText(language.get(Lang.PERFORMANCE_TEXT));
        performanceTitle.setText(language.get(Lang.PERFORMANCE_TITLE));
    }

}
