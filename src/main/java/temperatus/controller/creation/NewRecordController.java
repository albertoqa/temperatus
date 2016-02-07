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
import temperatus.controller.archived.MissionInfoController;
import temperatus.importer.IbuttonDataImporter;
import temperatus.model.SourceChoice;
import temperatus.model.pojo.*;
import temperatus.model.service.*;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
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
    @Autowired GamePositionService gamePositionService;
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
        game = gameService.getById(mission.getGameId());

        // Get default game-position
        List<GamePosition> gamePositions = gamePositionService.getAllForGame(game.getId());

        // Get default positions (if any)
        defaultPositions = new ArrayList<>();
        gamePositions.stream().forEach(gamePosition -> {
            Position position = positionService.getById(gamePosition.getPositionId());
            defaultPositions.add(position);
        });

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

            Integer defaultPositionForIbuttonId = ibutton.getDefaultPositionId();

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

        Ibutton ibutton3 = new Ibutton("A00239122", "DSL9902", 0);
        Ibutton ibutton4 = new Ibutton("B00239122", "DSL9902", 1);
        Ibutton ibutton5 = new Ibutton("C00239122", "DSL9902", 2);

        ibuttonList.add(ibutton3);
        ibuttonList.add(ibutton4);
        ibuttonList.add(ibutton5);

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
    void save() {

        for (int i = 0; i < game.getNumButtons(); i++) {

            Position position = ((ChoiceBox<Position>) positionBox.getChildren().get(i)).getSelectionModel().getSelectedItem();
            SourceChoice sourceChoice = ((ChoiceBox<SourceChoice>) sourceBox.getChildren().get(i)).getSelectionModel().getSelectedItem();

            if (sourceChoice == null || position == null) {
                // TODO
                continue;
            }

            Ibutton ibutton = new Ibutton();
            List<Measurement> measurements = null;

            if (sourceChoice.getFile() != null) {
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

            for (Measurement measurement : measurements) {
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
