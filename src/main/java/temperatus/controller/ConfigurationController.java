package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
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

    static Logger logger = LoggerFactory.getLogger(ConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        unitGroup.getToggles().add(cRadio);
        unitGroup.getToggles().add(fRadio);

        String prefUnit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C);

        if (Constants.UNIT_C.equals(prefUnit)) {
            cRadio.setSelected(true);
        } else {
            fRadio.setSelected(true);
        }

        languageChoice.getItems().addAll(Constants.LANG_EN, Constants.LANG_SP);
        String prefLang = Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN);

        if (Constants.LANGUAGE_EN.equals(prefLang)) {
            languageChoice.getSelectionModel().select(Constants.LANG_EN);
        } else {
            languageChoice.getSelectionModel().select(Constants.LANG_SP);
        }
    }

    @FXML
    private void okAction() {
        savePrefs();
        VistaNavigator.closeModal(titledPane);
        VistaNavigator.baseController.selectBase();
    }

    @FXML
    private void applyAction() {
        savePrefs();
    }

    /**
     * Save the actual configuration of preferences
     */
    private void savePrefs() {
        if (cRadio.isSelected()) {
            Constants.prefs.put(Constants.UNIT, Constants.UNIT_C);
        } else if (fRadio.isSelected()) {
            Constants.prefs.put(Constants.UNIT, Constants.UNIT_F);
        }


        try {
            warnOfRestart();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // This must be the last preference to save
        if (Constants.LANG_EN.equals(languageChoice.getSelectionModel().getSelectedItem())) {
            Constants.prefs.put(Constants.LANGUAGE, Constants.LANGUAGE_EN);
        } else if (Constants.LANG_SP.equals(languageChoice.getSelectionModel().getSelectedItem())) {
            Constants.prefs.put(Constants.LANGUAGE, Constants.LANGUAGE_SP);
        }

        //language.loadLanguage();
    }

    private void warnOfRestart() throws IOException, URISyntaxException {
        boolean restartNeeded = false;

        if(Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN).equals(Constants.LANGUAGE_EN) && !languageChoice.getSelectionModel().getSelectedItem().equals(Constants.LANG_EN)) {
            restartNeeded = true;
        } else if(Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN).equals(Constants.LANGUAGE_SP) && !languageChoice.getSelectionModel().getSelectedItem().equals(Constants.LANG_SP)) {
            restartNeeded = true;
        }

        if(restartNeeded) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Application must restart to apply this changes");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                restartApplication();
            }
        }
    }

    /**
     * Discard changes and close the window
     */
    @FXML
    private void cancelAction() {
        VistaNavigator.closeModal(titledPane);
        VistaNavigator.baseController.selectBase();
    }

    /**
     * Show the user's manual and/or the web page
     */
    @FXML
    private void helpAction() {
        // TODO
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.CONFIGURATIONTITLE));

    }

    private void restartApplication() throws URISyntaxException, IOException {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(ConfigurationController.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        /* is it a jar file? */
        if (!currentJar.getName().endsWith(".jar"))
            return;

        /* Build command: java -jar application.jar */
        final ArrayList<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentJar.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }

}
