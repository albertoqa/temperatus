package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Component;
import temperatus.util.Animation;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Component
public class BaseController implements Initializable{

    @FXML private StackPane vistaHolder;

    public void setView(Node node) {
        if(vistaHolder.getChildren().size() > 0) {
            Animation.fadeOutIn(vistaHolder.getChildren().get(0), node);
        }
        vistaHolder.getChildren().setAll(node);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public StackPane getVistaHolder() {
        return vistaHolder;
    }
}
