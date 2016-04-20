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
import temperatus.model.service.ConfigurationService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 19/4/16.
 */
@Controller
public class StartDeviceMissionController implements Initializable, AbstractController {

    @FXML private CheckListView<Device> deviceCheckListView;
    @FXML private ListView<Configuration> configurationListView;

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
    @FXML private CheckBox enableClock;
    @FXML private CheckBox rollOver;

    @FXML private Spinner<Double> highAlarm;
    @FXML private Spinner<Double> lowAlarm;
    @FXML private Spinner<Integer> delayInput;

    @FXML private TextField nameInput;
    @FXML private TextField dateInput;
    @FXML private TextField rateInput;
    @FXML private TextArea observationsArea;

    @FXML private ChoiceBox resolutionBox;
    @FXML private Button configureButton;
    @FXML private Button helpButton;

    @Autowired DeviceMissionStartTask deviceMissionStartTask;   // read from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;
    @Autowired DeviceConnectedList deviceConnectedList;

    @Autowired ConfigurationService configurationService;

    private ToggleGroup startGroup = new ToggleGroup();

    private static Logger logger = LoggerFactory.getLogger(StartDeviceMissionController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        startGroup.getToggles().addAll(immediatelyCheck, onDateCheck, onAlarmCheck, delayCheck);

        deviceCheckListView.setItems(deviceConnectedList.getDevices());
        configurationListView.setItems(FXCollections.observableArrayList(configurationService.getAll()));

        loadDefaultConfiguration();
    }

    private Configuration generateConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setName(nameInput.getText());
        configuration.setSyncTime(syncTime.isSelected());
        configuration.setRollover(rollOver.isSelected());

        return configuration;
    }

    @FXML
    private void saveConfiguration() {
        Configuration configuration = generateConfiguration();

        if(configuration != null) {
            configurationService.saveOrUpdate(configuration);
        }
    }

    private void loadDefaultConfiguration() {
        for(Configuration configuration: configurationListView.getItems()) {
            if("Default".equals(configuration.getName())) {
                nameInput.setText(configuration.getName());
                rateInput.setText(String.valueOf(configuration.getRate()));

                if(configuration.isSyncTime()) {
                    syncTime.setSelected(true);
                } else {
                    syncTime.setSelected(false);
                }

                if(configuration.isRollover()) {
                    rollOver.setSelected(true);
                } else {
                    rollOver.setSelected(false);
                }

                if(configuration.isSuta()) {
                    // TODO
                }

                //observationsArea.setText(configuration.getObservations()); // TODO add observations to database

                break;
            }
        }
    }

    @Override
    public void translate() {

    }

    @FXML
    private void startMission() {
        Configuration configuration = new Configuration();
        // TODO set

        deviceMissionStartTask.setConfiguration(configuration);
        ListenableFuture future = deviceOperationsManager.submitTask(deviceMissionStartTask);

        // TODO show progress???


        Futures.addCallback(future, new FutureCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                Platform.runLater(() -> {


                });
            }

            public void onFailure(Throwable thrown) {
                logger.error("Error starting mission on device - Future error");
            }
        });

    }

}
