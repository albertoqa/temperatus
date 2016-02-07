package temperatus.controller;

import javafx.fxml.Initializable;
import org.springframework.stereotype.Controller;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Controller
public class HomeController implements Initializable, AbstractController {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.openModal(Constants.NEW_POSITION, "N");
    }

    @Override
    public void translate() {

    }
}
