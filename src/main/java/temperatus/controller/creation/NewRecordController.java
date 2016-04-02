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
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.importer.IbuttonDataImporter;
import temperatus.listener.DeviceDetector;
import temperatus.listener.DeviceDetectorListener;
import temperatus.model.pojo.*;
import temperatus.model.pojo.types.AutoCompleteComboBoxListener;
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
import java.util.stream.Collectors;

/**
 * Created by alberto on 31/1/16.
 */
@Controller
@Scope("prototype")
public class NewRecordController extends AbstractCreationController implements Initializable, DeviceDetectorListener {

    @FXML private Label titleLabel;
    @FXML private Label indexLabel;
    @FXML private Label positionLabel;
    @FXML private Label dataSourceLabel;

    @FXML private ScrollPane scrollPane;

    @FXML private VBox idBox;
    @FXML private VBox positionBox;
    @FXML private VBox addPositionBox;
    @FXML private VBox sourceBox;
    @FXML private VBox addSourceBox;
    @FXML private VBox keepDataBox;

    @FXML private VBox idBoxTitle;
    @FXML private VBox positionBoxTitle;
    @FXML private VBox addPositionBoxTitle;
    @FXML private VBox sourceBoxTitle;
    @FXML private VBox addSourceBoxTitle;
    @FXML private VBox keepDataBoxTitle;

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
    private final Double prefWidth = 200.0;     // Preferred width for combobox

    static Logger logger = LoggerFactory.getLogger(NewProjectController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        idBoxTitle.prefWidthProperty().bind(idBox.widthProperty());
        positionBoxTitle.prefWidthProperty().bind(positionBox.widthProperty());
        addPositionBoxTitle.prefWidthProperty().bind(addPositionBox.widthProperty());
        sourceBoxTitle.prefWidthProperty().bind(sourceBox.widthProperty());
        addSourceBoxTitle.prefWidthProperty().bind(addSourceBox.widthProperty());
        keepDataBoxTitle.prefWidthProperty().bind(keepDataBox.widthProperty());
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
        game = mission.getGame();

        // Get default positions fot the game
        defaultPositions = game.getPositions().stream().collect(Collectors.toList());

        loadAllPositions(); // Pre-load all positions from db

        iButtons = detectButtons();

        // The table will have the same number of rows as iButtons/Positions
        for (int index = 0; index < game.getNumButtons(); index++) {

            // Each row will have { ID | POSITION | + | SOURCE | + }

            // ID = index
            // POSITION -> add all positions + if default, select it
            ComboBox<Position> choiceBoxPositions = addAllPositions();
            new AutoCompleteComboBoxListener<>(choiceBoxPositions); // Allow write and autocomplete

            if (defaultPositions.size() > index) {
                choiceBoxPositions.getSelectionModel().select(defaultPositions.get(index));
            }

            // SOURCE -> all detected iButtons
            ComboBox<SourceChoice> choiceBoxSource = addAllDetectediButtons();

            // Add a new "row" with generated info
            addNewRow(index, choiceBoxPositions, choiceBoxSource);
        }

        // At his point all choiceBoxes are full with all positions and all detected buttons
        // Also, if the game has default positions, those positions are pre-selected

        // For each detected button, compare if its default position is equal to any of the default positions of the game
        for (Ibutton ibutton : iButtons) {

            Position defaultPositionForIbutton = ibutton.getPosition();

            if (defaultPositionForIbutton != null) {
                // If game default positions contains the same position as ibutton default position
                // Set that ibutton to that position
                if (defaultPositions.contains(defaultPositionForIbutton)) {
                    int index = getRowForPosition(defaultPositionForIbutton);
                    ((ComboBox<SourceChoice>) sourceBox.getChildren().get(index)).getSelectionModel().select(new SourceChoice(ibutton));
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
            if (((ComboBox<Position>) positionBox.getChildren().get(index)).getSelectionModel().getSelectedItem().equals(position)) {
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
    private ComboBox<Position> addAllPositions() {
        ComboBox<Position> choiceBoxPositions = new ComboBox<>();
        choiceBoxPositions.getStylesheets().add("/styles/temperatus.css");
        choiceBoxPositions.setItems(FXCollections.observableArrayList(positions));
        return choiceBoxPositions;
    }

    /**
     * Create a SourceChoice for each iButton detected and add it to the ComboBox
     *
     * @return
     */
    private ComboBox<SourceChoice> addAllDetectediButtons() {
        ComboBox<SourceChoice> choiceBoxSource = new ComboBox<>();
        choiceBoxSource.getStylesheets().add("/styles/temperatus.css");

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
                position = ((ComboBox<Position>) positionBox.getChildren().get(index)).getSelectionModel().getSelectedItem();
                sourceChoice = ((ComboBox<SourceChoice>) sourceBox.getChildren().get(index)).getSelectionModel().getSelectedItem();

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

                for (Measurement measurement : measurements) {
                    measurement.setRecord(record);
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


    }

    private void addNewRow(int index, ComboBox<Position> posBox, ComboBox<SourceChoice> srcBox) {
        Label id = new Label(String.valueOf(index));
        id.setPrefHeight(prefHeight);
        id.setMaxHeight(prefHeight);
        id.setMinHeight(prefHeight);

        idBox.getChildren().add(id);

        posBox.setMinHeight(prefHeight);
        posBox.setMaxHeight(prefHeight);
        posBox.setPrefHeight(prefHeight);
        posBox.setMaxWidth(Double.MAX_VALUE);
        posBox.setPrefWidth(250);
        posBox.setMinWidth(prefWidth);

        positionBox.getChildren().add(posBox);

        srcBox.setMinHeight(prefHeight);
        srcBox.setMaxHeight(prefHeight);
        srcBox.setPrefHeight(prefHeight);
        srcBox.setMaxWidth(Double.MAX_VALUE);
        srcBox.setPrefWidth(250);
        srcBox.setMinWidth(prefWidth);

        sourceBox.getChildren().add(srcBox);

        Button importSource = new Button();
        importSource.setMinHeight(prefHeight);
        importSource.setMaxHeight(prefHeight);
        importSource.setPrefHeight(prefHeight);
        importSource.setUserData(index);
        importSource.getStyleClass().add("ibtn");
        importSource.setText("+");
        importSource.addEventHandler(MouseEvent.MOUSE_CLICKED, addImportDataButtonHandler());

        addSourceBox.getChildren().addAll(importSource);

        ToggleButton keepButton = new ToggleButton();
        keepButton.setText("Keep Data");
        keepButton.setMinHeight(prefHeight);
        keepButton.setMaxHeight(prefHeight);
        keepButton.setPrefHeight(prefHeight);
        keepButton.setUserData(index);
        keepButton.getStyleClass().add("kbtn");
        keepButton.setDisable(true);
        keepButton.addEventHandler(MouseEvent.MOUSE_CLICKED, keepDataForRow());

        keepDataBox.getChildren().addAll(keepButton);
    }

    /**
     * Handle action when a keep data button is pressed
     *
     * @return
     */
    private EventHandler<Event> keepDataForRow() {
        final EventHandler<Event> myHandler = event -> {

            ToggleButton clickedButton = (ToggleButton) event.getSource();
            Integer index = (Integer) clickedButton.getUserData();

            if(((ComboBox) positionBox.getChildren().get(index)).getSelectionModel().getSelectedItem() == null) {

                logger.warn("A position must be selected");
                showAlert(Alert.AlertType.ERROR, "A position must be selected");
                clickedButton.setSelected(false);

            } else if(clickedButton.isSelected()) {

                ((ComboBox) positionBox.getChildren().get(index)).setDisable(true);
                ((ComboBox) sourceBox.getChildren().get(index)).setDisable(true);
                ((Button) addSourceBox.getChildren().get(index)).setDisable(true);

                SourceChoice sourceChoice = (SourceChoice) ((ComboBox) sourceBox.getChildren().get(index)).getSelectionModel().getSelectedItem();

                // TODO save data to temp file!


                // Alert user that iButton can be removed
                Notifications.create().title("Data Saved").text("You can safely remove iButton now").show();

            } else {
                ((ComboBox) positionBox.getChildren().get(index)).setDisable(false);
                ((ComboBox) sourceBox.getChildren().get(index)).setDisable(false);
                ((Button) addSourceBox.getChildren().get(index)).setDisable(false);
            }

            event.consume();
        };

        return myHandler;
    }

    /**
     * Handle action when an import button is pressed
     *
     * @return
     */
    private EventHandler<Event> addImportDataButtonHandler() {
        final EventHandler<Event> myHandler = event -> {
            File file = importDataFromSource();

            if(file != null) {
                SourceChoice sourceChoice = new SourceChoice(file);

                Button clickedButton = (Button) event.getSource();
                Integer index = (Integer) clickedButton.getUserData();

                if (sourceBox.getChildren().get(index) instanceof ComboBox) {
                    if(!((ComboBox) sourceBox.getChildren().get(index)).getItems().contains(sourceChoice)) {
                        ((ComboBox) sourceBox.getChildren().get(index)).getItems().add(sourceChoice);
                    }
                    ((ComboBox) sourceBox.getChildren().get(index)).getSelectionModel().select(sourceChoice);

                    // TODO remove
                    ((ToggleButton) keepDataBox.getChildren().get(index)).setDisable(false);

                }
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

    @Override
    public void arrival(DeviceDetector event) {

    }

    @Override
    public void departure(DeviceDetector event) {

    }
}
