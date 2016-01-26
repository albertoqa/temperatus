package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import org.springframework.stereotype.Component;
import temperatus.util.Animation;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 26/1/16.
 */
@Component
public class NewGameController implements Initializable {

    @FXML
    TitledPane titledPane;

    @FXML
    private void saveGame(){}

    @FXML
    private void cancel() {
        Animation.fadeInOutClose(titledPane);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
