package temperatus.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Controller
public class SplashController implements Initializable, AbstractController {

    @FXML private Label subtitle;
    @FXML private Label rights;
    @FXML private Label version;
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
     * @return
     */
    private Task createSleepTask() {
        Task task = new Task<Void>() {
            @Override
            public Void call() {

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loadHome();
                    }
                });

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

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                showHome();
            }
        });

        return task;
    }

    @Override
    public void translate() {
        rights.setText(language.get(Constants.RIGHTS));
        // TODO
    }

    /**
     * Close actual stage and open a new one with Home screen loaded
     */
    private void showHome() {
        Stage currentStage = (Stage) rights.getScene().getWindow();
        currentStage.close();
        stage.show();
    }


    private void loadHome() {
        Pane pane = VistaNavigator.loader.load(getClass().getResource(Constants.BASE));
        stage.setScene(new Scene(pane));
        stage.setMinHeight(VistaNavigator.MIN_HEIGHT);
        stage.setMinWidth(VistaNavigator.MIN_WIDTH);
        VistaNavigator.setBaseController(VistaNavigator.loader.getController());
        VistaNavigator.loadVista(Constants.HOME);
    }

}
