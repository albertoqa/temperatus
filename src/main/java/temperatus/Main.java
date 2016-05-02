package temperatus;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

/**
 * TEMPERATUS Main - Check if application is registered
 * <p>
 * Created by alberto on 17/1/16.
 */
public class Main extends Application {

    private static Logger logger = LoggerFactory.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane pane;

        boolean isActivated = Constants.prefs.getBoolean(Constants.ACTIVATED, false);
        logger.info("Is the software already activated?  " + isActivated);
        if (!isActivated) {
            // load the activation window
            pane = FXMLLoader.load(getClass().getResource(Constants.ACTIVATION));
            VistaNavigator.setCurrentStage(primaryStage);
        } else {
            // load the Splash screen
            pane = VistaNavigator.loader.load(getClass().getResource(Constants.SPLASH));
            primaryStage.initStyle(StageStyle.UNDECORATED); // remove borders
        }

        Scene scene = new Scene(pane);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
