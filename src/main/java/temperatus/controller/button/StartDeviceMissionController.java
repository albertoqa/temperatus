package temperatus.controller.button;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.controlsfx.control.CheckListView;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.device.DeviceConnectedList;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceMissionStartTask;
import temperatus.model.pojo.Configuration;
import temperatus.model.pojo.types.Device;
import temperatus.model.pojo.utils.AutoCompleteComboBoxListener;
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

    @Autowired DeviceMissionStartTask deviceMissionStartTask;   // read from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;
    @Autowired DeviceConnectedList deviceConnectedList;

    private static Logger logger = LoggerFactory.getLogger(StartDeviceMissionController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        startGroup.getToggles().addAll(immediatelyCheck, onDateCheck, onAlarmCheck, delayCheck);
        immediatelyCheck.setSelected(true);
        addListenersToStartTypes();

        resolutionBox.getItems().addAll(RESOLUTION_LOW, RESOLUTION_HIGH);
        resolutionBox.getSelectionModel().select(RESOLUTION_LOW);

        deviceCheckListView.setItems(deviceConnectedList.getDevices());
        configurationsCombobox.setItems(FXCollections.observableArrayList(configurationService.getAll()));

        loadDefaultConfiguration();

        new AutoCompleteComboBoxListener<>(configurationsCombobox);

        // if selected a pre-configuration, load it
        configurationsCombobox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.intValue() > 0) {
                loadConfiguration(configurationsCombobox.getItems().get(newValue.intValue()));
            }
        });
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

        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Duplicate entry");
            alert.show();
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unknown error.");
            alert.show();
        }
    }


    /**
     * Search for the default configuration and load it on screen
     */
    private void loadDefaultConfiguration() {
        for (Configuration configuration : configurationsCombobox.getItems()) {
            if ("Default".equals(configuration.getName())) {
                loadConfiguration(configuration);
                break;
            }
        }
    }

    @Override
    public void translate() {

    }

    @Override
    public void reload(Object object) {
        if (object instanceof Configuration) {
            configurationsCombobox.getItems().add((Configuration) object);
            configurationsCombobox.getSelectionModel().select((Configuration) object);
        }
    }

    /**
     * Generate a configuration to apply to a mission. Get all selected devices and start a mission in each
     * of them with the selected parameters.
     */
    @FXML
    private void startMission() {
        Configuration configuration = new Configuration();
        generateConfiguration(configuration);  // current configuration options
        if (isConfigurationValid(configuration)) {
            for (Device device : deviceCheckListView.getCheckModel().getCheckedItems()) {     // apply configuration to all selected devices

                deviceMissionStartTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort());  // device connection data
                deviceMissionStartTask.setConfiguration(configuration);     // current configuration to apply
                ListenableFuture future = deviceOperationsManager.submitTask(deviceMissionStartTask);

                // TODO show progress???

                Futures.addCallback(future, new FutureCallback<Boolean>() {
                    public void onSuccess(Boolean result) {
                        Platform.runLater(() -> {
                            logger.info("Device configured correctly");
                        });
                    }

                    public void onFailure(Throwable thrown) {
                        logger.error("Error starting mission on device - Future error");
                    }
                });
            }
        }
    }

    // TODO
    private boolean isConfigurationValid(Configuration configuration) {
        return true;
    }

}
