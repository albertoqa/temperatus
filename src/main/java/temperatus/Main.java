package temperatus;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;
import temperatus.controller.FirstStartController;
import temperatus.listener.DeviceDetectorTask;
import temperatus.util.Constants;
import temperatus.util.SpringFxmlLoader;

/**
 * TEMPERATUS
 * <p>
 * Created by alberto on 17/1/16.
 */
public class Main extends Application {

    static Logger logger = Logger.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {

        boolean isFirstTime = Constants.prefs.getBoolean(Constants.FIRST_TIME, true);
        logger.info("Is this the first time?: " + isFirstTime);

        // Start a wizard asking for default prefs to the user
        if (isFirstTime) {
            Constants.prefs.putBoolean(Constants.FIRST_TIME, false);

            FirstStartController firstStartController = new FirstStartController();
            firstStartController.startWizard();
        }

        // TODO uncomment
        //startDeviceListener();  // search for new connected devices

        // load the Splash screen
        SpringFxmlLoader loader = new SpringFxmlLoader();
        Pane pane = loader.load(getClass().getResource(Constants.SPLASH));

        Scene scene = new Scene(pane);
        primaryStage.initStyle(StageStyle.UNDECORATED); // remove borders
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Create a infinite task that search for all connected devices
     * If a new device is detected a notification is sent and all
     * classes which implement the DeviceDetectorListener are notified of
     * the event.
     * <p>
     * The task runs in a different thread and will stop when the program finish
     */
    private void startDeviceListener() {
        logger.info("Starting device detector task");

        DeviceDetectorTask task = new DeviceDetectorTask();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
