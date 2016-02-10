package temperatus.controller.creation;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.importer.IbuttonDataImporter;
import temperatus.model.pojo.*;
import temperatus.model.pojo.types.SourceChoice;
import temperatus.model.service.*;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 31/1/16.
 */
@Controller
public class NewRecordController extends AbstractCreationController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label indexLabel;
    @FXML private Label positionLabel;
    @FXML private Label dataSourceLabel;

    @FXML private Button refreshButton;

    @FXML private TableView iButtonsTable;
    @FXML private ScrollPane scrollPane;

    @FXML private VBox idBox;
    @FXML private VBox positionBox;
    @FXML private VBox addPositionBox;
    @FXML private VBox sourceBox;
    @FXML private VBox addSourceBox;

    @Autowired GameService gameService;
    @Autowired IbuttonService ibuttonService;
    @Autowired PositionService positionService;
    @Autowired RecordService recordService;
    @Autowired MeasurementService measurementService;

    private Mission mission;
    private Game game;                          // Game assigned to the mission
    private List<Position> defaultPositions;    // Default positions for selected game
    private List<Position> positions;           // All positions saved to the db
    private List<Ibutton> iButtons;             // Detected iButtons

    private final Double prefHeight = 30.0;     // Preferred height for "rows"

    static Logger logger = Logger.getLogger(NewProjectController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();
    }

    /**
     * User can change the position of a button to a previously saved position
     * Even if the default is pre-selected, we have to allow to choose all positions
     */
    private void loadAllPositions() {
        positions = new ArrayList<>();
        positions = positionService.getAll();
    }

    public void loadData(Mission mission) {

        this.mission = mission;

        // Get game assigned to this mission
        game = gameService.getById(mission.getGame().getId());

        // Get default game-position
        //List<GamePosition> gamePositions = gamePositionService.getAllForGame(game.getId());



        loadAllPositions(); // Pre-load all positions from db

        iButtons = detectButtons();

        // The table will have the same number of rows as iButtons
        for (int index = 0; index < game.getNumButtons(); index++) {

            // Each row will have { ID | POSITION | + | SOURCE | + }

            // ID = index
            // POSITION -> add all positions + if default, select it
            ChoiceBox<Position> choiceBoxPositions = addAllPositions();

            if (defaultPositions.size() > index) {
                choiceBoxPositions.getSelectionModel().select(defaultPositions.get(index));
            }

            // SOURCE -> all detected iButtons
            ChoiceBox<SourceChoice> choiceBoxSource = addAllDetectediButtons();

            // Add a new "row" with generated info
            addNewRow(index, choiceBoxPositions, choiceBoxSource);
        }

        // At his point all choiceBoxes are full with all positions and all detected buttons
        // Also, if the game has default positions, those positions are pre-selected

        // For each detected button, compare if its default position is equal to any of the default positions of the game
        for (Ibutton ibutton : iButtons) {

            Integer defaultPositionForIbuttonId = ibutton.getPosition().getId();

            if (defaultPositionForIbuttonId != null && defaultPositionForIbuttonId > 0) {
                Position defaultPositionForIbutton = positionService.getById(defaultPositionForIbuttonId);

                // If game default positions contains the same position as ibutton default position
                // Set that ibutton to that position
                if (defaultPositions.contains(defaultPositionForIbutton)) {
                    int index = getRowForPosition(defaultPositionForIbutton);
                    ((ChoiceBox<SourceChoice>) sourceBox.getChildren().get(index)).getSelectionModel().select(new SourceChoice(ibutton));
                }
            }
        }
    }

    /**
     * Get row index for a given selected position
     *
     * @param position
     * @return
     */
    private int getRowForPosition(Position position) {
        for (int index = 0; index < positionBox.getChildren().size(); index++) {
            if (((ChoiceBox<Position>) positionBox.getChildren().get(index)).getSelectionModel().getSelectedItem().equals(position)) {
                return index;
            }
        }
        return -1;
    }

    // TODO
    private List<Ibutton> detectButtons() {
        List<Ibutton> ibuttonList = new ArrayList<>();



        return ibuttonList;
    }

    /**
     * Add all positions to the choicebox
     *
     * @return
     */
    private ChoiceBox<Position> addAllPositions() {
        ChoiceBox<Position> choiceBoxPositions = new ChoiceBox<>();
        choiceBoxPositions.setItems(FXCollections.observableArrayList(positions));
        return choiceBoxPositions;
    }

    /**
     * Create a SourceChoice for each iButton detected and add it to the ChoiceBox
     *
     * @return
     */
    private ChoiceBox<SourceChoice> addAllDetectediButtons() {
        ChoiceBox<SourceChoice> choiceBoxSource = new ChoiceBox<>();

        List<SourceChoice> sourceChoiceList = new ArrayList<>();
        for (Ibutton ibutton : iButtons) {
            SourceChoice sourceChoice = new SourceChoice(ibutton);
            sourceChoiceList.add(sourceChoice);
        }

        choiceBoxSource.setItems(FXCollections.observableArrayList(sourceChoiceList));

        return choiceBoxSource;
    }

    @FXML
    void save() {   // aqui tengo que guardar el iButton y la Mission

        HashMap<Ibutton, List<Measurement>> buttonMeasurementsHashMap = new HashMap<>();

        for(int index = 0; index < game.getNumButtons(); index++){
            // TODO check if iButton or Record are already in DB

            Position position = null;
            SourceChoice sourceChoice = null;
            Ibutton ibutton = null;
            List<Measurement> measurements = null;
            Record record = null;
            String iButtonSerial = "";
            String iButtonModel = "";

            try {
                position = ((ChoiceBox<Position>) positionBox.getChildren().get(index)).getSelectionModel().getSelectedItem();
                sourceChoice = ((ChoiceBox<SourceChoice>) sourceBox.getChildren().get(index)).getSelectionModel().getSelectedItem();

                // Import data
                if(sourceChoice.getFile() != null) {
                    IbuttonDataImporter ibuttonDataImporter = new IbuttonDataImporter(sourceChoice.getFile());
                    iButtonModel = ibuttonDataImporter.getDeviceModel();
                    iButtonSerial = ibuttonDataImporter.getDeviceSerial();
                    measurements = ibuttonDataImporter.getMeasurements();
                } else if (sourceChoice.getIbutton() != null) {



                }

                // Check if iButton is already in the db

                ibutton = ibuttonService.getBySerial(iButtonSerial);

                if(ibutton == null) {
                    //ibutton = new Ibutton(iButtonSerial, iButtonModel, position.getId());
                    ibuttonService.save(ibutton);
                } else {
                    //ibutton.setDefaultPositionId(position.getId());
                    //ibuttonService.saveOrUpdate(ibutton); //TODO
                }

                int positionId = position.getId();

                //record = new Record(mission.getId(), ibutton.getId(), positionId);
                recordService.save(record);
                int recordId = record.getId();

                for (Measurement measurement : measurements) {
                    measurement.setRecordId(recordId);
                }

                if(measurements != null && ibutton != null) {
                    buttonMeasurementsHashMap.put(ibutton, measurements);
                }


            } catch (Exception e) {

            }


        }

        // TODO check if all iButtons and Records are saved to db -- also check for duplicates
        RecordConfigController recordConfigController = VistaNavigator.pushViewToStack(Constants.RECORD_CONFIG);
        recordConfigController.setDataMap(buttonMeasurementsHashMap);
        //recordConfigController.setMissionId(mission.getId());


        // Primero, comprobar si se ha seleccionado todo lo necesario.

        // Si hay alguna fila completa sin seleccionar mostrar un aviso y seguir

        // Si en alguna fila hay una posicion seleccionada pero no un source mostrar aviso y seguir

        // Si en alguna fila hay un source seleccionado pero no una posición ERROR y stop



        // Si hay algún botón que no tenga posición por defecto, preguntar si se quiere asignar la actual


        // Si hay algún botón que haya cambiado de posición por defecto, preguntar si se quiere asignar la actual




        // Validar los datos de todos los botones -> comprobar que no haya ningún valor raro
        // Si hay algún valor raro, avisar al usuario y permitirle continuar o descartar ese valor
        // No mostrar valor por valor, mostrar una lista que se pueda seleccionar varios


        // Seleccionar el tiempo que quiere usarse para el experimento


        // Guardar todos los datos



        // Mostrar información de la misión





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

        Button importSource = new Button();
        importSource.setText("+");
        importSource.setMinHeight(prefHeight);
        importSource.setMaxHeight(prefHeight);
        importSource.setPrefHeight(prefHeight);
        importSource.setMinWidth(prefHeight);
        importSource.setMaxWidth(prefHeight);
        importSource.setPrefWidth(prefHeight);
        importSource.setUserData(index);
        importSource.addEventHandler(MouseEvent.MOUSE_CLICKED, addImportDataButtonHandler());

        addSourceBox.getChildren().addAll(importSource);
    }

    /**
     * Handle action when an import button is pressed
     *
     * @return
     */
    private EventHandler<Event> addImportDataButtonHandler() {
        final EventHandler<Event> myHandler = event -> {
            File file = importDataFromSource();
            SourceChoice sourceChoice = new SourceChoice(file);

            Button clickedButton = (Button) event.getSource();
            Integer index = (Integer) clickedButton.getUserData();

            if(sourceBox.getChildren().get(index) instanceof ChoiceBox) {
                ((ChoiceBox) sourceBox.getChildren().get(index)).getItems().add(sourceChoice);
                ((ChoiceBox) sourceBox.getChildren().get(index)).getSelectionModel().select(sourceChoice);
            }

            event.consume();
        };

        return myHandler;
    }

    /**
     * Open the File Chooser (only allow csv files) and return the selected file
     *
     * @return selected file
     */
    protected File importDataFromSource() {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterCSV = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.CSV");
        fileChooser.getExtensionFilters().add(extFilterCSV);

        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);
        return file;
    }

    @Override
    public void reload(Object object) {

    }

    @Override
    public void translate() {

    }

}
