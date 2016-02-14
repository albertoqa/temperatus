package temperatus.controller.manage;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 14/2/16.
 */
@Controller
public class ManageController implements Initializable, AbstractController {

    @FXML private Label manageLabel;
    @FXML private TabPane tabPane;

    static Logger logger = Logger.getLogger(ManageController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void translate() {

    }
}
