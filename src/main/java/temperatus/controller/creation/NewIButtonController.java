package temperatus.controller.creation;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.device.DeviceConnectedList;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.Position;
import temperatus.model.pojo.utils.AutoCompleteComboBoxListener;
import temperatus.model.service.IbuttonService;
import temperatus.model.service.PositionService;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * View to create/update a iButton
 * <p>
 * Created by alberto on 13/2/16.
 */
@Controller
@Scope("prototype")
public class NewIButtonController extends AbstractCreationController implements Initializable {

    @FXML private Label serialLabel;
    @FXML private Label modelLabel;
    @FXML private Label defaultPosLabel;
    @FXML private Label aliasLabel;

    @FXML private Label serial;
    @FXML private Label model;
    @FXML private TextField alias;
    @FXML private ComboBox<Position> position;

    @Autowired PositionService positionService;
    @Autowired IbuttonService ibuttonService;
    @Autowired DeviceConnectedList deviceConnectedList;

    private Ibutton ibutton;

    private static Logger logger = LoggerFactory.getLogger(NewIButtonController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
        ibutton = null;

        // * Load all positions from database and allow the user to choose them
        new AutoCompleteComboBoxListener<>(position);
        getAllElements();
    }

    /**
     * Fetch all Positions from database and add it to the table.
     * Use a different thread than the UI thread.
     */
    private void getAllElements() {
        Task<List<Position>> getPositionsTask = new Task<List<Position>>() {
            @Override
            public List<Position> call() throws Exception {
                return positionService.getAll();
            }
        };

        // on task completion add all positions to the table
        getPositionsTask.setOnSucceeded(e -> position.getItems().addAll(getPositionsTask.getValue()));

        // run the task using a thread from the thread pool:
        databaseExecutor.submit(getPositionsTask);
    }

    /**
     * Serial and model cannot be modified by the user. Set them from the device information
     *
     * @param serial    device serial
     * @param model     device model
     * @param isAdapter if adapter true don't allow to choose a position
     */
    public void setData(String serial, String model, boolean isAdapter) {
        this.serial.setText(serial);
        this.model.setText(model);

        if (isAdapter) {
            position.setDisable(true);
        }
    }

    /**
     * Save or update a iButton
     */
    @Override
    @FXML
    void save() {
        try {
            logger.info("Saving iButton...");

            if (ibutton == null) {
                ibutton = new Ibutton();
            }

            ibutton.setAlias(alias.getText().length() > 0 ? alias.getText() : " ");
            ibutton.setPosition(position.getSelectionModel().getSelectedItem());
            ibutton.setModel(model.getText());
            ibutton.setSerial(serial.getText());

            ibuttonService.saveOrUpdate(ibutton);

            VistaNavigator.closeModal(titledPane);
            if (VistaNavigator.getController() != null) {
                // Only necessary if base view needs to know about the new ibutton creation
                VistaNavigator.getController().reload(ibutton);
            }

            deviceConnectedList.replaceDevice(ibutton.getSerial(), ibutton.getAlias(), ibutton.getPositionProperty().getValue());

            logger.info("Saved: " + ibutton);

        } catch (ControlledTemperatusException ex) {
            logger.warn("Invalid data");
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
     * When editing an iButton, pre-load its data
     *
     * @param ibutton iButton to update/edit
     */
    public void setIbuttonForUpdate(Ibutton ibutton) {
        saveButton.setText(language.get(Lang.UPDATE));  // change save button text to update
        this.ibutton = ibutton;
        serial.setText(ibutton.getSerial());
        model.setText(ibutton.getModel());
        alias.setText(ibutton.getAlias());
        position.getSelectionModel().select(ibutton.getPosition());
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Lang.NEWBUTTONTITLE));
        saveButton.setText(language.get(Lang.SAVE));
        cancelButton.setText(language.get(Lang.CANCEL));
        serialLabel.setText(language.get(Lang.SERIAL_LABEL));
        modelLabel.setText(language.get(Lang.MODEL_LABEL));
        defaultPosLabel.setText(language.get(Lang.DEFAULT_POSITIONS_LABEL));
        aliasLabel.setText(language.get(Lang.ALIAS_LABEL));
        alias.setPromptText(language.get(Lang.ALIAS_PROMPT));
    }

}
