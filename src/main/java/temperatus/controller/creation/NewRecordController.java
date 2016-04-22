package temperatus.controller.creation;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import temperatus.device.DeviceConnectedList;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceReadTask;
import temperatus.exception.ControlledTemperatusException;
import temperatus.importer.AbstractImporter;
import temperatus.importer.IbuttonDataImporter;
import temperatus.lang.Lang;
import temperatus.model.pojo.*;
import temperatus.model.pojo.types.Device;
import temperatus.model.pojo.types.SourceChoice;
import temperatus.model.pojo.utils.AutoCompleteComboBoxListener;
import temperatus.model.service.*;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Add data to the recently created mission. Data can be added from a device or import it from a file.
 * Is not required to add the data to all the positions but at least one must be set.
 * <p>
 * There will be a "row" for each position fo the game. Each row has: id, position, inputSource
 * If input source is a device, user is required tu use the button "Keep Data" to save the information.
 * <p>
 * When save button is pressed:
 * - Data is validated and analyzed.
 * - Configuration screen is loaded.
 * <p>
 * Created by alberto on 31/1/16.
 */
@Controller
@Scope("prototype")
public class NewRecordController extends AbstractCreationController implements Initializable {

    @FXML private StackPane stackPane;
    @FXML private AnchorPane anchorPane;

    @FXML private Label titleLabel; // title of the view
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

    @Autowired DeviceConnectedList deviceConnectedList;             // List of currently connected devices
    @Autowired DeviceReadTask deviceReadTask;                       // read from device task - save to temp file
    @Autowired DeviceOperationsManager deviceOperationsManager;

    private Mission mission;
    private Game game;                                      // Game assigned to the mission
    private ObservableList<Position> defaultPositions;                // Default positions for selected game
    private ObservableList<Position> positions;                       // All positions saved to the db

    private File[] filesToSave;                         // Files where temp data is temporary stored

    private static final Double PREF_HEIGHT = 30.0;     // Preferred height for "rows"
    private static final Double PREF_WIDTH = 200.0;     // Preferred width for combo-box
    private static final Double BOX_PREF_WIDTH = 250.0;     // Preferred width for box
    private static final Double BUTTON_PREF_WIDTH = 127.0;     // Preferred width for keep data button

    private static final String STYLESHEET = "/styles/temperatus.css";

    private static Logger logger = LoggerFactory.getLogger(NewRecordController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        keepSameColumnWidth();
    }

    /**
     * Keep the same width for column names and rows
     */
    private void keepSameColumnWidth() {
        idBoxTitle.prefWidthProperty().bind(idBox.widthProperty());
        positionBoxTitle.prefWidthProperty().bind(positionBox.widthProperty());
        addPositionBoxTitle.prefWidthProperty().bind(addPositionBox.widthProperty());
        sourceBoxTitle.prefWidthProperty().bind(sourceBox.widthProperty());
        addSourceBoxTitle.prefWidthProperty().bind(addSourceBox.widthProperty());
        keepDataBoxTitle.prefWidthProperty().bind(keepDataBox.widthProperty());
    }

    /**
     * Create rows and load all the data related to the mission
     *
     * @param mission mission to load
     */
    public void loadData(Mission mission) {

        this.mission = mission;
        game = mission.getGame();   // Get game assigned to this mission
        defaultPositions = FXCollections.observableArrayList(game.getPositions());   // Get default positions fot the game
        positions = FXCollections.observableArrayList(positionService.getAll());     // Pre-load all positions from db

        // We need to save as many files as numOfButtons has the game
        filesToSave = new File[game.getNumButtons()];

        // The table will have the same number of rows as iButtons/Positions
        for (int index = 0; index < game.getNumButtons(); index++) {

            // Each row will have { ID | POSITION | SOURCE | + | KEEP }

            // ID = index
            // POSITION -> add all positions + if default, select it
            ComboBox<Position> choiceBoxPositions = new ComboBox<>(positions);
            new AutoCompleteComboBoxListener<>(choiceBoxPositions); // Allow write and autocomplete

            if (defaultPositions.size() > index) {
                choiceBoxPositions.getSelectionModel().select(defaultPositions.get(index)); // preselect default position
            }

            // SOURCE -> all detected iButtons
            ComboBox<SourceChoice> choiceBoxSource = new ComboBox<>();

            // if a device is selected activate the button "Keep Data"
            choiceBoxSource.valueProperty().addListener((ov, t, t1) -> {
                logger.debug("selected iButton");
                if (t1 != null) {
                    if (t1.getIbutton() != null) {
                        keepDataBox.getChildren().get((Integer) choiceBoxSource.getUserData()).setDisable(false);
                    } else if (t1.getFile() == null) {
                        keepDataBox.getChildren().get((Integer) choiceBoxSource.getUserData()).setDisable(true);
                    }
                } else {
                    keepDataBox.getChildren().get((Integer) choiceBoxSource.getUserData()).setDisable(true);
                }
            });

            // Add a new "row" with generated info
            addNewRow(index, choiceBoxPositions, choiceBoxSource);
        }

        // At his point all choiceBoxes are full with all positions
        // Also, if the game has default positions, those positions are pre-selected

        // Now we add all detected devices to the source box

        for (Device device : deviceConnectedList.getDevices()) {
            addiButtonToBoxes(device.getSerial());
        }

        // listen for changes: arrival/departure events
        deviceConnectedList.getDevices().addListener((ListChangeListener<? super Device>) change -> {
            while (change.next()) {
                for (Device device : change.getAddedSubList()) {
                    addiButtonToBoxes(device.getSerial());
                }
                for (Device device : change.getRemoved()) {
                    removeiButtonFromBoxes(device.getSerial());
                }
            }
        });
    }

    /**
     * Get row index for a given selected position
     *
     * @param position position to look for
     * @return index of the position or -1 if not found
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
     * @param index index to get the position from
     * @return position at index
     */
    private Position getPositionForIndex(int index) {
        return (Position) ((ComboBox) positionBox.getChildren().get(index)).getSelectionModel().getSelectedItem();
    }

    /**
     * Get Source for a given Index
     *
     * @param index index to get the source from
     * @return source at index
     */
    private SourceChoice getSourceChoiceForIndex(int index) {
        return (SourceChoice) ((ComboBox) sourceBox.getChildren().get(index)).getSelectionModel().getSelectedItem();
    }

    /**
     * Create a new "ROW" of input data with: INDEX | POSITION | + | KEEP DATA
     *
     * @param index  index of the row
     * @param posBox combo-box of positions
     * @param srcBox combo-box of sources
     */
    private void addNewRow(int index, ComboBox<Position> posBox, ComboBox<SourceChoice> srcBox) {
        Label id = new Label(String.valueOf(index));
        id.setPrefHeight(PREF_HEIGHT);
        id.setMaxHeight(PREF_HEIGHT);
        id.setMinHeight(PREF_HEIGHT);
        idBox.getChildren().add(id);

        posBox.getStylesheets().add(STYLESHEET);
        posBox.setMinHeight(PREF_HEIGHT);
        posBox.setMaxHeight(PREF_HEIGHT);
        posBox.setPrefHeight(PREF_HEIGHT);
        posBox.setMaxWidth(Double.MAX_VALUE);
        posBox.setPrefWidth(BOX_PREF_WIDTH);
        posBox.setMinWidth(PREF_WIDTH);
        posBox.setUserData(index);  // required to know in which row is located
        positionBox.getChildren().add(posBox);

        srcBox.getStylesheets().add(STYLESHEET);
        srcBox.setMinHeight(PREF_HEIGHT);
        srcBox.setMaxHeight(PREF_HEIGHT);
        srcBox.setPrefHeight(PREF_HEIGHT);
        srcBox.setMaxWidth(Double.MAX_VALUE);
        srcBox.setPrefWidth(BOX_PREF_WIDTH);
        srcBox.setMinWidth(PREF_WIDTH);
        srcBox.setUserData(index);  // required to know in which row is located
        sourceBox.getChildren().add(srcBox);

        Button importSource = new Button();
        importSource.setMinHeight(PREF_HEIGHT);
        importSource.setMaxHeight(PREF_HEIGHT);
        importSource.setPrefHeight(PREF_HEIGHT);
        importSource.setUserData(index);    // required to know in which row is located
        importSource.getStyleClass().add("ibtn");
        importSource.setText(language.get(Lang.IMPORTPLUS));
        importSource.addEventHandler(MouseEvent.MOUSE_CLICKED, addImportDataButtonHandler());   // action for the button
        addSourceBox.getChildren().addAll(importSource);

        ToggleButton keepButton = new ToggleButton();
        keepButton.setText(language.get(Lang.KEEPDATA));
        keepButton.setMinHeight(PREF_HEIGHT);
        keepButton.setMaxHeight(PREF_HEIGHT);
        keepButton.setPrefHeight(PREF_HEIGHT);
        keepButton.setPrefWidth(BUTTON_PREF_WIDTH);
        keepButton.setUserData(index);  // required to know in which row is located
        keepButton.getStyleClass().add("kbtn");
        keepButton.setDisable(true);    // by default disabled, enable only if device selected in combo-box
        keepButton.addEventHandler(MouseEvent.MOUSE_CLICKED, keepDataForRow()); // action for the button
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

                Device device = getDeviceFromIbutton(sourceChoice.getIbutton());

                if (device != null) {

                    deviceReadTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort());
                    ListenableFuture future = deviceOperationsManager.submitTask(deviceReadTask);

                    ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
                    progressIndicator.setMaxSize(25, 25);

                    clickedButton.setText("");
                    clickedButton.getStyleClass().clear();
                    clickedButton.getStyleClass().add("kpbtn");
                    clickedButton.setGraphic(progressIndicator);

                    Futures.addCallback(future, new FutureCallback<File>() {
                        public void onSuccess(File result) {
                            Platform.runLater(() -> {

                                clickedButton.setGraphic(null);
                                clickedButton.setText(language.get(Lang.SAVEDDATA));
                                clickedButton.getStyleClass().clear();
                                clickedButton.getStyleClass().add("kbtn");

                                sourceChoice.setFile(result);
                                filesToSave[index] = result;
                                // Alert user that iButton can be removed
                                Notifications.create().title("Data Saved").text("You can safely remove iButton now").show();

                            });
                        }

                        public void onFailure(Throwable thrown) {
                            logger.error("Error reading data - Future error");
                        }
                    });

                } else {
                    // TODO show error
                }

            } else {
                positionBox.getChildren().get(index).setDisable(false);
                sourceBox.getChildren().get(index).setDisable(false);
                addSourceBox.getChildren().get(index).setDisable(false);
            }

            event.consume();
        };

        return myHandler;
    }

    private Device getDeviceFromIbutton(Ibutton ibutton) {
        for (Device device : deviceConnectedList.getDevices()) {
            if (device.getSerial().equals(ibutton.getSerial())) {
                return device;
            }
        }
        return null;
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
    private File importDataFromSource() {
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
    private void addiButtonToBoxes(String serial) {

        Ibutton ibutton = ibuttonService.getBySerial(serial);

        if (ibutton == null) {
            ibutton = new Ibutton();
            ibutton.setSerial(serial);
        }

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
     * @param serial
     */
    private void removeiButtonFromBoxes(String serial) {
        for (int i = 0; i < game.getNumButtons(); i++) {
            boolean remove = false;
            // if "keep data" is not selected, remove iButton on this row
            if (!((ToggleButton) keepDataBox.getChildren().get(i)).isSelected()) {
                remove = true;
            }
            // if "keep data" is selected and iButton source is not the same as this ibutton remove
            else if (!serial.equals(((SourceChoice) ((ComboBox) sourceBox.getChildren().get(i)).getSelectionModel().getSelectedItem()).getIbutton().getSerial())) {
                remove = true;
            }

            if (remove) {
                for (SourceChoice sourceChoice : (ObservableList<SourceChoice>) ((ComboBox) sourceBox.getChildren().get(i)).getItems()) {
                    if (sourceChoice.isSameiButton(serial)) {
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

        // TODO comprobar que al menos una ha sido seleccionada

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
                if (validatedData.getStartDate().after(startDate)) {
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
                if (validatedData.getFinishDate().before(endDate)) {
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
    public void translate() {
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
        titleLabel.setText(language.get(Lang.RECORDTITLE));
        indexLabel.setText(language.get(Lang.INDEX));
        positionLabel.setText(language.get(Lang.POSITIONCOLUMN));
        dataSourceLabel.setText(language.get(Lang.DATASOURCECOLUMN));
    }
}
