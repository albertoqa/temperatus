package temperatus.controller.button;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import temperatus.controller.AbstractController;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceMissionStartTask;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 19/4/16.
 */
public class StartDeviceMissionController implements Initializable, AbstractController {

    @Autowired DeviceMissionStartTask deviceMissionStartTask;   // read from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void translate() {

    }

    @FXML
    private void startMission() {

    }

}
