package temperatus.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
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
    @FXML private ListView<String> menu;

    public void setView(Node node) {
        if(vistaHolder.getChildren().size() > 0) {
            // avoid to set the same controller twice
            // remember to set the id of all fxml set in the baseView
            if(vistaHolder.getChildren().get(0).getId().equals(node.getId())){
                return;
            }
            Animation.fadeOutIn(vistaHolder.getChildren().get(0), node);
        }
        vistaHolder.getChildren().setAll(node);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menu.getItems().addAll("  Home", "  Archive","  Devices","  Configuration");

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
    private void menuActions(MouseEvent event) {
        switch(menu.getSelectionModel().getSelectedIndex()){
            case 0: {
                VistaNavigator.loadVista(Constants.HOME);
            } break;
            case 1: {
                VistaNavigator.loadVista(Constants.ARCHIVED);
            } break;
            case 2: {
                VistaNavigator.loadVista(Constants.CONNECTED);
            } break;
            case 3: {
                VistaNavigator.openModal(Constants.CONFIG, "Configuration");
            } break;
        }
    }

}
