package temperatus.controller.creation;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.IButtonDataValidator;
import temperatus.analysis.pojo.GeneralData;
import temperatus.analysis.pojo.ValidatedData;
import temperatus.exception.ControlledTemperatusException;
import temperatus.importer.AbstractImporter;
import temperatus.importer.IbuttonDataImporter;
import temperatus.listener.DeviceDetector;
import temperatus.listener.DeviceDetectorListener;
import temperatus.model.pojo.*;
import temperatus.model.pojo.types.SourceChoice;
import temperatus.model.pojo.utils.AutoCompleteComboBoxListener;
import temperatus.model.service.*;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alberto on 31/1/16.
 */
@Controller
@Scope("prototype")
public class NewRecordController extends AbstractCreationController implements Initializable, DeviceDetectorListener {

    @FXML private StackPane stackPane;
    @FXML private AnchorPane anchorPane;

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
    private Game game;                                      // Game assigned to the mission
    private List<Position> defaultPositions;                // Default positions for selected game
    private List<Position> positions;                       // All positions saved to the db
    private List<Ibutton> iButtons = new ArrayList<>();     // Detected iButtons

    private File[] filesToSave;                         // Files where temp data is temporary stored

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

        // We need to save as many files as numOfButtons has the game
        filesToSave = new File[game.getNumButtons()];

        // Get default positions fot the game
        defaultPositions = game.getPositions().stream().collect(Collectors.toList());

        loadAllPositions(); // Pre-load all positions from db

        // The table will have the same number of rows as iButtons/Positions
        for (int index = 0; index < game.getNumButtons(); index++) {

            // Each row will have { ID | POSITION | SOURCE | + | KEEP }

            // ID = index
            // POSITION -> add all positions + if default, select it
            ComboBox<Position> choiceBoxPositions = addAllPositions();
            new AutoCompleteComboBoxListener<>(choiceBoxPositions); // Allow write and autocomplete

            if (defaultPositions.size() > index) {
                choiceBoxPositions.getSelectionModel().select(defaultPositions.get(index));
            }

            // SOURCE -> all detected iButtons
            ComboBox<SourceChoice> choiceBoxSource = new ComboBox<>();
            choiceBoxSource.valueProperty().addListener((ov, t, t1) -> {
                logger.info("selected iButton");
                if (t1 != null) {
                    if (t1.getIbutton() != null) {
                        ((ToggleButton) keepDataBox.getChildren().get((Integer) choiceBoxSource.getUserData())).setDisable(false);
                    } else if (t1.getFile() == null) {
                        ((ToggleButton) keepDataBox.getChildren().get((Integer) choiceBoxSource.getUserData())).setDisable(true);
                    }
                } else {
                    ((ToggleButton) keepDataBox.getChildren().get((Integer) choiceBoxSource.getUserData())).setDisable(true);
                }
            });

            // Add a new "row" with generated info
            addNewRow(index, choiceBoxPositions, choiceBoxSource);
        }

        // At his point all choiceBoxes are full with all positions
        // Also, if the game has default positions, those positions are pre-selected
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

    /**
     * Get Position for a given Index
     *
     * @param index
     * @return
     */
    private Position getPositionForIndex(int index) {
        return (Position) ((ComboBox) positionBox.getChildren().get(index)).getSelectionModel().getSelectedItem();
    }

    /**
     * Get Source for a given Index
     *
     * @param index
     * @return
     */
    private SourceChoice getSourceChoiceForIndex(int index) {
        return (SourceChoice) ((ComboBox) sourceBox.getChildren().get(index)).getSelectionModel().getSelectedItem();
    }


    /**
     * Add all positions to the choicebox
     *
     * @return
     */
    private ComboBox<Position> addAllPositions() {
        ComboBox<Position> choiceBoxPositions = new ComboBox<>();
        choiceBoxPositions.setItems(FXCollections.observableArrayList(positions));
        return choiceBoxPositions;
    }

    /**
     * Create a new "ROW" of input data with: INDEX | POSITION | + | KEEP DATA
     *
     * @param index
     * @param posBox
     * @param srcBox
     */
    private void addNewRow(int index, ComboBox<Position> posBox, ComboBox<SourceChoice> srcBox) {
        Label id = new Label(String.valueOf(index));
        id.setPrefHeight(prefHeight);
        id.setMaxHeight(prefHeight);
        id.setMinHeight(prefHeight);

        idBox.getChildren().add(id);

        posBox.getStylesheets().add("/styles/temperatus.css");
        posBox.setMinHeight(prefHeight);
        posBox.setMaxHeight(prefHeight);
        posBox.setPrefHeight(prefHeight);
        posBox.setMaxWidth(Double.MAX_VALUE);
        posBox.setPrefWidth(250);
        posBox.setMinWidth(prefWidth);
        posBox.setUserData(index);

        positionBox.getChildren().add(posBox);

        srcBox.getStylesheets().add("/styles/temperatus.css");
        srcBox.setMinHeight(prefHeight);
        srcBox.setMaxHeight(prefHeight);
        srcBox.setPrefHeight(prefHeight);
        srcBox.setMaxWidth(Double.MAX_VALUE);
        srcBox.setPrefWidth(250);
        srcBox.setMinWidth(prefWidth);
        srcBox.setUserData(index);

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

            if (((ComboBox) positionBox.getChildren().get(index)).getSelectionModel().getSelectedItem() == null) {

                logger.warn("A position must be selected");
                showAlert(Alert.AlertType.ERROR, "A position must be selected");
                clickedButton.setSelected(false);

            } else if (clickedButton.isSelected()) {

                positionBox.getChildren().get(index).setDisable(true);
                sourceBox.getChildren().get(index).setDisable(true);
                addSourceBox.getChildren().get(index).setDisable(true);

                SourceChoice sourceChoice = (SourceChoice) ((ComboBox) sourceBox.getChildren().get(index)).getSelectionModel().getSelectedItem();

                // TODO save data to temp file!

                // Alert user that iButton can be removed
                Notifications.create().title("Data Saved").text("You can safely remove iButton now").show();

            } else {
                positionBox.getChildren().get(index).setDisable(false);
                sourceBox.getChildren().get(index).setDisable(false);
                addSourceBox.getChildren().get(index).setDisable(false);
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

            if (file != null) {
                SourceChoice sourceChoice = new SourceChoice(file);

                Button clickedButton = (Button) event.getSource();
                Integer index = (Integer) clickedButton.getUserData();

                if (sourceBox.getChildren().get(index) instanceof ComboBox) {
                    if (!((ComboBox) sourceBox.getChildren().get(index)).getItems().contains(sourceChoice)) {
                        ((ComboBox) sourceBox.getChildren().get(index)).getItems().add(sourceChoice);
                    }
                    ((ComboBox) sourceBox.getChildren().get(index)).getSelectionModel().select(sourceChoice);

                    filesToSave[index] = file;
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
        return fileChooser.showOpenDialog(null);
    }

    /**
     * On iButton arrival, add it to all boxes and select it in its corresponding position if exists
     */
    private void addiButtonToBoxes(Ibutton ibutton) {

        SourceChoice sourceChoice = new SourceChoice(ibutton);
        for (int i = 0; i < game.getNumButtons(); i++) {
            ((ComboBox) sourceBox.getChildren().get(i)).getItems().add(sourceChoice);
        }

        Position defaultPositionForIbutton = ibutton.getPosition();
        if (defaultPositionForIbutton != null) {
            // If game default positions contains the same position as ibutton default position
            // Set that ibutton to that position
            if (defaultPositions.contains(defaultPositionForIbutton)) {
                int index = getRowForPosition(defaultPositionForIbutton);
                ((ComboBox<SourceChoice>) sourceBox.getChildren().get(index)).getSelectionModel().select(sourceChoice);
            }
        }
    }

    /**
     * Remove iButton from all boxes but when "keep data" selected
     *
     * @param ibutton
     */
    private void removeiButtonFromBoxes(Ibutton ibutton) {
        for (int i = 0; i < game.getNumButtons(); i++) {
            boolean remove = false;
            // if "keep data" is not selected, remove iButton on this row
            if (!((ToggleButton) keepDataBox.getChildren().get(i)).isSelected()) {
                remove = true;
            }
            // if "keep data" is selected and iButton source is not the same as this ibutton remove
            else if (!ibutton.equals(((SourceChoice) ((ComboBox) sourceBox.getChildren().get(i)).getSelectionModel().getSelectedItem()).getIbutton())) {
                remove = true;
            }

            if (remove) {
                for (SourceChoice sourceChoice : (ObservableList<SourceChoice>) ((ComboBox) sourceBox.getChildren().get(i)).getItems()) {
                    if (sourceChoice.isSameiButton(ibutton)) {
                        ((ComboBox) sourceBox.getChildren().get(i)).getItems().remove(sourceChoice);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Import data from file
     *
     * @param index
     * @return
     */
    private IbuttonDataImporter importIbuttonData(int index) {
        if (filesToSave[index] != null) {
            try {
                return new IbuttonDataImporter(filesToSave[index]);
            } catch (ControlledTemperatusException e) {
                // TODO show warning with e.getMessage()
            }
        }
        return null;
    }

    /**
     * Look for posible errors on the data
     * Save (if not saved already) the iButtons
     *
     * @param index
     * @param importedData
     * @return
     */
    private ValidatedData validateData(int index, AbstractImporter importedData) {

        ValidatedData validatedData = new ValidatedData(importedData);
        validatedData.setPossibleErrors(IButtonDataValidator.getAllOutliers(importedData.getMeasurements()));
        validatedData.setPosition(getPositionForIndex(index));

        SourceChoice sourceChoice = getSourceChoiceForIndex(index);
        if (sourceChoice.getIbutton() != null) {
            validatedData.setIbutton(sourceChoice.getIbutton());
        } else {

            Ibutton ibutton = ibuttonService.getBySerial(validatedData.getDeviceSerial());

            // This situation means that user has imported data to the computer with another program
            // and thi iButton has never been registered in the application
            if (ibutton == null) {

                // iButton must be saved to db
                ibutton = new Ibutton(validatedData.getPosition(), validatedData.getDeviceSerial(), validatedData.getDeviceModel(), null);
                ibuttonService.save(ibutton);
            }

            validatedData.setIbutton(ibutton);
        }

        return validatedData;
    }


    @FXML
    void save() {
        List<ValidatedData> validatedDataList = new ArrayList<>();
        GeneralData generalData = new GeneralData();

        ProgressIndicator pForm = new ProgressIndicator();

        Task<Void> importAndValidateTask = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {

                // Import and check
                for (int index = 0; index < game.getNumButtons(); index++) {
                    updateProgress(index, game.getNumButtons() - 1);

                    // Import data
                    AbstractImporter importedData = importIbuttonData(index);

                    if (importedData != null) {
                        // Validate data
                        ValidatedData validatedData = validateData(index, importedData);

                        // Save data
                        validatedDataList.add(validatedData);
                    }
                }

                // TODO create popup to show problematic data and manage it


                // TODO compare data of different buttons
                generalData.setAvgTemp(12.5);
                generalData.setEndDate(getGlobalEndDate(validatedDataList));
                generalData.setStartDate(getGlobalStartDate(validatedDataList));
                generalData.setMaxTemp(15.0);
                generalData.setMinTemp(11.4);
                generalData.setRate("5 seconds");
                generalData.setModels("DS1922L");
                generalData.setMeasurementsPerButton(255);


                // TODO show problematic data


                // save Records to database
                Set<Record> records = new HashSet<>();
                for (ValidatedData validatedData : validatedDataList) {
                    Record record = new Record(validatedData.getIbutton(), mission, validatedData.getPosition());
                    recordService.save(record);

                    for (int i = 0; i < validatedData.getMeasurements().size(); i++) {
                        validatedData.getMeasurements().get(i).setRecord(record);
                    }

                    for (int i = 0; i < validatedData.getPossibleErrors().size(); i++) {
                        validatedData.getPossibleErrors().get(i).setRecord(record);
                    }

                    records.add(record);
                }
                mission.setRecords(records);

                updateProgress(10, 10);

                return null;
            }
        };

        // binds progress of progress bars to progress of task:
        pForm.progressProperty().bind(importAndValidateTask.progressProperty());

        importAndValidateTask.setOnSucceeded(event -> {
            RecordConfigController recordConfigController = VistaNavigator.pushViewToStack(Constants.RECORD_CONFIG);
            recordConfigController.setMission(mission);
            recordConfigController.setData(validatedDataList, generalData);

            stackPane.getChildren().remove(stackPane.getChildren().size() - 1);
            anchorPane.setDisable(false);
        });

        anchorPane.setDisable(true);

        VBox box = new VBox(pForm);
        box.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(box);

        Thread thread = new Thread(importAndValidateTask);
        thread.start();
    }

    private Date getGlobalStartDate(List<ValidatedData> data) {
        if (data.size() > 0) {
            Date startDate = data.get(0).getStartDate();
            for (ValidatedData validatedData : data) {
                if(validatedData.getStartDate().after(startDate)) {
                    startDate = validatedData.getStartDate();
                }
            }
            return startDate;
        }
        return null;
    }

    private Date getGlobalEndDate(List<ValidatedData> data) {
        if (data.size() > 0) {
            Date endDate = data.get(0).getFinishDate();
            for (ValidatedData validatedData : data) {
                if(validatedData.getFinishDate().before(endDate)) {
                    endDate = validatedData.getFinishDate();
                }
            }
            return endDate;
        }
        return null;
    }

    @FXML
    private void cancel() {

    }

    @Override
    public void reload(Object object) {
        if (object instanceof Position) {
            Position newPosition = (Position) object;

            for (int i = 0; i < game.getNumButtons(); i++) {
                ((ComboBox) positionBox.getChildren().get(i)).getItems().add(newPosition);
            }
        }
    }

    @Override
    public void arrival(DeviceDetector event) {
        Ibutton ibutton = ibuttonService.getBySerial(event.getSerial());

        if (ibutton != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    addiButtonToBoxes(ibutton);
                }
            });
        }
    }

    @Override
    public void departure(DeviceDetector event) {
        Ibutton ibutton = ibuttonService.getBySerial(event.getSerial());

        if (ibutton != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    removeiButtonFromBoxes(ibutton);
                }
            });
        }
    }

    @Override
    public void translate() {


    }
}
