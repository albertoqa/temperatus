package temperatus.controller.manage;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Base controller for the manage view: it has a tab for each element of the database
 * <p>
 * Created by alberto on 14/2/16.
 */
@Controller
@Scope("prototype")
public class ManageController implements Initializable, AbstractController {

    @FXML private TabPane tabPane;

    private Tab subjectsTab = new Tab();
    private Tab gamesTab = new Tab();
    private Tab formulasTab = new Tab();
    private Tab positionsTab = new Tab();
    private Tab authorsTab = new Tab();
    private Tab iButtonsTab = new Tab();
    private Tab configurationsTab = new Tab();

    private static Logger logger = LoggerFactory.getLogger(ManageController.class.getName());

    private HashMap<Tab, AbstractController> controllers = new HashMap<>(); // store the controller of each tab

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        tabPane.getTabs().addAll(subjectsTab, authorsTab, gamesTab, formulasTab, positionsTab, iButtonsTab, configurationsTab);

        tabPane.getSelectionModel().clearSelection();
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue.getContent() == null) {
                    logger.debug("Selecting new tab... " + newValue.getText());

                    String fxml = "";

                    if (language.get(Lang.SUBJECTS_TAB).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_SUBJECT;
                    } else if (language.get(Lang.GAMES_TAB).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_GAME;
                    } else if (language.get(Lang.FORMULAS_TAB).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_FORMULA;
                    } else if (language.get(Lang.POSITIONS_TAB).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_POSITION;
                    } else if (language.get(Lang.AUTHORS_TAB).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_AUTHOR;
                    } else if (language.get(Lang.IBUTTONS_TAB).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_IBUTTON;
                    } else if (language.get(Lang.CONFIGURATIONS_TAB).equals(newValue.getText())) {
                        fxml = Constants.MANAGE_CONFIGURATIONS;
                    }

                    if (!fxml.equals("")) {
                        Parent root = VistaNavigator.loader.load(this.getClass().getResource(fxml));
                        AbstractController controller = VistaNavigator.loader.getController();
                        newValue.setContent(root);
                        controllers.put(newValue, controller);
                    }
                } else {
                    logger.debug("Setting controller for tab... " + newValue.getText());
                    // set the controller of the tab so it can be reloaded (on new object creation or update) if needed
                    VistaNavigator.setController(controllers.get(newValue));
                }
            }
        });

        tabPane.getSelectionModel().selectFirst();  // select first tab
    }

    @Override
    public void translate() {
        subjectsTab.setText(language.get(Lang.SUBJECTS_TAB));
        gamesTab.setText(language.get(Lang.GAMES_TAB));
        formulasTab.setText(language.get(Lang.FORMULAS_TAB));
        positionsTab.setText(language.get(Lang.POSITIONS_TAB));
        authorsTab.setText(language.get(Lang.AUTHORS_TAB));
        iButtonsTab.setText(language.get(Lang.IBUTTONS_TAB));
        configurationsTab.setText(language.get(Lang.CONFIGURATIONS_TAB));
    }
}
