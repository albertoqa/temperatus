package temperatus;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.controller.FirstStartController;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

/**
 * TEMPERATUS Main - Configure if first start
 * <p>
 * Created by alberto on 17/1/16.
 */
public class Main extends Application {

    private static Logger logger = LoggerFactory.getLogger(Main.class.getName());

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

        // load the Splash screen
        Pane pane = VistaNavigator.loader.load(getClass().getResource(Constants.SPLASH));

        Scene scene = new Scene(pane);
        primaryStage.initStyle(StageStyle.UNDECORATED); // remove borders
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
