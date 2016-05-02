package temperatus.controller.button;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import temperatus.util.VistaNavigator;

import java.net.URL;
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

    @FXML private StackPane stackPane;
    @FXML private AnchorPane anchorPane;

    @FXML private Label headerLabel;
    @FXML private Label infoArea;
    @FXML private Label preloadLabel;

    @Autowired DeviceMissionStartTask deviceMissionStartTask;   // read from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;
    @Autowired DeviceConnectedList deviceConnectedList;

    private static final String DEFAULT = "Default";
    private static Logger logger = LoggerFactory.getLogger(StartDeviceMissionController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        initializeViewElements();

        deviceCheckListView.setItems(deviceConnectedList.getDevices());
        configurationsCombobox.setItems(FXCollections.observableArrayList(configurationService.getAll()));

        loadDefaultConfiguration();     // load the default configuration

        new AutoCompleteComboBoxListener<>(configurationsCombobox);

        // if selected a pre-configuration, load it
        configurationsCombobox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.intValue() > 0) {
                loadConfiguration(configurationsCombobox.getItems().get(newValue.intValue()));
            }
        });

        infoArea.setText("");
    }

    /**
     * Update information about how much measurement can the device register with the current configuration
     */
    private void updateInformation() {
        // TODO
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
            configurationService.saveOrUpdate(configuration);

            logger.info("Saved: " + configuration);

        } catch (ControlledTemperatusException ex) {
            logger.warn("Error in the configuration... " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            Alert alert = new Alert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
            alert.show();
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
            alert.show();
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
            for (Device device : deviceCheckListView.getCheckModel().getCheckedItems()) {     // apply configuration to all selected devices

                deviceMissionStartTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort(), false);  // device connection data
                deviceMissionStartTask.setConfiguration(configuration);     // current configuration to apply
                ListenableFuture future = deviceOperationsManager.submitTask(deviceMissionStartTask);
                startProgressIndicator();

                Futures.addCallback(future, new FutureCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        Platform.runLater(() -> {
                            logger.info("Device configured correctly");
                            Notifications.create().title(language.get(Lang.MISSION_CONFIGURED)).text(language.get(Lang.SERIAL_COLUMN) + device.getSerial()).show();
                            stopProgressIndicator();
                        });
                    }

                    public void onFailure(Throwable thrown) {
                        logger.error("Error starting mission on device - Future error");
                        showAlert(Alert.AlertType.ERROR, language.get(Lang.ERROR_STARTING_MISSION) + device.getSerial());
                        stopProgressIndicator();
                    }
                });
            }
        } catch (ControlledTemperatusException ex) {
            logger.warn("Error in the configuration... " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
        }
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
        if(stackPane.getChildren().size() > 1) {
            stackPane.getChildren().remove(stackPane.getChildren().size() - 1); // remove the progress indicator
        }
        anchorPane.setDisable(false);
    }

    /**
     * Show a modal window with help about how to configure the mission
     */
    @FXML
    private void help() {
        VistaNavigator.openModal(Constants.MISSION_HELP, language.get(Lang.MISSION_HELP));
    }

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
    }

}
