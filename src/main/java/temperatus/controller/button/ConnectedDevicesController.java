package temperatus.controller.button;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.stereotype.Component;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 23/1/16.
 */
@Component
public class ConnectedDevicesController implements Initializable {


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void newMission() {
        VistaNavigator.loadVista(Constants.NEW_MISSION);
    }
}
