package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Game;
import temperatus.model.service.GameService;
import temperatus.util.Animation;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 26/1/16.
 */
@Component
public class NewGameController extends AbstractCreation implements Initializable {

    @FXML Label nameLabel;
    @FXML Label observationsLabel;
    @FXML Label numButtonsLabel;

    @FXML TextField nameInput;
    @FXML TextArea observationsInput;
    @FXML TextField numButtonsInput;

    @Autowired GameService gameService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
    }

    @Override @FXML
    protected void save() throws ControlledTemperatusException{

        String name = nameInput.getText();
        String observations = observationsInput.getText();
        Integer numButtons = 0;

        try {
            numButtons = Integer.parseInt(numButtonsInput.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid number of buttons");
        }

        // TODO show warnings!
        if(name.length() < 3 || name.length() > 20) {
            showAlert(Alert.AlertType.ERROR, "Invalid name lenght");
        } else if(observations.length() > 300) {
            showAlert(Alert.AlertType.ERROR, "Invalid observations lenght");
        } else if(numButtons < 0 || numButtons > 30) {
            showAlert(Alert.AlertType.ERROR, "Invalid number of buttons");
        }

        Game game = new Game();

        game.setTitle(name);
        game.setObservations(observations);
        game.setNumButtons(numButtons);

        gameService.save(game); // TODO throw exception if game with same name already exists

        Animation.fadeInOutClose(titledPane);
        if(VistaNavigator.getController() != null) {
            VistaNavigator.getController().reload(game);
        }

        // TODO show sucess
    }

    @Override
    void translate() {
        nameLabel.setText("Name");
        observationsLabel.setText("Observations");
        numButtonsLabel.setText("Number of Buttons");
        saveButton.setText("Save");
        cancelButton.setText("Cancel");
    }



}
