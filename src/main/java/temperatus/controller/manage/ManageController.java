package temperatus.controller.manage;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 14/2/16.
 */
@Controller
@Scope("prototype")
public class ManageController implements Initializable, AbstractController {

    @FXML private Label manageLabel;
    @FXML private TabPane tabPane;

    private Tab subjectsTab = new Tab();
    private Tab gamesTab = new Tab();

    static Logger logger = Logger.getLogger(ManageController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        subjectsTab.setText(language.get(Constants.SUBJECTSPANE));
        VistaNavigator.loadViewInTab(subjectsTab, Constants.MANAGE_SUBJECT);

        tabPane.getTabs().addAll(subjectsTab, gamesTab);
        tabPane.getSelectionModel().select(0);
    }

    @Override
    public void translate() {

    }
}
