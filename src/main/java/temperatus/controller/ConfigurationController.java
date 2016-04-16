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
 * Application configuration controller - user preferences
 * <p>
 * Created by alberto on 17/1/16.
 */
@Controller
public class ConfigurationController implements Initializable, AbstractController {

    @FXML private TitledPane titledPane;
    @FXML private Label languageLabel;
    @FXML private Label unitLabel;
    @FXML private Label restartLabel;
    @FXML private ChoiceBox<String> languageChoice;
    @FXML private RadioButton cRadio;
    @FXML private RadioButton fRadio;
    @FXML private CheckBox writeAsIndexBox;
    @FXML private CheckBox autoSync;

    @FXML private Button cancelButton;
    @FXML private Button applyButton;
    @FXML private Button okButton;

    private ToggleGroup unitGroup = new ToggleGroup();

    private static Logger logger = LoggerFactory.getLogger(ConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Loading user preferences");

        translate();

        unitGroup.getToggles().add(cRadio);
        unitGroup.getToggles().add(fRadio);

        if (Constants.UNIT_C.equals(Constants.prefs.get(Constants.UNIT, Constants.UNIT_C))) {
            cRadio.setSelected(true);
        } else {
            fRadio.setSelected(true);
        }

        languageChoice.getItems().addAll(Constants.LANG_EN, Constants.LANG_SP);

        if (Constants.LANGUAGE_EN.equals(Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN))) {
            languageChoice.getSelectionModel().select(Constants.LANG_EN);
        } else {
            languageChoice.getSelectionModel().select(Constants.LANG_SP);
        }

        if (Constants.prefs.getBoolean(Constants.WRITE_AS_INDEX, Constants.WRITE_INDEX)) {
            writeAsIndexBox.setSelected(true);
        } else {
            writeAsIndexBox.setSelected(false);
        }

        if (Constants.prefs.getBoolean(Constants.AUTO_SYNC, Constants.SYNC)) {
            autoSync.setSelected(true);
        } else {
            autoSync.setSelected(false);
        }

    }

    @FXML
    private void okAction() {
        savePrefs();
        cancelAction();
    }

    @FXML
    private void applyAction() {
        savePrefs();
    }

    /**
     * Save the actual configuration of preferences
     */
    private void savePrefs() {
        logger.info("Saving user preferences");

        // Unit preference
        if (cRadio.isSelected()) {
            Constants.prefs.put(Constants.UNIT, Constants.UNIT_C);
        } else {
            Constants.prefs.put(Constants.UNIT, Constants.UNIT_F);
        }

        // Write index instead of dateTime preference
        if (writeAsIndexBox.isSelected()) {
            Constants.prefs.put(Constants.WRITE_AS_INDEX, "true");
        } else {
            Constants.prefs.put(Constants.WRITE_AS_INDEX, "false");
        }

        // Auto sync device with system time preference
        if (autoSync.isSelected()) {
            Constants.prefs.put(Constants.AUTO_SYNC, "true");
        } else {
            Constants.prefs.put(Constants.AUTO_SYNC, "false");
        }

        try {
            warnOfRestart();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        // This must be the last preference to save
        if (Constants.LANG_EN.equals(languageChoice.getSelectionModel().getSelectedItem())) {
            Constants.prefs.put(Constants.LANGUAGE, Constants.LANGUAGE_EN);
        } else if (Constants.LANG_SP.equals(languageChoice.getSelectionModel().getSelectedItem())) {
            Constants.prefs.put(Constants.LANGUAGE, Constants.LANGUAGE_SP);
        }

    }

    /**
     * Check if language preference has changed: if it changed ask the user to restart the application
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    private void warnOfRestart() throws IOException, URISyntaxException {
        boolean restartNeeded = false;

        if (Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN).equals(Constants.LANGUAGE_EN) && !languageChoice.getSelectionModel().getSelectedItem().equals(Constants.LANG_EN)) {
            restartNeeded = true;
        } else if (Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN).equals(Constants.LANGUAGE_SP) && !languageChoice.getSelectionModel().getSelectedItem().equals(Constants.LANG_SP)) {
            restartNeeded = true;
        }

        if (restartNeeded) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Constants.RESTART));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && ButtonType.OK == result.get()) {
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

    @Override
    public void translate() {
        titledPane.setText(language.get(Constants.CONFIGURATIONTITLE));
        languageLabel.setText(language.get(Constants.LANG_SELECTOR));
        unitLabel.setText(language.get(Constants.UNIT_SELECTOR));
        restartLabel.setText(language.get(Constants.RESTART_LABEL));
        writeAsIndexBox.setText(language.get(Constants.WRITE_AS_INDEX_LABEL));
        autoSync.setText(language.get(Constants.AUTO_SYNC_LABEL));
        cancelButton.setText(language.get(Constants.CANCEL));
        applyButton.setText(language.get(Constants.APPLY));
        okButton.setText(language.get(Constants.OK));
    }

    /**
     * If user change language and accept to restart the application so the change take effect
     *
     * @throws URISyntaxException
     * @throws IOException
     */
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
