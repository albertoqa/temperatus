package temperatus.controller.creation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.Position;
import temperatus.model.service.IbuttonService;
import temperatus.model.service.PositionService;
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

    @Override @FXML
    void save() {
        Ibutton ibutton = new Ibutton();
        ibutton.setSerial(serial.getText());
        ibutton.setModel(model.getText());
        ibutton.setPosition(position.getSelectionModel().getSelectedItem());
        ibutton.setAlias(alias.getText());

        ibuttonService.save(ibutton);
        VistaNavigator.getController().reload(ibutton);
    }

    @Override
    public void translate() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // * Load all projects from database and allow the user to choose them
        ObservableList<Position> positions = FXCollections.observableArrayList();
        positions.addAll(positionService.getAll().stream().collect(Collectors.toList()));
        position.setItems(positions);

    }

    public void setData(String serial, String model) {
        this.serial.setText(serial);
        this.model.setText(model);
    }
}
