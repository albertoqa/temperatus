package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceReadTask;
import temperatus.util.SpringFxmlLoader;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Controller
public class HomeController implements Initializable, AbstractController {


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addNewReadTask();
    }

    @Override
    public void translate() {

    }

    // FIXME TEST ONLY, REMEMBER TO DELETE THIS

    @Autowired DeviceOperationsManager deviceOperationsManager;

    @FXML
    private void addNewReadTask() {
        DeviceReadTask deviceReadTask = (DeviceReadTask) SpringFxmlLoader.getApplicationContext().getBean("deviceReadTask");

        deviceOperationsManager.submitTask(deviceReadTask);
    }

}
