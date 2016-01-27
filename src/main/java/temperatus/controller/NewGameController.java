package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.model.pojo.Game;
import temperatus.model.service.GameService;
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
    TextField nameInput;
    @FXML
    TextArea observationsInput;
    @FXML
    TextField numButtonsInput;

    @Autowired
    GameService gameService;

    @FXML
    private void saveGame(){
        // TODO hacer todas las comprobaciones necesarias
        Game game = new Game();
        game.setTitle(nameInput.getText());
        game.setObservations(observationsInput.getText());
        game.setNumButtons(Integer.parseInt(numButtonsInput.getText()));

        gameService.save(game);
    }

    @FXML
    private void cancel() {
        Animation.fadeInOutClose(titledPane);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
