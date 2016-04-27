package temperatus.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.lang.Lang;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Imitate a long running task to show a welcome screen to the program
 * Splash is set to prototype because once it finishes its function, I want it to be removed by the garbage collector
 * <p>
 * Created by alberto on 17/1/16.
 */
@Controller
@Scope("prototype")
public class SplashController implements Initializable, AbstractController {

    @FXML private Label rights;
    @FXML private Label version;
    @FXML private Label webPage;
    @FXML private ProgressBar progress;

    private Stage stage = new Stage();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        Task task = createSleepTask();
        progress.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Imitate a long and expensive task + load home screen
     *
     * @return sleep task
     */
    private Task createSleepTask() {
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                Platform.runLater(() -> loadHome());

                final int max = 50;
                for (int i = 1; i <= max; i++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException interrupted) {
                        if (isCancelled()) {
                            updateMessage("Cancelled");
                            break;
                        }
                    }
                    updateProgress(i, max);
                }
                return null;
            }
        };

        // When the task finishes show the home window
        task.setOnSucceeded(t -> showHome());
        return task;
    }

    /**
     * Close actual stage and open a new one with Home screen loaded
     */
    private void showHome() {
        VistaNavigator.loadVista(Constants.HOME);

        Stage currentStage = (Stage) rights.getScene().getWindow();
        currentStage.close();
        stage.show();
    }

    /**
     * Load and save the BaseController, create a new Stage which will be the main stage of the application
     */
    private void loadHome() {
        Pane pane = VistaNavigator.loader.load(getClass().getResource(Constants.BASE));
        stage.setScene(new Scene(pane));
        stage.setMinHeight(VistaNavigator.MIN_HEIGHT);
        stage.setMinWidth(VistaNavigator.MIN_WIDTH);
        VistaNavigator.setBaseController(VistaNavigator.loader.getController());    // loader is set to BASE
    }

    @Override
    public void translate() {
        rights.setText(language.get(Lang.RIGHTS));
        version.setText(Constants.VERSION);
        webPage.setText(Constants.WEB);
    }
}
