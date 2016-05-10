package temperatus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.lang.Lang;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    @FXML private ChoiceBox<String> languageChoice;
    @FXML private RadioButton cRadio;
    @FXML private RadioButton fRadio;
    @FXML private CheckBox writeAsIndexBox;
    @FXML private CheckBox autoSync;
    @FXML private CheckBox updates;

    @FXML private Button cancelButton;
    @FXML private Button applyButton;
    @FXML private Button okButton;
    @FXML private Button exportButton;
    @FXML private Button importButton;

    private ToggleGroup unitGroup = new ToggleGroup();

    private static Logger logger = LoggerFactory.getLogger(ConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Loading user preferences");

        translate();

        unitGroup.getToggles().add(cRadio);
        unitGroup.getToggles().add(fRadio);

        languageChoice.getItems().addAll(Constants.LANG_EN, Constants.LANG_SP);

        if (Constants.LANGUAGE_EN.equals(Constants.prefs.get(Constants.LANGUAGE, Constants.LANGUAGE_EN))) {
            languageChoice.getSelectionModel().select(Constants.LANG_EN);
        } else {
            languageChoice.getSelectionModel().select(Constants.LANG_SP);
        }

        writeAsIndexBox.setSelected(Constants.prefs.getBoolean(Constants.WRITE_AS_INDEX, Constants.WRITE_INDEX));
        autoSync.setSelected(Constants.prefs.getBoolean(Constants.AUTO_SYNC, Constants.SYNC));
        updates.setSelected(Constants.prefs.getBoolean(Constants.UPDATE, Constants.UPD));
        cRadio.setSelected(Constants.UNIT_C.equals(Constants.prefs.get(Constants.UNIT, Constants.UNIT_C)));
        fRadio.setSelected(Constants.UNIT_F.equals(Constants.prefs.get(Constants.UNIT, Constants.UNIT_C)));
    }

    /**
     * Save preferences and close the window
     */
    @FXML
    private void okAction() {
        savePrefs();
        cancelAction();
    }

    /**
     * Save preferences but don't close the window
     */
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
        Constants.prefs.put(Constants.WRITE_AS_INDEX, String.valueOf(writeAsIndexBox.isSelected()));

        // Auto sync device with system time preference
        Constants.prefs.put(Constants.AUTO_SYNC, String.valueOf(autoSync.isSelected()));

        // Search for application updates on start
        Constants.prefs.put(Constants.UPDATE, String.valueOf(updates.isSelected()));

        // Default language
        if (Constants.LANG_EN.equals(languageChoice.getSelectionModel().getSelectedItem())) {
            Constants.prefs.put(Constants.LANGUAGE, Constants.LANGUAGE_EN);
        } else if (Constants.LANG_SP.equals(languageChoice.getSelectionModel().getSelectedItem())) {
            Constants.prefs.put(Constants.LANGUAGE, Constants.LANGUAGE_SP);
        }

        language.reloadLanguage();
        VistaNavigator.baseController.translate();
        VistaNavigator.getController().translate();
        this.translate();
    }

    @FXML
    private void exportApplicationData() {
        // Copy history, images, missions data and database
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("TemperatusBackup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TemperatusBackup (*.tb)", "*.tb"));
        File f = fileChooser.showSaveDialog(titledPane.getScene().getWindow());
        f.mkdir();

        try {
            FileUtils.copyFileToDirectory(new File(Constants.HISTORY_PATH), f);
        } catch (IOException e) {
            logger.warn("History cannot be exported");
        }
        try {
            FileUtils.copyFileToDirectory(new File(Constants.DATABASE_PATH), f);
        } catch (IOException e) {
            logger.warn("Database cannot be exported");
        }
        try {
            FileUtils.copyDirectoryToDirectory(new File(Constants.IMAGES_PATH), f);
        } catch (IOException e) {
            logger.warn("Images cannot be exported");
        }
        try {
            FileUtils.copyDirectoryToDirectory(new File(Constants.MISSIONS_PATH), f);
        } catch (IOException e) {
            logger.warn("Missions data cannot be exported");
        }
    }

    /**
     * Replace application current data with the import
     */
    @FXML
    private void importApplicationData() {
        if (VistaNavigator.confirmationAlert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION_IMPORT))) {
            try {
                // Copy history, images, missions data and database
                DirectoryChooser fileChooser = new DirectoryChooser();
                File f = fileChooser.showDialog(titledPane.getScene().getWindow());
                File destiny = new File("./");
                FileUtils.copyDirectory(f, destiny);
            } catch (IOException e) {
                logger.error("Error importing application data...");
                VistaNavigator.showAlert(Alert.AlertType.ERROR, language.get(Lang.IMPORT_ERROR));
            }
            logger.info("Application data imported correctly");
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
        titledPane.setText(language.get(Lang.CONFIGURATIONTITLE));
        languageLabel.setText(language.get(Lang.LANG_SELECTOR));
        unitLabel.setText(language.get(Lang.UNIT_SELECTOR));
        writeAsIndexBox.setText(language.get(Lang.WRITE_AS_INDEX_LABEL));
        autoSync.setText(language.get(Lang.AUTO_SYNC_LABEL));
        updates.setText(language.get(Lang.AUTO_UPDATE_LABEL));
        cancelButton.setText(language.get(Lang.CANCEL));
        applyButton.setText(language.get(Lang.APPLY));
        okButton.setText(language.get(Lang.OK));
        exportButton.setText(language.get(Lang.EXPORT));
        importButton.setText(language.get(Lang.IMPORT));
    }

}
