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

        initializeViewElements();
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
            logger.warn("Error in the configuration... " + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, ex.getMessage());
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

    @Override
    public void translate() {
        translateCommon();

        titledPane.setText(language.get(Lang.NEW_CONFIG));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
    }
}
