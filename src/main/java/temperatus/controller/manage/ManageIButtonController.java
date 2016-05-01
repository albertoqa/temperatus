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
import temperatus.lang.Lang;
import temperatus.model.pojo.Ibutton;
import temperatus.model.service.IbuttonService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Allow the user to search, edit and delete iButtons
 * <p>
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManageIButtonController implements Initializable, AbstractController {

    @FXML private TableView<Ibutton> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;

    @FXML private Label nameLabel;
    @FXML private Label modelLabel;
    @FXML private Label aliasLabel;
    @FXML private Label defPosLabel;
    @FXML private Label modelInfo;
    @FXML private Label aliasInfo;
    @FXML private Label defPos;

    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private TableColumn<Ibutton, String> model = new TableColumn<>();
    private TableColumn<Ibutton, String> serial = new TableColumn<>();
    private TableColumn<Ibutton, String> alias = new TableColumn<>();
    private TableColumn<Ibutton, String> defaultPosition = new TableColumn<>();

    private ObservableList<Ibutton> ibuttons;

    @Autowired IbuttonService ibuttonService;

    private static Logger logger = LoggerFactory.getLogger(ManageIButtonController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        ibuttons = FXCollections.observableArrayList(ibuttonService.getAll());
        model.setCellValueFactory(cellData -> cellData.getValue().getModelProperty());
        serial.setCellValueFactory(cellData -> cellData.getValue().getSerialProperty());
        alias.setCellValueFactory(cellData -> cellData.getValue().getAliasProperty());
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
                } else if (ibutton.getAlias().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (ibutton.getSerial().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (ibutton.getPosition().getPlace().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, ibutton) -> {
            Animation.fadeInTransition(infoPane);
            if (ibutton != null) {
                nameLabel.setText(ibutton.getSerial().toUpperCase());
                defPos.setText(ibutton.getPosition() != null ? ibutton.getPosition().getPlace() : language.get(Lang.NOT_SET));
                modelInfo.setText(ibutton.getModel());
                aliasInfo.setText(ibutton.getAlias());
            }
        });

        SortedList<Ibutton> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(model, serial, alias, defaultPosition);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    /**
     * Edit the selected iButton - only alias and default position
     */
    @FXML
    private void editIbutton() {
        NewIButtonController newIButtonController = VistaNavigator.openModal(Constants.NEW_IBUTTON, language.get(Lang.NEWBUTTONTITLE));
        newIButtonController.setIbuttonForUpdate(table.getSelectionModel().getSelectedItem());
    }

    /**
     * Delete the selected button
     */
    @FXML
    private void deleteIbutton() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Ibutton ibutton = table.getSelectionModel().getSelectedItem();
            ibuttonService.delete(ibutton);
            ibuttons.remove(ibutton);
            logger.info("Deleted ibutton... " + ibutton);
        }
    }

    /**
     * Reload a edited iButton
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Ibutton) {
            if (!ibuttons.contains(object)) {
                ibuttons.add((Ibutton) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select((Ibutton) object);
        }
    }

    @Override
    public void translate() {
        filterInput.setPromptText(language.get(Lang.FILTER));
        model.setText(language.get(Lang.MODEL_COLUMN));
        serial.setText(language.get(Lang.SERIAL_COLUMN));
        alias.setText(language.get(Lang.ALIAS_COLUMN));
        defaultPosition.setText(language.get(Lang.DEFAULT_POS_COLUMN));
        defPos.setText(language.get(Lang.DEFAULT_POSITIONS_LABEL));
        modelInfo.setText(language.get(Lang.MODEL_LABEL));
        aliasInfo.setText(language.get(Lang.ALIAS_LABEL));
        editButton.setText(language.get(Lang.EDIT));
        deleteButton.setText(language.get(Lang.DELETE));
    }

}
