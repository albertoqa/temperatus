package temperatus.controller.configuration;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
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
    @FXML private Label languageLabel;
    @FXML private Label unitLabel;
    @FXML private ChoiceBox<String> languageChoice;
    @FXML private RadioButton cRadio;
    @FXML private RadioButton fRadio;

    private ToggleGroup unitGroup = new ToggleGroup();

    static Logger logger = Logger.getLogger(ConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        unitGroup.getToggles().add(cRadio);
        unitGroup.getToggles().add(fRadio);

        String prefUnit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C);

        if(Constants.UNIT_C.equals(prefUnit)) {
            cRadio.setSelected(true);
        } else {
            fRadio.setSelected(true);
        }

        languageChoice.getItems().addAll(Constants.LANG_EN, Constants.LANG_SP);
        String prefLang = Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN);

        if(Constants.LANGUAGE_EN.equals(prefLang)) {
            languageChoice.getSelectionModel().select(Constants.LANG_EN);
        } else {
            languageChoice.getSelectionModel().select(Constants.LANG_SP);
        }
    }

    @FXML
    private void okAction() {
        savePrefs();
        Animation.fadeInOutClose(titledPane);
    }

    @FXML
    private void applyAction() {
        savePrefs();
    }

    private void savePrefs() {
        if(cRadio.isSelected()) {
            Constants.prefs.put(Constants.UNIT, Constants.UNIT_C);
        } else if (fRadio.isSelected()) {
            Constants.prefs.put(Constants.UNIT, Constants.UNIT_F);
        }

        if(Constants.LANG_EN.equals(languageChoice.getSelectionModel().getSelectedItem())) {
            Constants.prefs.put(Constants.LANGUAGE, Constants.LANGUAGE_EN);
        } else if (Constants.LANG_SP.equals(languageChoice.getSelectionModel().getSelectedItem())) {
            Constants.prefs.put(Constants.LANGUAGE, Constants.LANGUAGE_SP);
        }
    }

    /**
     * Discard changes and close the window
     */
    @FXML
    private void cancelAction() {
        Animation.fadeInOutClose(titledPane);
    }

    @FXML
    private void helpAction() {

    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.CONFIGURATIONTITLE));

    }

}
