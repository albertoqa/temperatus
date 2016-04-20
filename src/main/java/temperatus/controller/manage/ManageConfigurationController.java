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
import temperatus.model.pojo.Configuration;
import temperatus.model.service.ConfigurationService;
import temperatus.util.Animation;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by alberto on 20/4/16.
 */
@Controller
@Scope("prototype")
public class ManageConfigurationController implements Initializable, AbstractController {

    @FXML private TableView<Configuration> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    private TableColumn<Configuration, String> name = new TableColumn<>();

    private ObservableList<Configuration> configurations;

    @Autowired ConfigurationService configurationService;

    static Logger logger = LoggerFactory.getLogger(ManageConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        configurations = FXCollections.observableArrayList();
        addAllConfigurations();

        name.setText("Name");
        name.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());

        FilteredList<Configuration> filteredData = new FilteredList<>(configurations, p -> true);

        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(configuration -> {
                // If filter text is empty, display all configurations.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (configuration.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(configuration.getRate()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, configuration) -> {
            Animation.fadeInTransition(infoPane);

            if (configuration != null) {
                // TODO
            }

        });

        SortedList<Configuration> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(name);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    private void addAllConfigurations() {
        configurations.addAll(configurationService.getAll());
    }

    @FXML
    private void editConfiguration() {  // TODO
        //NewAuthorController newAuthorController = VistaNavigator.openModal(Constants.NEW_AUTHOR, language.get(Lang.NEWAUTHOR));
        //newAuthorController.setAuthorForUpdate(table.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void newConfiguration() {   // TODO
        //VistaNavigator.openModal(Constants.NEW_AUTHOR, language.get(Lang.NEWAUTHOR));
    }

    @FXML
    private void deleteConfiguration() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Configuration configuration = table.getSelectionModel().getSelectedItem();
            configurationService.delete(configuration);
            configurations.remove(configuration);
        }
    }

    @Override
    public void reload(Object object) {
        if (object instanceof Configuration) {
            if (!configurations.contains((Configuration) object)) {
                configurations.add((Configuration) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().select((Configuration) object);
        }
    }

    @Override
    public void translate() {

    }
}
