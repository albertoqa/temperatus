package temperatus.controller.configuration;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.stereotype.Component;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 26/1/16.
 */
@Component
public class GamesGeneralController implements Initializable {

    @FXML
    private void newGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, "New Game");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
