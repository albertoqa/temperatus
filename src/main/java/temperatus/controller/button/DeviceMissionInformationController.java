package temperatus.controller.button;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.pojo.DeviceMissionData;
import temperatus.calculator.Calculator;
import temperatus.controller.AbstractController;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceMissionDisableTask;
import temperatus.device.task.DeviceReadTask;
import temperatus.lang.Lang;
import temperatus.model.pojo.types.Device;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;
import temperatus.util.User;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Show information related to the mission configured in the selected device
 * Allow to start a new mission and disable a currently running mission
 * <p>
 * Created by alberto on 24/4/16.
 */
@Controller
@Scope("prototype")
public class DeviceMissionInformationController implements Initializable, AbstractController {

    @FXML private StackPane stackPane;
    @FXML private AnchorPane anchorPane;

    @FXML private Label missionInProgress;
    @FXML private Label sutaMission;
    @FXML private Label wfta;
    @FXML private Label sampleRate;
    @FXML private Label missionStartTime;
    @FXML private Label rollOver;
    @FXML private Label totalMissionSamples;
    @FXML private Label totalDeviceSamples;
    @FXML private Label resolution;
    @FXML private Label highAlarm;
    @FXML private Label lowAlarm;
    @FXML private Label missionSampleCount;
    @FXML private Label firstSampleTime;

    @FXML private Label missionInProgressLabel;
    @FXML private Label sutaMissionLabel;
    @FXML private Label wftaLabel;
    @FXML private Label sampleRateLabel;
    @FXML private Label missionStartTimeLabel;
    @FXML private Label rollOverLabel;
    @FXML private Label totalMissionSamplesLabel;
    @FXML private Label totalDeviceSamplesLabel;
    @FXML private Label resolutionLabel;
    @FXML private Label highAlarmLabel;
    @FXML private Label lowAlarmLabel;
    @FXML private Label missionSampleCountLabel;
    @FXML private Label firstSampleTimeLabel;

    @FXML private Button disableMissionButton;
    @FXML private Button startMissionButton;
    @FXML private Button temperatureLogButton;

    private DeviceMissionDisableTask deviceMissionDisableTask = new DeviceMissionDisableTask();   // read from device task
    private DeviceReadTask deviceReadTask = new DeviceReadTask();   // read from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;

    private Device device;
    private DeviceMissionData deviceMissionData;

    private static Logger logger = LoggerFactory.getLogger(DeviceMissionInformationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
        device = null;
    }

    /**
     * Set the selected device to read mission info from
     *
     * @param device device to read from
     */
    void setDevice(Device device) {
        this.device = device;
        readMissionInfo();
    }

    /**
     * Read the information of the mission from the device and load it in the view
     */
    private void readMissionInfo() {
        startProgressIndicator();
        deviceReadTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort(), false);  // device connection data
        ListenableFuture future = deviceOperationsManager.submitTask(deviceReadTask);

        Futures.addCallback(future, new FutureCallback<Object>() {
            public void onSuccess(Object result) {
                Platform.runLater(() -> {
                    stopProgressIndicator();
                    deviceMissionData = (DeviceMissionData) result;
                    loadMissionData(true);
                    logger.info("Device read correctly");
                });
            }

            public void onFailure(Throwable thrown) {
                Platform.runLater(() -> {
                    stopProgressIndicator();
                    loadMissionData(false);
                    logger.error("Error reading mission info from device - Future error:  " + thrown.getMessage());
                });
            }
        });
    }

    /**
     * Show the mission data on the screen
     *
     * @param success was the operation of read data success?
     */
    private void loadMissionData(boolean success) {
        if (success) {
            missionInProgress.setText(deviceMissionData.getInProgress());
            sutaMission.setText(deviceMissionData.getIsSuta());
            wfta.setText(deviceMissionData.getWaitingForTempAlarm());
            sampleRate.setText(deviceMissionData.getSampleRate());
            missionStartTime.setText(deviceMissionData.getMissionStartTime());
            rollOver.setText(deviceMissionData.getRollOverEnabled());
            totalMissionSamples.setText(deviceMissionData.getTotalMissionSamples());
            totalDeviceSamples.setText(deviceMissionData.getTotalDeviceSamples());
            resolution.setText(deviceMissionData.getResolution());

            // Show the data using the preferred unit
            Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;

            if (Unit.C.equals(unit)) {
                highAlarm.setText(deviceMissionData.getHighAlarm());
                lowAlarm.setText(deviceMissionData.getLowAlarm());
            } else {
                highAlarm.setText(String.valueOf(Calculator.celsiusToFahrenheit(Double.valueOf(deviceMissionData.getHighAlarm()))));
                lowAlarm.setText(String.valueOf(Calculator.celsiusToFahrenheit(Double.valueOf(deviceMissionData.getLowAlarm()))));
            }

            missionSampleCount.setText(deviceMissionData.getMissionSampleCount());
            firstSampleTime.setText(deviceMissionData.getFirstSampleTime());
        } else {
            showAlert(Alert.AlertType.ERROR, language.get(Lang.READING_DEVICE_ERROR));
        }
    }

    /**
     * Open a modal window showing all measurements read by the device in a graphic
     */
    @FXML
    private void showTemperatureData() {
        if (deviceMissionData.getMeasurements() != null) {
            TemperatureLogController temperatureLogController = VistaNavigator.openModal(Constants.TEMPERATURE_LOG, Constants.EMPTY);
            temperatureLogController.setData(deviceMissionData.getMeasurements(), device.getSerial(), device.getDefaultPosition());
        }
    }

    /**
     * Open the view to configure a new mission on the device
     */
    @FXML
    private void configureIbutton() {
        StartDeviceMissionController startDeviceMissionController = VistaNavigator.loadVista(Constants.CONFIG_DEVICE);
        assert startDeviceMissionController != null;
        startDeviceMissionController.selectDevice(device);
    }

    /**
     * Stop a mission on the device (if currently running)
     */
    @FXML
    private void stopDeviceMission() {
        if (device != null) {
            startProgressIndicator();
            deviceMissionDisableTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort(), false);  // device connection data
            ListenableFuture future = deviceOperationsManager.submitTask(deviceMissionDisableTask);

            history.info(User.getUserName() + Constants.SPACE + language.get(Lang.STOP_MISSION_HISTORY) + Constants.SPACE + device.getAlias());

            Futures.addCallback(future, new FutureCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    Platform.runLater(() -> {
                        stopProgressIndicator();
                        missionInProgress.setText(language.get(Lang.FALSE));
                        logger.info("Device's mission stopped correctly");
                    });
                }

                public void onFailure(Throwable thrown) {
                    Platform.runLater(() -> {
                        stopProgressIndicator();
                        showAlert(Alert.AlertType.ERROR, language.get(Lang.ERROR_STOPPING_MISSION));
                        logger.error("Error starting mission on device - Future error");
                    });
                }
            });
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
        if (stackPane.getChildren().size() > 1) {
            stackPane.getChildren().remove(stackPane.getChildren().size() - 1); // remove the progress indicator
        }
        anchorPane.setDisable(false);
    }

    @Override
    public void translate() {
        missionInProgressLabel.setText(language.get(Lang.MISSION_IN_PROGRESS));
        sutaMissionLabel.setText(language.get(Lang.SUTA_LABEL));
        wftaLabel.setText(language.get(Lang.WFTA));
        sampleRateLabel.setText(language.get(Lang.RATE_LABEL));
        missionStartTimeLabel.setText(language.get(Lang.START_DATE_LABEL));
        rollOverLabel.setText(language.get(Lang.ROLL_OVER_LABEL));
        totalMissionSamplesLabel.setText(language.get(Lang.TOTAL_MISSION_SAMPLES_LABEL));
        totalDeviceSamplesLabel.setText(language.get(Lang.TOTAL_DEVICE_SAMPLES_LABEL));
        resolutionLabel.setText(language.get(Lang.RESOLUTION_LABEL));
        highAlarmLabel.setText(language.get(Lang.HIGH_ALARM_LABEL));
        lowAlarmLabel.setText(language.get(Lang.LOW_ALARM_LABEL));
        missionSampleCountLabel.setText(language.get(Lang.MISSION_SAMPLE_COUNT_LABEL));
        firstSampleTimeLabel.setText(language.get(Lang.FIRST_SAMPLE_TIME_LABEL));

        disableMissionButton.setText(language.get(Lang.DISABLE_MISSION));
        startMissionButton.setText(language.get(Lang.START_DEVICE_MISSION));
        temperatureLogButton.setText(language.get(Lang.SAVE_DATA));
    }
}
