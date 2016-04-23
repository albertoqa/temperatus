package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewGameController;
import temperatus.controller.manage.ampliate.GameInfoController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Game;
import temperatus.model.pojo.Position;
import temperatus.model.service.GameService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Allow the user to search, edit, delete and create games
 * <p>
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManageGameController implements Initializable, AbstractController {

    @FXML private TableView<Game> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    @FXML private Label nameLabel;
    @FXML private Label numberOfButtonsLabel;
    @FXML private Label numButtons;
    @FXML private Label defaultFormulasLabel;
    @FXML private Label defaultPositionsLabel;
    @FXML private Label positionsInfo;
    @FXML private Label formulasInfo;

    @FXML private Button completeInfoButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private TableColumn<Game, String> title = new TableColumn<>();
    private TableColumn<Game, String> numberOfButtons = new TableColumn<>();
    private TableColumn<Game, String> defaultPositions = new TableColumn<>();
    private TableColumn<Game, String> defaultFormulas = new TableColumn<>();

    private ObservableList<Game> games;

    @Autowired GameService gameService;

    private static Logger logger = LoggerFactory.getLogger(ManageGameController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        games = FXCollections.observableArrayList(gameService.getAll());
        title.setCellValueFactory(cellData -> cellData.getValue().getTitleProperty());
        numberOfButtons.setCellValueFactory(cellData -> cellData.getValue().getNumberOfButtonsProperty());
        defaultPositions.setCellValueFactory(cellData -> cellData.getValue().getNumberOfDefaultPositionsProperty());
        defaultFormulas.setCellValueFactory(cellData -> cellData.getValue().getNumberOfDefaultFormulasProperty());

        FilteredList<Game> filteredData = new FilteredList<>(games, p -> true);
        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(game -> newValue == null || newValue.isEmpty() || game.getTitle().toLowerCase().contains(newValue.toLowerCase()) || String.valueOf(game.getNumButtons()).contains(newValue.toLowerCase()));
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, game) -> {
            Animation.fadeInTransition(infoPane);
            if (game != null) {
                nameLabel.setText(game.getTitle().toUpperCase());
                formulasInfo.setText("");
                positionsInfo.setText("");
                for (Formula formula : game.getFormulas()) {
                    formulasInfo.setText(formulasInfo.getText() + formula.getName() + ", ");
                }
                for (Position position : game.getPositions()) {
                    positionsInfo.setText(positionsInfo.getText() + position.getPlace() + ", ");
                }
            }
        });

        SortedList<Game> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(title, numberOfButtons, defaultPositions, defaultFormulas);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    /**
     * Edit selected game
     */
    @FXML
    private void editGame() {
        NewGameController newGameController = VistaNavigator.openModal(Constants.NEW_GAME, language.get(Lang.NEWGAME));
        newGameController.setGameForUpdate(table.getSelectionModel().getSelectedItem());
    }

    /**
     * Show modal window to create a new game
     */
    @FXML
    private void newGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, language.get(Lang.NEWGAME));
    }

    /**
     * Delete selected game from database and table
     */
    @FXML
    private void deleteGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Game game = table.getSelectionModel().getSelectedItem();
            gameService.delete(game);
            games.remove(game);
            logger.info("Deleted game... " + game);
        }
    }

    /**
     * Show game complete info. Push a new view to the stack with all the info.
     */
    @FXML
    private void gameCompleteInfo() {
        GameInfoController gameInfoController = VistaNavigator.pushViewToStack(Constants.GAME_INFO);
        gameInfoController.setGame(table.getSelectionModel().getSelectedItem());
    }

    /**
     * If a game is updated or created reload the table/selection
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Game) {
            if (!games.contains(object)) {
                games.add((Game) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select((Game) object);
        }
    }

    @Override
    public void translate() {
        title.setText(language.get(Lang.TITLE));
        numberOfButtons.setText(language.get(Lang.NUM_BUTTONS_COLUMN));
        defaultPositions.setText(language.get(Lang.DEFAULT_POS_COLUMN));
        defaultFormulas.setText(language.get(Lang.DEFAULT_FORMULAS_COLUMN));
        editButton.setText(language.get(Lang.EDIT));
        deleteButton.setText(language.get(Lang.DELETE));
        newElementButton.setText(language.get(Lang.NEWGAMEBUTTON));
        numberOfButtonsLabel.setText(language.get(Lang.NUMBUTTONSLABEL));
        defaultFormulasLabel.setText(language.get(Lang.DEFAULTFORMULASLABEL));
        defaultPositionsLabel.setText(language.get(Lang.DEFAULTPOSLABEL));
        completeInfoButton.setText(language.get(Lang.COMPLETE_INFO));
    }

}
