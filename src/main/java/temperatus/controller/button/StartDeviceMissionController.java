package temperatus.controller.button;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 19/4/16.
 */
@Controller
public class StartDeviceMissionController implements Initializable, AbstractController {

    @FXML CheckListView<Device> deviceCheckListView;

    @Autowired DeviceMissionStartTask deviceMissionStartTask;   // read from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;
    @Autowired DeviceConnectedList deviceConnectedList;

    private static Logger logger = LoggerFactory.getLogger(StartDeviceMissionController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deviceCheckListView.setItems(deviceConnectedList.getDevices());
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
