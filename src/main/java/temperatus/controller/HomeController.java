package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.device.DeviceOperationsManager;
import temperatus.util.Constants;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Home view of the application, start the scan task
 * <p>
 * Created by alberto on 17/1/16.
 */
@Controller
public class HomeController implements Initializable, AbstractController {

    @FXML private Hyperlink linkToWeb;

    @Autowired DeviceOperationsManager deviceOperationsManager;

    private static Logger logger = LoggerFactory.getLogger(HomeController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        askForUserDefaultPreferences();

        logger.info("Starting devices scan task");
        //deviceOperationsManager.init(); // start executors (and device scan task)
        //Constants.prefs.putBoolean(Constants.ACTIVATED, false);
    }

    /**
     * Check if first time application start. If so, ask for user default preferences.
     */
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

    }

}
