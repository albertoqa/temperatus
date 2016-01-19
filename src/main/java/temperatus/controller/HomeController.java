package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.springframework.stereotype.Component;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Component
public class HomeController implements Initializable {

    @FXML
    private Button configuration;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void openConfiguration() {
        VistaNavigator.openModal(Constants.CONFIG, "Configuration");
    }

    @FXML
    private void archivedView() {
        VistaNavigator.loadVista(Constants.ARCHIVED);
    }

}
