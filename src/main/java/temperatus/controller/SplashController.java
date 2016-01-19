package temperatus.controller;

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
import org.springframework.stereotype.Component;
import temperatus.lang.Language;
import temperatus.util.Constants;
import temperatus.util.SpringFxmlLoader;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Component
public class SplashController implements Initializable{

    @FXML
    private Label subtitle;
    @FXML
    private Label rights;
    @FXML
    private Label version;
    @FXML
    private ProgressBar progress;

    private final Language language = Language.getInstance();
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stage = new Stage();
        translate();

        Task task = createSleepTask();
        progress.progressProperty().bind(task.progressProperty());
        new Thread(task).start();

        loadHome();
    }

    private Task createSleepTask() {
        Task task = new Task<Void>() {
            @Override public Void call() {
                final int max = 50;
                for (int i = 1; i <= max; i++) {
                    /*try { // TODO uncoment
                        Thread.sleep(100);
                    } catch (InterruptedException interrupted) {
                        if (isCancelled()) {
                            updateMessage("Cancelled");
                            break;
                        }
                    }*/
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

    private void translate() {
        rights.setText(language.get(Constants.RIGHTS));
        // TODO
    }

    private void showHome() {
        Stage currentStage = (Stage) rights.getScene().getWindow();
        currentStage.close();

        stage.show();
    }

    private void loadHome() {
        SpringFxmlLoader loader = new SpringFxmlLoader();
        Pane pane = loader.load(getClass().getResource(Constants.BASE));
        stage.setScene(new Scene(pane));
        VistaNavigator.setBaseController(loader.getController());
        VistaNavigator.loadVista(Constants.HOME);
    }


}
