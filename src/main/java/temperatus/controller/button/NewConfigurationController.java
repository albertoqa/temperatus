package temperatus.controller.button;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Configuration;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * View to create and save/update a new configuration
 * <p>
 * Created by alberto on 23/4/16.
 */
@Controller
@Scope("prototype")
public class NewConfigurationController extends AbstractStartDeviceMissionController implements Initializable, AbstractController {

    @FXML private TitledPane titledPane;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Configuration configuration;

    private static Logger logger = LoggerFactory.getLogger(NewConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configuration = null;
        translate();

        startGroup.getToggles().addAll(immediatelyCheck, onDateCheck, onAlarmCheck, delayCheck);
        immediatelyCheck.setSelected(true);
        addListenersToStartTypes();

        resolutionBox.getItems().addAll(RESOLUTION_LOW, RESOLUTION_HIGH);
        resolutionBox.getSelectionModel().select(RESOLUTION_LOW);
    }

    /**
     * When editing a configuration, pre-load its data
     *
     * @param configuration configuration to update/edit
     */
    public void setConfigurationForUpdate(Configuration configuration) {
        saveButton.setText(language.get(Lang.UPDATE));  // change save button text to update
        this.configuration = configuration;
        loadConfiguration(configuration);
    }

    /**
     * Save or update a configuration on the DB
     */
    @FXML
    void save() {
        try {
            logger.info("Saving configuration...");

            if (configuration == null) {   // creation of new configuration - no update
                configuration = new Configuration();
            }

            generateConfiguration(configuration);
            configurationService.saveOrUpdate(configuration);

            VistaNavigator.closeModal(titledPane);
            if (VistaNavigator.getController() != null) {
                // Only necessary if base view needs to know about the new configuration creation
                VistaNavigator.getController().reload(configuration);
            }

            logger.info("Saved: " + configuration);

        } catch (ControlledTemperatusException ex) {
            logger.warn("Invalid name? --- " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, language.get(Lang.INVALID_NAME));
        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, language.get(Lang.DUPLICATE_ENTRY));
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, language.get(Lang.UNKNOWN_ERROR));
        }
    }

    /**
     * Cancel the creation/update and close the modal window
     */
    @FXML
    private void cancel() {
        VistaNavigator.closeModal(titledPane);
        VistaNavigator.baseController.selectBase();
    }

    /**
     * Show a new alert to the user
     *
     * @param alertType type of alert
     * @param message   message to show
     */
    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message);
        alert.show();
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Lang.NEW_CONFIG));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));

        nameInput.setPromptText(language.get(Lang.NAMEPROMPT));
        nameLabel.setText(language.get(Lang.NAMELABEL));

        rateLabel.setText(language.get(Lang.RATE_LABEL));
        resolutionLabel.setText(language.get(Lang.RESOLUTION_LABEL));
        startLabel.setText(language.get(Lang.STARTDATELABEL));
        highLabel.setText(language.get(Lang.HIGH_ALARM_LABEL));
        lowLabel.setText(language.get(Lang.LOW_ALARM_LABEL));
        alarmLabel.setText(language.get(Lang.SET_ALARM_LABEL));
        observationsLabel.setText(language.get(Lang.OBSERVATIONSLABEL));

        immediatelyCheck.setText(language.get(Lang.IMMEDIATELY));
        onDateCheck.setText(language.get(Lang.ON_DATE));
        onAlarmCheck.setText(language.get(Lang.ON_ALARM));
        delayCheck.setText(language.get(Lang.ON_DELAY));

        syncTime.setText(language.get(Lang.SYNC_CHECK));
        rollOver.setText(language.get(Lang.ROLL_OVER_CHECK));
        activateAlarmCheck.setText(language.get(Lang.SET_ALARM_CHECK));

        rateInput.setPromptText(language.get(Lang.RATE_PROMPT));
        observationsArea.setPromptText(language.get(Lang.OBSERVATIONSPROMPT));
    }
}
