package temperatus.controller.configuration;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.model.pojo.Game;
import temperatus.model.service.GameService;
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
    TableView gamesTable;

    @Autowired
    GameService gameService;

    @FXML
    private void newGame() {
        VistaNavigator.openModal(Constants.NEW_GAME, "New Game");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Game> data = FXCollections.observableArrayList(gameService.getAll());
        TableColumn name = new TableColumn("Name");
        TableColumn observations = new TableColumn("Observations");

        name.setCellValueFactory(
                new PropertyValueFactory<Game,String>("title")
        );
        observations.setCellValueFactory(
                new PropertyValueFactory<Game,String>("observations")
        );

        gamesTable.setItems(data);
        gamesTable.getColumns().addAll(name, observations);

    }
}
