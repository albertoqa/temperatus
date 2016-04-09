package temperatus.controller.manage;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Tab formulasTab = new Tab();
    private Tab positionsTab = new Tab();
    private Tab authorsTab = new Tab();
    private Tab iButtonsTab = new Tab();

    static Logger logger = LoggerFactory.getLogger(ManageController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        tabPane.getTabs().addAll(subjectsTab, authorsTab, gamesTab, formulasTab, positionsTab, iButtonsTab);

        tabPane.getSelectionModel().clearSelection();
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue.getContent() == null) {

                    String fxml = "";

                    if (language.get(Constants.SUBJECTSPANE).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_SUBJECT;
                    } else if (language.get(Constants.GAMESPANE).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_GAME;
                    } else if (language.get(Constants.FORMULASPANE).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_FORMULA;
                    } else if (language.get(Constants.POSITIONSPANE).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_POSITION;
                    } else if (language.get(Constants.AUTHORSPANE).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_AUTHOR;
                    } else if (language.get(Constants.IBUTTONSPANE).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_IBUTTON;
                    }

                    if (fxml != "") {
                        Parent root = (Parent) VistaNavigator.loader.load(this.getClass().getResource(fxml));
                        newValue.setContent(root);
                    }
                } else {
                    Parent root = (Parent) newValue.getContent();
                }
            }
        });

        tabPane.getSelectionModel().selectFirst();
    }

    @Override
    public void translate() {
        subjectsTab.setText(language.get(Constants.SUBJECTSPANE));
        gamesTab.setText(language.get(Constants.GAMESPANE));
        formulasTab.setText(language.get(Constants.FORMULASPANE));
        positionsTab.setText(language.get(Constants.POSITIONSPANE));
        authorsTab.setText(language.get(Constants.AUTHORSPANE));
        iButtonsTab.setText(language.get(Constants.IBUTTONSPANE));
    }
}
