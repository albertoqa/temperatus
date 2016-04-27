package temperatus.controller;

import javafx.fxml.Initializable;
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

    @Autowired DeviceOperationsManager deviceOperationsManager;

    private static Logger logger = LoggerFactory.getLogger(HomeController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        logger.info("Starting devices scan task");
        deviceOperationsManager.init(); // start executors (and device scan task)
    }

    /**
     *
     */
    private void askForUserDefaultPreferences() {
        boolean isFirstTime = Constants.prefs.getBoolean(Constants.FIRST_TIME, true);
        logger.info("Is this the first time?: " + isFirstTime);

        // Start a wizard asking for default prefs to the user
        /*if (isFirstTime) {
            Constants.prefs.putBoolean(Constants.FIRST_TIME, false);

            FirstStartController firstStartController = new FirstStartController();
            firstStartController.startWizard();
        }*/
    }

    @Override
    public void translate() {

    }

}
