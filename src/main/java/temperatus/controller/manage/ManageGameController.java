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
import temperatus.model.pojo.Game;
import temperatus.model.service.GameService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManageGameController implements Initializable, AbstractController {

    @FXML private TableView<Game> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    private TableColumn<Game, String> title = new TableColumn<>();
    private TableColumn<Game, String> numberOfButtons = new TableColumn<>();
    private TableColumn<Game, String> defaultPositions = new TableColumn<>();
    private TableColumn<Game, String> defaultFormulas = new TableColumn<>();

    private ObservableList<Game> games;

    @Autowired GameService gameService;

    static Logger logger = LoggerFactory.getLogger(ManageGameController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        games = FXCollections.observableArrayList();
        addAllGames();

        title.setText("Title");
        title.setCellValueFactory(cellData -> cellData.getValue().getTitleProperty());
        numberOfButtons.setText("Number of Buttons");
        numberOfButtons.setCellValueFactory(cellData -> cellData.getValue().getNumberOfButtonsProperty());
        defaultPositions.setText("Number of Default Positions");
        defaultPositions.setCellValueFactory(cellData -> cellData.getValue().getNumberOfDefaultPositionsProperty());
        defaultFormulas.setText("Number of Default Formulas");
        defaultFormulas.setCellValueFactory(cellData -> cellData.getValue().getNumberOfDefaultFormulasProperty());

        FilteredList<Game> filteredData = new FilteredList<>(games, p -> true);

        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(game -> {
                // If filter text is empty, display all subjects.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (game.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(game.getNumButtons()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Animation.fadeInTransition(infoPane);

            Game game = newValue;
            // TODO

        });

        SortedList<Game> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(title, numberOfButtons, defaultPositions, defaultFormulas);
        table.setItems(sortedData);
    }

    private void addAllGames() {
        games.addAll(gameService.getAll());
    }

    @FXML
    private void editGame() {
        NewGameController newGameController = VistaNavigator.openModal(Constants.NEW_GAME, language.get(Constants.NEWGAME));
        newGameController.setGameForUpdate(table.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void newGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, language.get(Constants.NEWGAME));
    }

    @FXML
    private void deleteGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Game game = table.getSelectionModel().getSelectedItem();
            gameService.delete(game);
            games.remove(game);
        }
    }

    @FXML
    private void gameCompleteInfo() {
        GameInfoController gameInfoController = VistaNavigator.pushViewToStack(Constants.GAME_INFO);
        gameInfoController.setGame(table.getSelectionModel().getSelectedItem());
    }

    @Override
    public void reload(Object object) {
        if(object instanceof Game) {
            if(!games.contains((Game) object)) {
                games.add((Game) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().select((Game) object);
        }
    }

    @Override
    public void translate() {

    }

}
