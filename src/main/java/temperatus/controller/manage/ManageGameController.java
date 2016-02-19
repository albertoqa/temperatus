package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Game;
import temperatus.model.service.GameService;
import temperatus.util.Animation;
import temperatus.util.VistaNavigator;

import java.net.URL;
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

    @Override
    public void reload(Object object) {

    }

    @Override
    public void translate() {

    }

}
