package temperatus.controller;

import javafx.fxml.Initializable;
import org.springframework.stereotype.Controller;
import temperatus.listener.DeviceDetectorListener;
import temperatus.util.Constants;

import java.net.URL;
import java.util.EventObject;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Controller
public class HomeController implements Initializable, AbstractController, DeviceDetectorListener {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Constants.deviceDetectorSource.addEventListener(this);
    }

    @Override
    public void translate() {

    }

    @Override
    public void deviceDetected(EventObject event) {

    }
}
