package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.ListSelectionView;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Game;
import temperatus.model.pojo.Position;
import temperatus.model.service.FormulaService;
import temperatus.model.service.GameService;
import temperatus.model.service.PositionService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 26/1/16.
 */
@Controller
@Scope("prototype")
public class NewGameController extends AbstractCreationController implements Initializable {

    // TODO add default positions to the Game

    @FXML Label nameLabel;
    @FXML Label observationsLabel;
    @FXML Label numButtonsLabel;

    @FXML TextField nameInput;
    @FXML TextArea observationsInput;
    @FXML TextField numButtonsInput;

    @FXML StackPane positionsListPane;
    @FXML StackPane formulasListPane;

    private ListSelectionView<Position> positionsSelector;
    private CheckListView<Formula> formulasSelector;

    @Autowired GameService gameService;
    @Autowired PositionService positionService;
    @Autowired FormulaService formulaService;

    static Logger logger = LoggerFactory.getLogger(NewGameController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        positionsSelector = new ListSelectionView<>();
        positionsSelector.getSourceItems().addAll(positionService.getAll());
        positionsListPane.getChildren().setAll(positionsSelector);

        formulasSelector = new CheckListView<>();
        formulasSelector.getItems().addAll(formulaService.getAll());
        formulasListPane.getChildren().setAll(formulasSelector);

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

            List<Position> defaultPositions = positionsSelector.getTargetItems();
            game.getPositions().addAll(defaultPositions);

            List<Formula> defaultFormulas = formulasSelector.getCheckModel().getCheckedItems();
            game.getFormulas().addAll(defaultFormulas);

            gameService.save(game);

            Animation.fadeInOutClose(titledPane);
            if (VistaNavigator.getController() != null) {
                VistaNavigator.getController().reload(game);
            }

            logger.info("Saved" + game);

        } catch (ControlledTemperatusException ex) {
            logger.warn("Exception: " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (NumberFormatException ex) {
            logger.warn("Invalid input for number of buttons");
            showAlert(Alert.AlertType.ERROR, "Invalid number of buttons.");
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, "Duplicate Game.");
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Unknown error.");
        }
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.NEWGAME));
        saveButton.setText(language.get(Constants.SAVE));
        cancelButton.setText(language.get(Constants.CANCEL));
        nameLabel.setText(language.get(Constants.NAMELABEL));
        observationsLabel.setText(language.get(Constants.OBSERVATIONSLABEL));
        nameInput.setPromptText(language.get(Constants.NAMEPROMPT));
        observationsInput.setPromptText(language.get(Constants.OBSERVATIONSPROMPT));
        numButtonsLabel.setText(language.get(Constants.NUMBUTTONSLABEL));
        numButtonsInput.setPromptText(language.get(Constants.NUMBUTTONSPROMPT));
    }

}
