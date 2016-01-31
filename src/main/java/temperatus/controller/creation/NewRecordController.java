package temperatus.controller.creation;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.model.pojo.Game;
import temperatus.model.pojo.GamePosition;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Position;
import temperatus.model.service.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 31/1/16.
 */
@Component
public class NewRecordController implements Initializable{

    @FXML private VBox idBox;
    @FXML private VBox positionBox;
    @FXML private VBox sourceBox;

    // TableView
    // Columna 1 -> id
    // Columna 2 -> Posición
    // Columna 3 -> fuente de datos

    // Si el juego seleccionado tiene posiciones por defecto, asignarlas directamente a la columna 2
    // Si no, dejarlas en blanco

    // Cada vez que se detecte un botón, comprobar si esta guardado en la base de datos
        // Si esta, buscar su posición por defecto
            // Comprobar si su posición por defecto coincide con algunas de las posiciones por defecto del juego y asignarlos



    @Autowired
    GameService gameService;
    @Autowired
    GamePositionService gamePositionService;
    @Autowired
    IbuttonService ibuttonService;
    @Autowired
    PositionService positionService;
    @Autowired
    RecordService recordService;

    private Mission mission;
    private Game game;
    private List<Position> defaultPositions;
    private List<Position> positions;

    private Double prefHeight = 30.0;

    public void loadData(Mission mission) {
        this.mission = mission;

        game = gameService.getById(mission.getGameId());

        List<GamePosition> gamePositions = gamePositionService.getAllForGame(game.getId());

        defaultPositions = new ArrayList<>();
        gamePositions.stream().forEach(gamePosition -> {
            Position position = positionService.getById(gamePosition.getPositionId());
            defaultPositions.add(position);
        });

        positions = new ArrayList<>();
        positions = positionService.getAll();


        // En la tabla necesito:
            // ID -> autoincrement
            // Selector de Posiciones -> positions
            // Selector de Sources -> ibuttons detectados + select file from computer

       for(int i = 0; i < game.getNumButtons(); i++){

           int index = i;

           ChoiceBox<Position> choiceBoxPositions = new ChoiceBox<>();
           choiceBoxPositions.setItems(FXCollections.observableArrayList(positions));

           if(defaultPositions.size() > i) {
               choiceBoxPositions.getSelectionModel().select(defaultPositions.get(i));
           }

           //TODO choicebox of sources

           addNewRow(index, choiceBoxPositions);

       }


    }

    private void addNewRow(int index, ChoiceBox<Position> posBox) {
        Label id = new Label(String.valueOf(index));
        id.setPrefHeight(prefHeight);
        id.setMaxHeight(prefHeight);
        id.setMinHeight(prefHeight);

        idBox.getChildren().add(id);

        posBox.setMinHeight(prefHeight);
        posBox.setMaxHeight(prefHeight);
        posBox.setPrefHeight(prefHeight);

        positionBox.getChildren().add(posBox);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
