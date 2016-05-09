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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.IButtonDataAnalysis;
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
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.SpringFxmlLoader;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Add data to the recently created mission. Data can be added from a device or import it from a file.
 * Is not required to add the data to all the positions but at least one must be set.
 * <p>
 * It is also possible to edit the data from a previously saved mission. Then the "Keep Data" button
 * becomes a "Delete Data" button allowing the user to delete the data of a row...
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
    @FXML private Label importLabel;

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
    @Autowired MissionService missionService;

    @Autowired DeviceConnectedList deviceConnectedList;             // List of currently connected devices
    @Autowired DeviceReadTask deviceReadTask;                       // read from device task - save to temp file
    @Autowired DeviceOperationsManager deviceOperationsManager;

    private Mission mission;
    private Game game;                                      // Game assigned to the mission
    private ObservableList<Position> defaultPositions;                // Default positions for selected game
    private ObservableList<Position> positions;                       // All positions saved to the db

    private File[] filesToSave;                         // Files where temp data is temporary stored
    private boolean isUpdate;

    private File lastOpenedDirectory;        // Save the last directory opened to reopen if more imports

    private static final Double PREF_HEIGHT = 30.0;     // Preferred height for "rows"
    private static final Double PREF_WIDTH = 180.0;     // Preferred width for combo-box
    private static final Double BOX_PREF_WIDTH = 230.0;     // Preferred width for box
    private static final Double BUTTON_PREF_WIDTH = 127.0;     // Preferred width for keep data button
    private static final Double PSIZE = 25.0;     // Preferred size for keep data button progress indicator

    private static final String STYLESHEET = "/styles/temperatus.css";

    private static Logger logger = LoggerFactory.getLogger(NewRecordController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        keepSameColumnWidth();
        isUpdate = false;
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
    void loadData(Mission mission, boolean isUpdate) {

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

        // if this is an update, select the previously saved elements
        if (isUpdate) {
            getUpdateReady();
        }
    }

    /**
     * Prepare the rows where the mission already has data in
     * Select the positions and sourceChoices already saved and set all to disable
     * Keep button becomes Delete button
     */
    private void getUpdateReady() {
        logger.info("Setting records and data for update...");
        isUpdate = true;

        List<Record> records = new ArrayList<>(mission.getRecords());
        for (int i = 0; i < mission.getGame().getNumButtons() && i < records.size(); i++) {
            getPositionComboBoxForIndex(i).getSelectionModel().select(records.get(i).getPosition());

            SourceChoice sourceChoice = new SourceChoice(records.get(i));
            getSourceChoiceComboBoxForIndex(i).getItems().add(sourceChoice);
            getSourceChoiceComboBoxForIndex(i).getSelectionModel().select(sourceChoice);

            positionBox.getChildren().get(i).setDisable(true);
            sourceBox.getChildren().get(i).setDisable(true);
            addSourceBox.getChildren().get(i).setDisable(true);

            setButtonStyleRemove((ToggleButton) keepDataBox.getChildren().get(i));
            saveButton.setText(language.get(Lang.UPDATE));
            titleLabel.setText(language.get(Lang.UPDATE_NEW_RECORD_TITLE));
        }
    }

    /**
     * Returns the ComboBox of positions of the given row
     *
     * @param index row to look for
     * @return comboBox of positions
     */
    private ComboBox<Position> getPositionComboBoxForIndex(int index) {
        return (ComboBox<Position>) positionBox.getChildren().get(index);
    }

    /**
     * Returns the ComboBox of sourceChoice of the given row
     *
     * @param index row to look for
     * @return comboBox of sourceChoice
     */
    private ComboBox<SourceChoice> getSourceChoiceComboBoxForIndex(int index) {
        return (ComboBox<SourceChoice>) sourceBox.getChildren().get(index);
    }


    /**
     * Get row index for a given selected position
     *
     * @param position position to look for
     * @return index of the position or -1 if not found
     */
    private int getRowForPosition(Position position) {
        for (int index = 0; index < positionBox.getChildren().size(); index++) {
            if (getPositionComboBoxForIndex(index).getSelectionModel().getSelectedItem().equals(position)) {
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
        return getPositionComboBoxForIndex(index).getSelectionModel().getSelectedItem();
    }

    /**
     * Get Source for a given Index
     *
     * @param index index to get the source from
     * @return source at index
     */
    private SourceChoice getSourceChoiceForIndex(int index) {
        return getSourceChoiceComboBoxForIndex(index).getSelectionModel().getSelectedItem();
    }

    /**
     * Create a new "ROW" of input data with: INDEX | POSITION | + | KEEP DATA
     *
     * @param index  index of the row
     * @param posBox combo-box of positions
     * @param srcBox combo-box of sources
     */
    private void addNewRow(int index, ComboBox<Position> posBox, ComboBox<SourceChoice> srcBox) {
        Label id = new Label(String.valueOf(index + 1));
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
        changeFileToSaveEvent(srcBox);
        sourceBox.getChildren().add(srcBox);

        Button importSource = new Button();
        importSource.setMinHeight(PREF_HEIGHT);
        importSource.setMaxHeight(PREF_HEIGHT);
        importSource.setPrefHeight(PREF_HEIGHT);
        importSource.setUserData(index);    // required to know in which row is located
        importSource.getStyleClass().add("ibtn");
        importSource.setText(language.get(Lang.IMPORTPLUS));
        importSource.addEventHandler(MouseEvent.MOUSE_CLICKED, importMoreThanOneFile());   // action for the button
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
     * On selection change, rewrite the file to save on the array
     *
     * @param sourceChoiceComboBox combo-box to add the event
     */
    private void changeFileToSaveEvent(ComboBox<SourceChoice> sourceChoiceComboBox) {
        sourceChoiceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filesToSave[(Integer) sourceChoiceComboBox.getUserData()] = newValue.getFile();
            }
        });
    }

    /**
     * Handle action when a keep data button is pressed
     *
     * @return eventHandler
     */
    private EventHandler<Event> keepDataForRow() {
        final EventHandler<Event> myHandler = event -> {

            ToggleButton clickedButton = (ToggleButton) event.getSource();  // button pressed
            Integer index = (Integer) clickedButton.getUserData();          // index (row) of the button

            if (getPositionComboBoxForIndex(index).getSelectionModel().getSelectedItem() == null) {
                logger.warn("A position must be selected");
                showAlert(Alert.AlertType.ERROR, language.get(Lang.MUST_SELECT_POSITION));
                clickedButton.setSelected(false);
            } else if (clickedButton.isSelected()) {    // save data to a temp file

                positionBox.getChildren().get(index).setDisable(true);
                sourceBox.getChildren().get(index).setDisable(true);
                addSourceBox.getChildren().get(index).setDisable(true);

                // sourceChoice corresponding to the selected row
                SourceChoice sourceChoice = getSourceChoiceComboBoxForIndex(index).getSelectionModel().getSelectedItem();
                Device device = getDeviceFromIbutton(sourceChoice.getIbutton());    // get its corresponding device

                if (device != null) {
                    deviceReadTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort(), true);
                    ListenableFuture future = deviceOperationsManager.submitTask(deviceReadTask);
                    setButtonStyleWithProgressIndicator(clickedButton);     // show infinite progress indicator

                    Futures.addCallback(future, new FutureCallback<Object>() {
                        public void onSuccess(Object result) {
                            Platform.runLater(() -> {
                                setButtonStyleNormal(clickedButton);    // set style back to normal
                                sourceChoice.setFile((File) result);           // set created file to the sourceChoice
                                filesToSave[index] = (File) result;            // save created file

                                // Alert user that iButton can be removed
                                Notifications.create().title(language.get(Lang.DATASAVEDTITLE)).text(language.get(Lang.DATASAVEDTEXT)).show();
                                logger.info("Data saved successfully");
                            });
                        }

                        public void onFailure(Throwable thrown) {
                            logger.error("Error reading data - Future error");
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, language.get(Lang.READING_DEVICE_ERROR));
                                clickedButton.setSelected(false);
                                setButtonStyleNormal(clickedButton);
                            });
                        }
                    });

                } else if (sourceChoice.getRecord() != null) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION_DELETE_RECORD));
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && ButtonType.OK == result.get()) {

                        // delete record from database
                        mission.getRecords().remove(sourceChoice.getRecord());
                        recordService.delete(sourceChoice.getRecord());

                        getSourceChoiceComboBoxForIndex(index).getSelectionModel().clearSelection();
                        getSourceChoiceComboBoxForIndex(index).getItems().remove(sourceChoice);

                        enableRow(index);
                        setButtonStyleNormal(clickedButton);

                        clickedButton.setText(language.get(Lang.KEEPDATA));
                        clickedButton.setSelected(false);
                        clickedButton.setDisable(true);

                    } else {
                        setButtonStyleRemove(clickedButton);    // keep same state
                    }
                } else {
                    logger.warn("Error looking for device... is possible that device is no longer connected?");
                    showAlert(Alert.AlertType.ERROR, language.get(Lang.DEVICE_NOT_FOUND_ERROR));
                }
            } else {    // allow user to change all data from the row again
                clickedButton.setText(language.get(Lang.KEEPDATA));
                enableRow(index);
            }
            event.consume();
        };
        return myHandler;
    }

    /**
     * Set elements of a row to enabled state
     *
     * @param index row index
     */
    private void enableRow(int index) {
        positionBox.getChildren().get(index).setDisable(false);
        sourceBox.getChildren().get(index).setDisable(false);
        addSourceBox.getChildren().get(index).setDisable(false);
    }

    /**
     * Set button style to delete row info
     *
     * @param button button to modify
     */
    private void setButtonStyleRemove(ToggleButton button) {
        button.setGraphic(null);
        button.setText(language.get(Lang.DELETEDATA));
        button.getStyleClass().clear();
        button.getStyleClass().add("kdbtn");
        button.setDisable(false);
        button.setSelected(false);
    }

    /**
     * Set button style to its normal state
     *
     * @param button button to modify
     */
    private void setButtonStyleNormal(ToggleButton button) {
        button.setGraphic(null);
        button.setText(language.get(Lang.SAVEDDATA));
        button.getStyleClass().clear();
        button.getStyleClass().add("kbtn");
        button.setDisable(false);
    }

    /**
     * Show a indeterminate progress indicator over the button
     *
     * @param button button to modify
     */
    private void setButtonStyleWithProgressIndicator(ToggleButton button) {
        ProgressIndicator progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressIndicator.setMaxSize(PSIZE, PSIZE);
        button.setGraphic(progressIndicator);

        button.setText("");
        button.getStyleClass().clear();
        button.getStyleClass().add("kpbtn");
        button.setDisable(true);
    }

    /**
     * Get device information (container, adapter...) from a serial
     *
     * @param ibutton serial to look for
     * @return device with same serial as passed
     */
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
     * @return eventHandler
     */
    private EventHandler<Event> addImportDataButtonHandler() {
        final EventHandler<Event> myHandler = event -> {
            File file = importDataFromSource(); // select a file (csv) from the computer

            if (file != null) {
                SourceChoice sourceChoice = new SourceChoice(file);
                lastOpenedDirectory = file.getParentFile();

                Button clickedButton = (Button) event.getSource();
                Integer index = (Integer) clickedButton.getUserData();

                if (sourceBox.getChildren().get(index) instanceof ComboBox) {
                    // check if already added this same file to the combo-box
                    if (!((ComboBox) sourceBox.getChildren().get(index)).getItems().contains(sourceChoice)) {
                        getSourceChoiceComboBoxForIndex(index).getItems().add(sourceChoice);
                    }
                    getSourceChoiceComboBoxForIndex(index).getSelectionModel().select(sourceChoice); // select it

                    filesToSave[index] = file;
                }
            }
            event.consume();
        };

        return myHandler;
    }

    /**
     * Handle action when an import button is pressed.
     * Allow to select more than one file. When more than one is selected, add them to the next rows.
     */
    private EventHandler<Event> importMoreThanOneFile() {
        final EventHandler<Event> myHandler = event -> {

            FileChooser fileChooser = new FileChooser();

            if (lastOpenedDirectory != null) {
                fileChooser.setInitialDirectory(lastOpenedDirectory);
            }

            //Set extension filter
            FileChooser.ExtensionFilter extFilterCSV = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.CSV");
            fileChooser.getExtensionFilters().add(extFilterCSV);

            //Show open file dialog
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stackPane.getScene().getWindow());

            Button clickedButton = (Button) event.getSource();
            Integer row = (Integer) clickedButton.getUserData();

            int actual_index = 0;

            // Add the files to the rows
            if (selectedFiles != null) {
                for (int index = row; actual_index < selectedFiles.size() && index < game.getNumButtons(); index++) {

                    File actual = selectedFiles.get(actual_index++);

                    if (actual != null) {
                        SourceChoice sourceChoice = new SourceChoice(actual);
                        lastOpenedDirectory = actual.getParentFile();

                        if (sourceBox.getChildren().get(index) instanceof ComboBox) {
                            // check if already added this same file to the combo-box
                            if (!((ComboBox) sourceBox.getChildren().get(index)).getItems().contains(sourceChoice)) {
                                getSourceChoiceComboBoxForIndex(index).getItems().add(sourceChoice);
                            }

                            // select it only if no "Keep Data" active
                            if (!sourceBox.getChildren().get(index).isDisable()) {
                                getSourceChoiceComboBoxForIndex(index).getSelectionModel().select(sourceChoice); // select it
                                filesToSave[index] = actual;
                            }
                        }
                    }

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

        if (lastOpenedDirectory != null) {
            fileChooser.setInitialDirectory(lastOpenedDirectory);
        }

        //Set extension filter
        FileChooser.ExtensionFilter extFilterCSV = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.CSV");
        fileChooser.getExtensionFilters().add(extFilterCSV);

        //Show open file dialog
        return fileChooser.showOpenDialog(stackPane.getScene().getWindow());
    }

    /**
     * On iButton arrival, add it to all boxes and select it in its corresponding position if exists
     *
     * @param serial ibutton serial
     */
    private void addiButtonToBoxes(String serial) {

        Ibutton ibutton = ibuttonService.getBySerial(serial);

        if (ibutton == null) {
            ibutton = new Ibutton();
            ibutton.setSerial(serial);
        }

        SourceChoice sourceChoice = new SourceChoice(ibutton);
        for (int i = 0; i < game.getNumButtons(); i++) {
            getSourceChoiceComboBoxForIndex(i).getItems().add(sourceChoice);
        }

        Position defaultPositionForIbutton = ibutton.getPosition();
        if (defaultPositionForIbutton != null) {
            // If game default positions contains the same position as ibutton default position
            // Set that ibutton to that position
            if (defaultPositions.contains(defaultPositionForIbutton)) {
                int index = getRowForPosition(defaultPositionForIbutton);
                getSourceChoiceComboBoxForIndex(index).getSelectionModel().select(sourceChoice);
            }
        }
    }

    /**
     * Remove iButton from all boxes but when "keep data" selected
     *
     * @param serial ibutton serial
     */
    private void removeiButtonFromBoxes(String serial) {
        for (int i = 0; i < game.getNumButtons(); i++) {
            boolean remove = false;
            // if "keep data" is not selected, remove iButton on this row
            if (!((ToggleButton) keepDataBox.getChildren().get(i)).isSelected()) {
                remove = true;
            }
            // if "keep data" is selected and iButton source is not the same as this ibutton remove
            else if (!serial.equals(getSourceChoiceComboBoxForIndex(i).getSelectionModel().getSelectedItem().getIbutton().getSerial())) {
                remove = true;
            }

            if (remove) {
                for (SourceChoice sourceChoice : getSourceChoiceComboBoxForIndex(i).getItems()) {
                    if (sourceChoice.isSameiButton(serial)) {
                        getSourceChoiceComboBoxForIndex(i).getItems().remove(sourceChoice);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Import data from file
     *
     * @param index row to import data to
     * @return iButtonDataImporter with all the info
     */
    private IbuttonDataImporter importIbuttonData(int index) throws ControlledTemperatusException {
        if (filesToSave[index] != null) {
            try {
                return new IbuttonDataImporter(filesToSave[index]);
            } catch (ControlledTemperatusException e) {
                int row = index + 1;
                throw new ControlledTemperatusException(language.get(Lang.INDEX) + ": " + row + "   " + language.get(Lang.PROCESSING_ERROR) + "  " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Look for possible errors on the data
     * Save (if not saved already) the iButtons
     *
     * @param index        row to validate
     * @param importedData data imported from the source of the row
     * @return ValidatedData object with the validated data
     */
    private ValidatedData validateData(int index, AbstractImporter importedData) throws ControlledTemperatusException {

        ValidatedData validatedData = new ValidatedData(importedData);
        Position position = getPositionForIndex(index);

        if (position == null) {
            throw new ControlledTemperatusException(language.get(Lang.MUST_SELECT_POSITION_FOR_ALL));
        }

        validatedData.setPossibleErrors(IButtonDataValidator.getAllOutliers(importedData.getMeasurements()));
        validatedData.setPosition(position);

        Ibutton ibutton = ibuttonService.getBySerial(validatedData.getDeviceSerial());

        SourceChoice sourceChoice = getSourceChoiceForIndex(index);
        if (sourceChoice.getIbutton() != null) {
            if (ibutton == null) {
                validatedData.setIbutton(sourceChoice.getIbutton());
            } else {
                validatedData.setIbutton(ibutton);
            }
        } else {
            // This situation means that user has imported data to the computer with another program
            // and this iButton has never been registered in the application
            if (ibutton == null) {

                // iButton must be saved to db
                ibutton = new Ibutton(validatedData.getPosition(), validatedData.getDeviceSerial(), validatedData.getDeviceModel(), null);
                ibuttonService.saveOrUpdate(ibutton);
            }
            validatedData.setIbutton(ibutton);
        }

        return validatedData;
    }

    private ValidatedData generateValidatedDataForAlreadySavedRecord(int index) throws ControlledTemperatusException {
        Record record = getSourceChoiceForIndex(index).getRecord();
        ValidatedData validatedData = new ValidatedData();
        validatedData.setIbutton(record.getIbutton());
        validatedData.setDeviceModel(record.getIbutton().getModel());
        validatedData.setDeviceSerial(record.getIbutton().getSerial());

        List<Measurement> measurements = null;

        try {
            IbuttonDataImporter ibuttonDataImporter = new IbuttonDataImporter(new File(record.getDataPath()));
            measurements = ibuttonDataImporter.getMeasurements();
        } catch (ControlledTemperatusException e) {
            throw new ControlledTemperatusException("");    // TODO MENSAJE!
        }

        Collections.sort(measurements, (a, b) -> a.getDate().compareTo(b.getDate()));    // sort list by date

        validatedData.setMeasurements(measurements);
        validatedData.setFinishDate(measurements.get(measurements.size() - 1).getDate());
        validatedData.setPosition(record.getPosition());
        validatedData.setSampleRate("");
        validatedData.setStartDate(measurements.get(0).getDate());
        validatedData.setUpdate(true);

        return validatedData;
    }


    /**
     * Analyze the data, get the general information, save to database and load configureMissionScreen
     */
    @FXML
    void save() {

        // check if at least one row has all the required info completed
        boolean atLeastOneComplete = false;
        for (int i = 0; i < game.getNumButtons(); i++) {
            if (getPositionForIndex(i) != null && getSourceChoiceForIndex(i) != null && (getSourceChoiceForIndex(i).getFile() != null || getSourceChoiceForIndex(i).getRecord() != null)) {
                atLeastOneComplete = true;
                break;
            }
        }

        // list containing the data validated and with the possible errors in a separated list
        List<ValidatedData> validatedDataList = new ArrayList<>();

        // general data to show on the next view in the general tab
        GeneralData generalData = new GeneralData();

        ProgressIndicator pForm = new ProgressIndicator();  // indicate the state of the task
        Task<Void> importAndValidateTask = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                try {
                    // Import and check
                    for (int index = 0; index < game.getNumButtons(); index++) {
                        updateProgress(index, game.getNumButtons() - 1);

                        if (getSourceChoiceForIndex(index) != null && getSourceChoiceForIndex(index).getRecord() != null) {    // already imported data
                            validatedDataList.add(generateValidatedDataForAlreadySavedRecord(index));
                        } else {
                            // Import data
                            AbstractImporter importedData = importIbuttonData(index);

                            if (importedData != null) {
                                // Validate data
                                ValidatedData validatedData = validateData(index, importedData);

                                // Save data
                                validatedDataList.add(validatedData);
                            }
                        }
                    }

                    // copy csv files to application folder and save Records to database
                    int index = 0;
                    for (ValidatedData validatedData : validatedDataList) {
                        if (!validatedData.isUpdate()) {

                            String path = Constants.MISSIONS_PATH + mission.getName() + File.separator + validatedData.getPosition().getPlace() + "_" + index + ".csv";

                            File dest = new File(path);
                            dest.getParentFile().mkdirs();
                            dest.createNewFile();

                            FileUtils.copyFile(validatedData.getDataFile(), dest);
                            validatedData.setDataFile(dest);

                            Record record = new Record(validatedData.getIbutton(), mission, validatedData.getPosition(), dest.getPath());
                            recordService.save(record);

                            mission.getRecords().add(record);
                            index++;
                        }
                    }

                    updateProgress(10, 10);

                } catch (Exception e) {
                    logger.error("Error saving or analyzing data: " + e.getMessage());
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, e.getMessage());
                        stackPane.getChildren().remove(stackPane.getChildren().size() - 1); // remove the progress indicator
                        anchorPane.setDisable(false);
                        mission.getRecords().clear();
                    });

                    throw new InterruptedException();
                }
                return null;
            }
        };

        // binds progress of progress bars to progress of task:
        pForm.progressProperty().bind(importAndValidateTask.progressProperty());

        importAndValidateTask.setOnSucceeded(event -> {

            //#################################################

            boolean thereAreOutliers = false;
            for (ValidatedData validatedData : validatedDataList) {
                if (validatedData.getPossibleErrors().size() > 0) {
                    thereAreOutliers = true;
                    break;
                }
            }

            if (thereAreOutliers) {
                // show the detected strange values to the user and allow to remove/edit them
                logger.info("Loading modal view outliers (show and wait): " + language.get(Lang.OUTLIERS));

                SpringFxmlLoader loader = new SpringFxmlLoader();
                Parent root = loader.load(VistaNavigator.class.getResource(Constants.OUTLIERS));

                Stage stage = new Stage(StageStyle.TRANSPARENT);
                stage.setTitle(language.get(Lang.OUTLIERS));
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.initOwner(VistaNavigator.getMainStage());

                if (VistaNavigator.getParentNode() != null) {
                    //VistaNavigator.getParentNode().setDisable(true);
                    Animation.blurOut(VistaNavigator.getParentNode());
                }

                //Animation.fadeOutIn(null, root);

                ((OutliersController) loader.getController()).setValidatedDataList(validatedDataList);
                stage.showAndWait();
            }

            //#################################################

            // compare data and obtain general information
            generalData.setMaxTemp(getMaximumTemperature(validatedDataList));
            generalData.setMinTemp(getMinimumTemperature(validatedDataList));
            generalData.setEndDate(getGlobalEndDate(validatedDataList));
            generalData.setStartDate(getGlobalStartDate(validatedDataList));
            generalData.setAvgTemp(getAverageTemperature(validatedDataList));
            generalData.setRate(getRate(validatedDataList));
            generalData.setModels(getModels(validatedDataList));
            generalData.setMeasurementsPerButton(getMeasurementsPerButton(validatedDataList));

            RecordConfigController recordConfigController = VistaNavigator.pushViewToStack(Constants.RECORD_CONFIG);
            recordConfigController.setMission(mission);
            recordConfigController.setData(validatedDataList, generalData);

            stackPane.getChildren().remove(stackPane.getChildren().size() - 1); // remove the progress indicator
            anchorPane.setDisable(false);
        });

        if (atLeastOneComplete) {
            anchorPane.setDisable(true);    // blur pane

            VBox box = new VBox(pForm); // add a progress indicator to the view
            box.setAlignment(Pos.CENTER);
            stackPane.getChildren().add(box);

            Thread thread = new Thread(importAndValidateTask);  // start task in a new thread
            thread.setDaemon(true);
            thread.start();
        } else {
            showAlert(Alert.AlertType.WARNING, language.get(Lang.MUST_SELECT_ALL_ROW));
        }
    }


    /**
     * Get the sample rate used for the experiment
     * If not all the devices where configured with the same rate show a warning because some data may not be correct.
     *
     * @param data list of devices and their information
     * @return sample rate used for the experiment
     */
    private String getRate(final List<ValidatedData> data) {
        if (data != null && data.size() > 0) {
            boolean showWarn = false;
            String rate = data.get(0).getSampleRate();

            for (ValidatedData validatedData : data) {
                if (!rate.equals("") && !rate.equals(validatedData.getSampleRate())) {
                    showWarn = true;
                }
            }

            if (showWarn) {
                Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, language.get(Lang.DIFFERENT_RATES)));
            }

            return rate;
        } else {
            return "";
        }
    }

    /**
     * Get minimum temperature of the experiment
     *
     * @param data list of devices and their information
     * @return minimum temperature of the list
     */
    private double getMinimumTemperature(final List<ValidatedData> data) {
        double minTemp = Double.MAX_VALUE;
        for (ValidatedData validatedData : data) {
            double actualTemp = IButtonDataAnalysis.getMinTemperature(validatedData.getMeasurements());
            if (minTemp > actualTemp) {
                minTemp = actualTemp;
            }
        }
        return minTemp;
    }

    /**
     * Get maximum temperature of the experiment
     *
     * @param data list of devices and their information
     * @return maximum temperature of the list
     */
    private double getMaximumTemperature(final List<ValidatedData> data) {
        double maxTemp = Double.MIN_VALUE;
        for (ValidatedData validatedData : data) {
            double actualTemp = IButtonDataAnalysis.getMaxTemperature(validatedData.getMeasurements());
            if (maxTemp < actualTemp) {
                maxTemp = actualTemp;
            }
        }
        return maxTemp;
    }

    /**
     * Get average temperature of the experiment
     *
     * @param data list of devices and their information
     * @return average temperature of the list
     */
    private double getAverageTemperature(final List<ValidatedData> data) {
        double average = 0;
        for (ValidatedData validatedData : data) {
            average = average + IButtonDataAnalysis.getAverage(validatedData.getMeasurements());
        }
        return average / data.size();
    }

    /**
     * Get average number of measurements of the experiment
     *
     * @param data list of devices and their information
     * @return average number of measurements
     */
    private int getMeasurementsPerButton(final List<ValidatedData> data) {
        int measurementsCount = 0;
        for (ValidatedData validatedData : data) {
            measurementsCount = measurementsCount + validatedData.getMeasurements().size();
        }
        return measurementsCount / data.size();
    }

    /**
     * Get all device models used to measure the data
     *
     * @param data data list
     * @return list of models (string)
     */
    private String getModels(final List<ValidatedData> data) {
        String model = "";

        for (ValidatedData validatedData : data) {
            String actualModel = validatedData.getDeviceModel();
            if (!model.contains(actualModel)) {
                model = model + actualModel;
            }
        }

        return model;
    }

    /**
     * Calculate the first date in which all devices where reading (common start date)
     *
     * @param data list of the measurements registered by all the devices
     * @return first date of register
     */
    private Date getGlobalStartDate(final List<ValidatedData> data) {
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

    /**
     * Calculate the end date in which all the devices where still reading (common end date)
     *
     * @param data list of the measurements registered by all the devices
     * @return end date of register
     */
    private Date getGlobalEndDate(final List<ValidatedData> data) {
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

    /**
     * Cancel the creation of the mission and remove the previously saved mission info
     */
    @FXML
    private void cancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION_LOSE_PROGRESS));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && ButtonType.OK == result.get()) {
            if (!isUpdate) {
                missionService.delete(mission);
            }
            VistaNavigator.loadVista(Constants.ARCHIVED);
            VistaNavigator.baseController.selectMenuButton(Constants.ARCHIVED);
            VistaNavigator.baseController.setActualBaseView(Constants.ARCHIVED);
        }
    }

    /**
     * If a new position is created add it to all the combo-boxes of positions
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Position) {
            Position newPosition = (Position) object;
            positions.add(newPosition);
        }
    }

    @Override
    public void translate() {
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
        titleLabel.setText(language.get(Lang.NEW_RECORD));
        indexLabel.setText(language.get(Lang.INDEX));
        positionLabel.setText(language.get(Lang.POSITION_COLUMN));
        dataSourceLabel.setText(language.get(Lang.DATA_SOURCE_COLUMN));
        importLabel.setText(language.get(Lang.IMPORT_COLUMN));
    }
}
