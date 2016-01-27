package temperatus.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Component
public class BaseController implements Initializable{

    @FXML private StackPane vistaHolder;
    @FXML private Label clock;

    public void setView(Node node) {
        if(vistaHolder.getChildren().size() > 0) {
            // avoid to set the same controller twice
            // remember to set the id of all fxml
            if(vistaHolder.getChildren().get(0).getId().equals(node.getId())){
                return;
            }
            Animation.fadeOutIn(vistaHolder.getChildren().get(0), node);
        }
        vistaHolder.getChildren().setAll(node);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startClock();
    }

    private void startClock() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0),
                event -> clock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))),
                new KeyFrame(Duration.seconds(1)));

        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    public StackPane getVistaHolder() {
        return vistaHolder;
    }


    @FXML
    private void goHome() {
        VistaNavigator.loadVista(Constants.HOME);
    }
    @FXML
    private void goArchive() {
        VistaNavigator.loadVista(Constants.ARCHIVED);
    }
    @FXML
    private void goNewProject() {
        //TODO reload treeView if new Project Inserted
        VistaNavigator.openModal(Constants.NEW_PROJECT, "New Project");

    }
    @FXML
    private void goIButtons() {
        VistaNavigator.loadVista(Constants.CONNECTED);
    }
    @FXML
    private void goConfiguration() {
        VistaNavigator.openModal(Constants.CONFIG, "Configuration");
    }
}
