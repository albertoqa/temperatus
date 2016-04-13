package temperatus.controller.creation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.Position;
import temperatus.model.pojo.types.AutoCompleteComboBoxListener;
import temperatus.model.service.IbuttonService;
import temperatus.model.service.PositionService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
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

    private Ibutton ibutton;

    static Logger logger = LoggerFactory.getLogger(NewIButtonController.class.getName());

    @Override @FXML
    void save() {

        String al;
        Position pos;

        try {
            logger.info("Saving iButton...");

            al = alias.getText();
            pos = position.getSelectionModel().getSelectedItem();

            if(ibutton == null) {
                ibutton = new Ibutton();
            }

            ibutton.setAlias(al);
            ibutton.setPosition(pos);
            ibutton.setModel(model.getText());
            ibutton.setSerial(serial.getText());

            ibuttonService.saveOrUpdate(ibutton);

            VistaNavigator.closeModal(titledPane);
            if (VistaNavigator.getController() != null) {
                // Only necessary if base view needs to know about the new ibutton creation
                VistaNavigator.getController().reload(ibutton);
            }

            logger.info("Saved: " + ibutton);

        } catch (ConstraintViolationException ex) {
            logger.warn("Duplicate entry");
            showAlert(Alert.AlertType.ERROR, "Duplicate entry");
        } catch (Exception ex) {
            logger.warn("Unknown exception" + ex.getMessage());
            showAlert(Alert.AlertType.ERROR, "Unknown error.");
        }
    }

    public void setIbuttonForUpdate(Ibutton ibutton) {
        saveButton.setText(language.get(Constants.UPDATE));
        this.ibutton = ibutton;
        serial.setText(ibutton.getSerial());
        model.setText(ibutton.getModel());
        alias.setText(ibutton.getAlias());
        position.getSelectionModel().select(ibutton.getPosition());
    }

    @Override
    public void translate() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // * Load all positions from database and allow the user to choose them
        ObservableList<Position> positions = FXCollections.observableArrayList();
        positions.addAll(positionService.getAll().stream().collect(Collectors.toList()));
        position.setItems(positions);

        new AutoCompleteComboBoxListener<>(position);

    }

    public void setData(String serial, String model) {
        this.serial.setText(serial);
        this.model.setText(model);
    }
}
