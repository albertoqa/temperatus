package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Language;
import temperatus.model.pojo.Game;
import temperatus.model.service.GameService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 26/1/16.
 */
@Component
public class NewGameController extends AbstractCreationController implements Initializable {

    @FXML Label nameLabel;
    @FXML Label observationsLabel;
    @FXML Label numButtonsLabel;

    @FXML TextField nameInput;
    @FXML TextArea observationsInput;
    @FXML TextField numButtonsInput;

    @Autowired GameService gameService;

    private final Language language = Language.getInstance();
    static Logger logger = Logger.getLogger(NewGameController.class.getName());


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
    }

    @Override
    @FXML
    protected void save() {

        String name;
        String observations;
        Integer numButtons;

        try {
            logger.info("Saving game...");

            name = nameInput.getText();
            observations = observationsInput.getText();
            numButtons = Integer.parseInt(numButtonsInput.getText());

            Game game = new Game(name, numButtons, observations);
            gameService.save(game);

            Animation.fadeInOutClose(titledPane);
            if (VistaNavigator.getController() != null) {
                VistaNavigator.getController().reload(game);
            }

            logger.info("Saved" + game);

        } catch (ControlledTemperatusException ex) {
            logger.warn("Exception: " + ex.getMessage());
            // TODO show alert
        } catch (NumberFormatException ex) {
            logger.warn("Invalid input for number of buttons");
            //showAlert(Alert.AlertType.ERROR, "Invalid number of buttons");
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            // TODO show alert
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            // TODO show alert
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWGAME));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        observationsLabel.setText(language.get(Constants.OBSERVATIONSLABEL));
        nameInput.setPromptText(language.get(Constants.NAMEPROMP));
        observationsInput.setPromptText(language.get(Constants.OBSERVATIONSPROMP));
        numButtonsLabel.setText(language.get(Constants.NUMBUTTONSLABEL));
        numButtonsInput.setPromptText(language.get(Constants.NUMBUTTONSPROMP));
    }

}
