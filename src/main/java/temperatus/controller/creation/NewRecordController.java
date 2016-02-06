package temperatus.controller.creation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.controller.archived.MissionInfoController;
import temperatus.importer.IbuttonDataImporter;
import temperatus.model.SourceChoice;
import temperatus.model.pojo.*;
import temperatus.model.service.*;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 31/1/16.
 */
@Component
public class NewRecordController implements Initializable {

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
    @Autowired
    MeasurementService measurementService;

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

        for (int i = 0; i < game.getNumButtons(); i++) {

            int index = i;

            ChoiceBox<Position> choiceBoxPositions = new ChoiceBox<>();
            choiceBoxPositions.setItems(FXCollections.observableArrayList(positions));

            if (defaultPositions.size() > i) {
                choiceBoxPositions.getSelectionModel().select(defaultPositions.get(i));
            }

            //TODO create one selection for FileChooser
            //TODO create one selection for each connected iButton

            ChoiceBox<SourceChoice> choiceBoxSource = new ChoiceBox<>();

            SourceChoice ibutton = new SourceChoice();
            ibutton.setIbutton(new Ibutton("WSMA00239122", "DSL9902", 0));
            //sourceChoice.setObject(new FileChooser());
            SourceChoice file = new SourceChoice();

            choiceBoxSource.setItems(FXCollections.observableArrayList(ibutton, file));

            choiceBoxSource.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SourceChoice>() {
                @Override public void changed(ObservableValue<? extends SourceChoice> observableValue, SourceChoice oldChoice, SourceChoice newChoice) {
                    if (newChoice.getIbutton() != null) {

                    } else if (newChoice.getFile() == null){
                        FileChooser fileChooser = new FileChooser();
                        SourceChoice sourceChoice = new SourceChoice();
                        sourceChoice.setFile(fileChooser.showOpenDialog(null));

                        choiceBoxSource.getItems().add(sourceChoice);

                    }
                }
            });

            addNewRow(index, choiceBoxPositions, choiceBoxSource);

        }

    }

    @FXML
    private void save(){

        for(int i = 0; i < game.getNumButtons(); i++) {

            Position position = ((ChoiceBox<Position>) positionBox.getChildren().get(i)).getSelectionModel().getSelectedItem();
            SourceChoice sourceChoice = ((ChoiceBox<SourceChoice>) sourceBox.getChildren().get(i)).getSelectionModel().getSelectedItem();

            if(sourceChoice == null || position == null) {
                // TODO
                continue;
            }

            Ibutton ibutton = new Ibutton();
            List<Measurement> measurements = null;

            if(sourceChoice.getFile() != null) {
                IbuttonDataImporter ibuttonDataImporter = new IbuttonDataImporter(sourceChoice.getFile());

                ibutton.setModel(ibuttonDataImporter.getDeviceModel());
                ibutton.setSerial(ibuttonDataImporter.getDeviceSerial());

                measurements = ibuttonDataImporter.getMeasurements();
            }

            ibuttonService.save(ibutton);
            int positionId = position.getId();

            Record record = new Record(mission.getId(), ibutton.getId(), positionId);
            recordService.save(record);
            int recordId = record.getId();

            for(Measurement measurement: measurements) {
                measurement.setRecordId(recordId);
                measurementService.save(measurement);
            }

        }

        // Load mission info //TODO
        MissionInfoController missionInfoController = VistaNavigator.pushViewToStack(Constants.MISSION_INFO);
        missionInfoController.setData(mission.getId());
    }

    private void addNewRow(int index, ChoiceBox<Position> posBox, ChoiceBox<SourceChoice> srcBox) {
        Label id = new Label(String.valueOf(index));
        id.setPrefHeight(prefHeight);
        id.setMaxHeight(prefHeight);
        id.setMinHeight(prefHeight);

        idBox.getChildren().add(id);

        posBox.setMinHeight(prefHeight);
        posBox.setMaxHeight(prefHeight);
        posBox.setPrefHeight(prefHeight);

        positionBox.getChildren().add(posBox);

        srcBox.setMinHeight(prefHeight);
        srcBox.setMaxHeight(prefHeight);
        srcBox.setPrefHeight(prefHeight);

        sourceBox.getChildren().add(srcBox);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
