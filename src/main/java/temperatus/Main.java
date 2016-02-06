package temperatus;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import temperatus.util.Constants;
import temperatus.util.SpringFxmlLoader;

/**
 * Created by alberto on 17/1/16.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.initStyle(StageStyle.UNDECORATED); // remove borders

        SpringFxmlLoader loader = new SpringFxmlLoader();
        Pane pane = loader.load(getClass().getResource(Constants.SPLASH));

        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

}
