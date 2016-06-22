package temperatus.controller.button;

import com.dalsemi.onewire.container.OneWireSensor;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.Notifications;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.device.DeviceConnectedList;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceMissionStartTask;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Configuration;
import temperatus.model.pojo.types.Device;
import temperatus.model.pojo.utils.AutoCompleteComboBoxListener;
import temperatus.util.Constants;
import temperatus.util.DateUtils;
import temperatus.util.User;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Create new configurations and save them to database.
 * Apply a configuration to a device and start a mission.
 * <p>
 * Created by alberto on 19/4/16.
 */
@Controller
public class StartDeviceMissionController extends AbstractStartDeviceMissionController implements Initializable, AbstractController {

    @FXML private CheckListView<Device> deviceCheckListView;        // list of connected devices
    @FXML private ComboBox<Configuration> configurationsCombobox;   // stored configurations

    @FXML private Button configureButton;
    @FXML private Button helpButton;
    @FXML private Button saveButton;
    @FXML private Button backButton;

    @FXML private StackPane stackPane;
    @FXML private AnchorPane anchorPane;

    @FXML private Label headerLabel;
    @FXML private Label infoArea;
    @FXML private Label preloadLabel;

    @Autowired DeviceOperationsManager deviceOperationsManager;
    @Autowired DeviceConnectedList deviceConnectedList;

    private static final String DEFAULT = "Default";
    private static final int CAPACITY_LOW = 8192;
    private static final int CAPACITY_HIGH = 4096;

    private static Logger logger = LoggerFactory.getLogger(StartDeviceMissionController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        initializeViewElements();

        deviceCheckListView.setItems(deviceConnectedList.getDevices().filtered(device -> device.getContainer() instanceof OneWireSensor));
        configurationsCombobox.setItems(FXCollections.observableArrayList());

        new AutoCompleteComboBoxListener<>(configurationsCombobox);

        // if selected a pre-configuration, load it
        configurationsCombobox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.intValue() >= 0) {
                loadConfiguration(configurationsCombobox.getItems().get(newValue.intValue()));
                saveButton.setText(language.get(Lang.UPDATE));

                nameInput.textProperty().addListener((ob, old, in) -> {
                    if (!old.equals(in)) {
                        saveButton.setText(language.get(Lang.SAVE));
                    }
                });
            }
        });

        //resolutionBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateInformation());
        //dateInput.textProperty().addListener((observable, oldValue, newValue) -> updateInformation());
        //rateInput.textProperty().addListener((observable, oldValue, newValue) -> updateInformation());
        //onAlarmDelayInput.getEditor().textProperty().addListener((observable, oldValue, newValue) -> updateInformation());
        //delayInput.getEditor().textProperty().addListener((observable, oldValue, newValue) -> updateInformation());
        //startGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> updateInformation());
        infoArea.setText(Constants.EMPTY);

        getAllElements();
    }

    /**
     * Fetch all Configurations from database and add it to the combo-box.
     * Use a different thread than the UI thread.
     */
    private void getAllElements() {
        Task<List<Configuration>> getConfigurationsTask = new Task<List<Configuration>>() {
            @Override
            public List<Configuration> call() throws Exception {
                return configurationService.getAll();
            }
        };

        // on task completion add all configurations to the table
        getConfigurationsTask.setOnSucceeded(e -> {
            configurationsCombobox.getItems().addAll(getConfigurationsTask.getValue());
            loadDefaultConfiguration();     // load the default configuration
        });

        // run the task using a thread from the thread pool:
        databaseExecutor.submit(getConfigurationsTask);
    }

    /**
     * Update information about how much measurement can the device register with the current configuration
     * 2048, 4096, 8192
     */
    private void updateInformation() {
        try {
            int capacity = isResHigh() ? CAPACITY_LOW : CAPACITY_HIGH;
            LocalDateTime dateEnd = DateUtils.asLocalDateTime(getStartDate()).plusSeconds(getStart());
            infoArea.setText(language.get(Lang.WITH_CURRENT_CONF) + Constants.SPACE + capacity + Constants.SPACE + language.get(Lang.MEMORY_FULL) + Constants.SPACE + dateEnd.toString());
        } catch (Exception e) {
            logger.error("Mission configuration is not valid. " + e.getMessage());
            infoArea.setText(language.get(Lang.INVALID_CURRENT_CONF));
        }
    }

    /**
     * Pre-select a device to configure
     *
     * @param device device to configure
     */
    void selectDevice(Device device) {
        deviceCheckListView.getCheckModel().check(device);
    }

    /**
     * Save the current configuration to database
     */
    @FXML
    private void saveConfiguration() {
        try {
            logger.info("Saving configuration...");

            Configuration configuration = new Configuration();
            generateConfiguration(configuration);

            Configuration preselected = configurationsCombobox.getSelectionModel().getSelectedItem();

            if (preselected != null && saveButton.getText().equals(language.get(Lang.UPDATE))) {
                configuration.setId(preselected.getId());
            }

            configurationService.saveOrUpdate(configuration);

            showAlertAndWait(Alert.AlertType.INFORMATION, language.get(Lang.SUCCESSFULLY_SAVED));

            logger.info("Saved: " + configuration);

        } catch (ControlledTemperatusException ex) {
            logger.warn("Error in the configuration... " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            VistaNavigator.showAlert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            VistaNavigator.showAlert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
        }
    }

    /**
     * Search for the default configuration and load it on screen
     */
    private void loadDefaultConfiguration() {
        for (Configuration configuration : configurationsCombobox.getItems()) {
            if (DEFAULT.equals(configuration.getName())) {
                loadConfiguration(configuration);
                break;
            }
        }
    }

    /**
     * Generate a configuration to apply to a mission. Get all selected devices and start a mission in each
     * of them with the selected parameters.
     */
    @FXML
    private void startMission() {
        Configuration configuration = new Configuration();
        try {
            generateConfiguration(configuration);  // current configuration options
            logger.warn("Generate configuration valid  " + configuration.getDelay());
            for (Device device : deviceCheckListView.getCheckModel().getCheckedItems()) {     // apply configuration to all selected devices
                DeviceMissionStartTask deviceMissionStartTask = new DeviceMissionStartTask();   // read from device task

                deviceMissionStartTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort(), false);  // device connection data
                deviceMissionStartTask.setConfiguration(configuration);     // current configuration to apply
                ListenableFuture future = deviceOperationsManager.submitTask(deviceMissionStartTask);
                startProgressIndicator();

                logger.warn("Right now we are at history point" + configuration.getDelay());
                history.info(User.getUserName() + Constants.SPACE + language.get(Lang.START_MISSION_HISTORY) + Constants.SPACE + device.getAlias());

                Futures.addCallback(future, new FutureCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        Platform.runLater(() -> {
                            if (result) {
                                logger.info("Device configured correctly");
                                Notifications.create().title(language.get(Lang.MISSION_CONFIGURED)).text(language.get(Lang.SERIAL_COLUMN) + device.getSerial()).show();
                                stopProgressIndicator();
                            } else {
                                error(device.getSerial());
                            }
                        });
                    }

                    public void onFailure(Throwable thrown) {
                        Platform.runLater(() -> error(device.getSerial()));
                    }
                });
            }
        } catch (ControlledTemperatusException ex) {
            logger.warn("Error in the configuration... " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        }
    }

    /**
     * If fail to start mission show error message
     *
     * @param serial serial of the device where the start failed
     */
    private void error(String serial) {
        logger.error("Error starting mission on device - Future error");
        showAlert(Alert.AlertType.ERROR, language.get(Lang.ERROR_STARTING_MISSION) + serial);
        stopProgressIndicator();
    }

    /**
     * Start the progress indicator and blur the pane
     */
    private void startProgressIndicator() {
        anchorPane.setDisable(true);    // blur pane
        VBox box = new VBox(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS)); // add a progress indicator to the view
        box.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(box);
    }

    /**
     * End the progress indicator and activate the anchor pane
     */
    private void stopProgressIndicator() {
        if (stackPane.getChildren().size() > 1) {
            stackPane.getChildren().remove(stackPane.getChildren().size() - 1); // remove the progress indicator
        }
        anchorPane.setDisable(false);
    }

    /**
     * Go back to ConnectedDevices view
     */
    @FXML
    private void back() {
        VistaNavigator.loadVista(Constants.CONNECTED);
    }

    /**
     * Show a modal window with help about how to configure the mission
     */
    @FXML
    private void help() {
        VistaNavigator.openModal(Constants.MISSION_HELP, language.get(Lang.MISSION_HELP));
    }

    /**
     * If a new configuration is created reload it on the combo-box
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Configuration) {
            configurationsCombobox.getItems().add((Configuration) object);
            configurationsCombobox.getSelectionModel().select((Configuration) object);
        }
    }

    @Override
    public void translate() {
        translateCommon();
        configureButton.setText(language.get(Lang.CONFIGURE));
        saveButton.setText(language.get(Lang.SAVE));
        helpButton.setText(language.get(Lang.HELP));
        preloadLabel.setText(language.get(Lang.PRELOAD_CONFIGURATION));
        headerLabel.setText(language.get(Lang.START_DEVICE_MISSION));
        backButton.setText(language.get(Lang.BACK_BUTTON));
    }

}
