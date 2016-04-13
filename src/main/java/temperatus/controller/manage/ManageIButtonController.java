package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewIButtonController;
import temperatus.model.pojo.Ibutton;
import temperatus.model.service.IbuttonService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManageIButtonController implements Initializable, AbstractController {

    @FXML private TableView<Ibutton> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    private TableColumn<Ibutton, String> model = new TableColumn<>();
    private TableColumn<Ibutton, String> serial = new TableColumn<>();
    private TableColumn<Ibutton, String> alias = new TableColumn<>();
    private TableColumn<Ibutton, String> defaultPosition = new TableColumn<>();

    private ObservableList<Ibutton> ibuttons;

    @Autowired IbuttonService ibuttonService;

    static Logger logger = LoggerFactory.getLogger(ManageIButtonController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        ibuttons = FXCollections.observableArrayList();
        addAlliButtons();

        model.setText("Model");
        model.setCellValueFactory(cellData -> cellData.getValue().getModelProperty());
        serial.setText("Serial");
        serial.setCellValueFactory(cellData -> cellData.getValue().getSerialProperty());
        alias.setText("Alias");
        alias.setCellValueFactory(cellData -> cellData.getValue().getAliasProperty());
        defaultPosition.setText("Default Position");
        defaultPosition.setCellValueFactory(cellData -> cellData.getValue().getPositionProperty());

        FilteredList<Ibutton> filteredData = new FilteredList<>(ibuttons, p -> true);

        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(ibutton -> {
                // If filter text is empty, display all authors.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (ibutton.getModel().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if(ibutton.getAlias().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if(ibutton.getSerial().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if(ibutton.getPosition().getPlace().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, ibutton) -> {
            Animation.fadeInTransition(infoPane);

            if(ibutton != null) {

            }
        });

        SortedList<Ibutton> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(model, serial, alias, defaultPosition);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    private void addAlliButtons() {
        ibuttons.addAll(ibuttonService.getAll());
    }

    @FXML
    private void editIbutton() {
        NewIButtonController newIButtonController = VistaNavigator.openModal(Constants.NEW_IBUTTON, language.get(Constants.NEWBUTTONTITLE));
        newIButtonController.setIbuttonForUpdate(table.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void deleteIbutton() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Ibutton ibutton = table.getSelectionModel().getSelectedItem();
            ibuttonService.delete(ibutton);
            ibuttons.remove(ibutton);
        }
    }

    @Override
    public void reload(Object object) {
        if(object instanceof Ibutton) {
            if(!ibuttons.contains((Ibutton) object)) {
                ibuttons.add((Ibutton) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().select((Ibutton) object);
        }
    }

    @Override
    public void translate() {

    }

}
