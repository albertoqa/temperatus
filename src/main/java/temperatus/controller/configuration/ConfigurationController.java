package temperatus.controller.configuration;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.types.ConfigurationMenuElement;
import temperatus.util.Animation;
import temperatus.util.Constants;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 17/1/16.
 */
@Controller
public class ConfigurationController implements Initializable, AbstractController {

    @FXML private TitledPane titledPane;
    @FXML private StackPane selectedView;
    @FXML private TreeView<ConfigurationMenuElement> treeSelector;

    @FXML private Label nothingSelectedLabel;

    static Logger logger = Logger.getLogger(ConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeMenu();
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
    private void initializeMenu() {

        TreeItem<ConfigurationMenuElement> root = new TreeItem<>(new ConfigurationMenuElement("root", ""));


        TreeItem<ConfigurationMenuElement> general = new TreeItem<>(new ConfigurationMenuElement("Titulo", "a donde va"));

        root.getChildren().add(general);



        // Subject
        TreeItem<ConfigurationMenuElement> subject = new TreeItem<>(new ConfigurationMenuElement(language.get(Constants.SUBJECTSPANE), Constants.SUBJECT_CONFIG));



        root.setExpanded(true);
        treeSelector.setRoot(root);
        treeSelector.setShowRoot(false);
    }




    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.CONFIGURATIONTITLE));

    }

}
