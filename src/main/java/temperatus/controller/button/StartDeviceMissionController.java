package temperatus.controller.button;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
import temperatus.model.service.ConfigurationService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Create new configurations and save them to database.
 * Apply a configuration to a device and start a mission.
 * <p>
 * Created by alberto on 19/4/16.
 */
@Controller
public class StartDeviceMissionController implements Initializable, AbstractController {

    @FXML private CheckListView<Device> deviceCheckListView;        // list of connected devices
    @FXML private ComboBox<Configuration> configurationsCombobox;   // stored configurations

    @FXML private Label nameLabel;
    @FXML private Label rateLabel;
    @FXML private Label resolutionLabel;
    @FXML private Label startLabel;
    @FXML private Label highLabel;
    @FXML private Label lowLabel;
    @FXML private Label alarmLabel;

    @FXML private RadioButton immediatelyCheck;
    @FXML private RadioButton onDateCheck;
    @FXML private RadioButton onAlarmCheck;
    @FXML private RadioButton delayCheck;

    @FXML private CheckBox syncTime;
    @FXML private CheckBox rollOver;
    @FXML private CheckBox activateAlarmCheck;

    @FXML private Spinner<Double> highAlarm;
    @FXML private Spinner<Double> lowAlarm;
    @FXML private Spinner<Integer> delayInput;
    @FXML private Spinner<Integer> onAlarmDelayInput;

    @FXML private TextField nameInput;
    @FXML private TextField dateInput;
    @FXML private TextField rateInput;
    @FXML private TextArea observationsArea;

    @FXML private ChoiceBox<String> resolutionBox;
    @FXML private Button configureButton;
    @FXML private Button helpButton;

    @Autowired DeviceMissionStartTask deviceMissionStartTask;   // read from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;
    @Autowired DeviceConnectedList deviceConnectedList;

    @Autowired ConfigurationService configurationService;

    private ToggleGroup startGroup = new ToggleGroup();

    private static Logger logger = LoggerFactory.getLogger(StartDeviceMissionController.class.getName());

    private static final String RESOLUTION_LOW = "0.5 (low)";
    private static final String RESOLUTION_HIGH = "0.065 (high)";
    private static final double RES_LOW = 0.5;
    private static final double RES_HIGH = 0.065;

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
     * Depending on the type of start, the user will be required to input different values
     */
    private void addListenersToStartTypes() {
        onDateCheck.selectedProperty().addListener((observable, oldValue, newValue) -> dateInput.setVisible(newValue));
        onAlarmCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onAlarmDelayInput.setVisible(newValue));
        delayCheck.selectedProperty().addListener((observable, oldValue, newValue) -> delayInput.setVisible(newValue));
    }

    /**
     * Generate a configuration object with the values obtained from the user input
     *
     * @return generated configuration object
     */
    private Configuration generateConfiguration() {
        Configuration configuration = new Configuration();

        configuration.setName(nameInput.getText());
        configuration.setSyncTime(syncTime.isSelected());
        configuration.setRollover(rollOver.isSelected());
        configuration.setDelay(getStart());
        configuration.setRate(Integer.valueOf(rateInput.getText()));
        configuration.setSuta(onAlarmCheck.isSelected());

        configuration.setChannelEnabledC1(true);
        configuration.setChannelEnabledC2(false);
        configuration.setResolutionC1(resolutionBox.getSelectionModel().getSelectedItem().equals(RESOLUTION_LOW) ? RES_LOW : RES_HIGH);
        if (activateAlarmCheck.isSelected()) {
            configuration.setHighAlarmC1(highAlarm.getValue());
            configuration.setLowAlarmC1(lowAlarm.getValue());
            configuration.setEnableAlarmC1(true);
        } else {
            configuration.setEnableAlarmC1(false);
        }

        configuration.setObservations(observationsArea.getText());

        return configuration;
    }

    /**
     * Save the current configuration to database
     */
    @FXML
    private void saveConfiguration() {
        try {
            logger.info("Saving configuration...");

            Configuration configuration = generateConfiguration();
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
     * Load configuration data on the view
     *
     * @param configuration to be loaded
     */
    private void loadConfiguration(Configuration configuration) {
        nameInput.setText(configuration.getName());
        rateInput.setText(String.valueOf(configuration.getRate()));
        observationsArea.setText(configuration.getObservations());

        if (configuration.isSyncTime()) {
            syncTime.setSelected(true);
        } else {
            syncTime.setSelected(false);
        }

        if (configuration.isRollover()) {
            rollOver.setSelected(true);
        } else {
            rollOver.setSelected(false);
        }

        if (configuration.isSuta()) {
            onAlarmCheck.setSelected(true);
        } else {
            onAlarmCheck.setSelected(false);
        }

        delayInput.getEditor().setText(String.valueOf(configuration.getDelay()));
        onAlarmDelayInput.getEditor().setText(String.valueOf(configuration.getDelay()));

        if (configuration.getResolutionC1() == RES_LOW) {
            resolutionBox.getSelectionModel().select(RESOLUTION_LOW);
        } else {
            resolutionBox.getSelectionModel().select(RESOLUTION_HIGH);
        }

        if (configuration.getEnableAlarmC1()) {
            activateAlarmCheck.setSelected(true);
            highAlarm.getEditor().setText(String.valueOf(configuration.getHighAlarmC1()));
            lowAlarm.getEditor().setText(String.valueOf(configuration.getLowAlarmC1()));
        } else {
            activateAlarmCheck.setSelected(false);
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

    private int getStart() {
        if (immediatelyCheck.isSelected()) {
            return 0;
        } else if (delayCheck.isSelected()) {
            return Integer.valueOf(delayInput.getEditor().getText());
        } else if (onDateCheck.isSelected()) {
            return calculateDateDelay(dateInput.getText());
        } else {
            return onAlarmDelayInput.getValue();
        }
    }

    private int calculateDateDelay(String d) {
        try {
            return (int) (Constants.dateTimeFormat.parse(d).getTime() - new Date().getTime()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0; //TODO
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
        Configuration configuration = generateConfiguration();  // current configuration options
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
