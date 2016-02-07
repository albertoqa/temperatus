package temperatus.controller.configuration;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Controller
public class ConfigurationController implements Initializable, AbstractController {

    @FXML private TitledPane titledPane;
    @FXML private StackPane selectedView;

    @FXML private Accordion accordionMenu;

    @FXML private ListView<String> menuListGeneral;
    @FXML private ListView<String> menuListGraphics;
    @FXML private ListView<String> menuListFormulas;
    @FXML private ListView<String> menuListGames;
    @FXML private ListView<String> menuListPositions;
    @FXML private ListView<String> menuListSubjects;

    @FXML private TitledPane paneGeneral;
    @FXML private TitledPane paneGraphics;
    @FXML private TitledPane paneFormulas;
    @FXML private TitledPane paneGames;
    @FXML private TitledPane panePositions;
    @FXML private TitledPane paneSubjects;

    @FXML private Label nothingSelectedLabel;

    static Logger logger = Logger.getLogger(ConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeMenus();
    }

    /**
     * Discard changes and close the window
     */
    @FXML
    private void cancelAction() {
        Animation.fadeInOutClose(titledPane);
    }

    /**
     * Initialize all menus with the preferred language
     */
    private void initializeMenus() {
        initializeGeneralMenu();
        initializeGraphicsMenu();
        initializeFormulasMenu();
        initializeGamesMenu();
        initializePositionsMenu();
        initializeSubjectsMenu();
    }

    /**
     * Set general menu labels/buttons
     */
    private void initializeGeneralMenu() {
        menuListGeneral.getItems().add(language.get(Constants.GENERAL));
        menuListGeneral.getItems().add(language.get(Constants.IMPORTEXPORT));
        menuListGeneral.getItems().add(language.get(Constants.DEFAULTS));
    }

    /**
     * Actions to perform when a element of the General menu is pressed
     * @param event
     */
    @FXML
    private void generalMenuActions(MouseEvent event) {
        switch (menuListGeneral.getSelectionModel().getSelectedIndex()) {
            case 0: {
                VistaNavigator.setViewInStackPane(selectedView, Constants.CONFIG_GENERAL);
            }
            break;
            case 1: {
                VistaNavigator.setViewInStackPane(selectedView, Constants.CONFIG_IMPORTEXPORT);
            }
            break;
            case 2: {
                VistaNavigator.setViewInStackPane(selectedView, Constants.CONFIG_DEFAULTS);
            }
            break;
            default:
                break;
        }
    }

    private void initializeGraphicsMenu() {

    }

    private void initializeFormulasMenu() {

    }

    private void initializeGamesMenu() {

    }

    private void initializePositionsMenu() {

    }

    private void initializeSubjectsMenu() {

    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.CONFIGURATIONTITLE));
        paneGeneral.setText(language.get(Constants.GENERALPANE));
        paneFormulas.setText(language.get(Constants.FORMULASPANE));
        paneGraphics.setText(language.get(Constants.GRAPHICSPANE));
        paneGames.setText(language.get(Constants.GAMESPANE));
        panePositions.setText(language.get(Constants.POSITIONSPANE));
        paneSubjects.setText(language.get(Constants.SUBJECTSPANE));
    }

}
