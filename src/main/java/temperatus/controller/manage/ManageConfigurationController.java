package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.calculator.Calculator;
import temperatus.controller.AbstractController;
import temperatus.controller.device.NewConfigurationController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Configuration;
import temperatus.model.pojo.types.Unit;
import temperatus.model.service.ConfigurationService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Allow the user to search, edit, delete and create configurations (start missions on devices)
 * <p>
 * Created by alberto on 20/4/16.
 */
@Controller
@Scope("prototype")
public class ManageConfigurationController implements Initializable, AbstractController {

    @FXML private TableView<Configuration> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private Label nameLabel;
    @FXML private Label syncLabel;
    @FXML private Label rollOverLabel;
    @FXML private Label sutaLabel;
    @FXML private Label highAlarmLabel;
    @FXML private Label lowAlarmLabel;
    @FXML private Label rateLabel;
    @FXML private Label delayLabel;
    @FXML private Label resolutionLabel;

    @FXML private Label sync;
    @FXML private Label rollOver;
    @FXML private Label suta;
    @FXML private Label highAlarm;
    @FXML private Label lowAlarm;
    @FXML private Label rate;
    @FXML private Label delay;
    @FXML private Label resolution;

    private TableColumn<Configuration, String> name = new TableColumn<>();

    private ObservableList<Configuration> configurations;

    @Autowired ConfigurationService configurationService;

    private static final String DEFAULT = "Default";
    private static Logger logger = LoggerFactory.getLogger(ManageConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        configurations = FXCollections.observableArrayList();
        name.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());

        FilteredList<Configuration> filteredData = new FilteredList<>(configurations, p -> true);
        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(configuration -> newValue == null || newValue.isEmpty() || configuration.getName().toLowerCase().contains(newValue.toLowerCase()));
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, configuration) -> {
            Animation.fadeInTransition(infoPane);
            if (configuration != null) {
                nameLabel.setText(configuration.getName());
                sync.setText(configuration.isSyncTime() ? language.get(Lang.YES) : language.get(Lang.NO));
                rollOver.setText(configuration.isRollover() ? language.get(Lang.YES) : language.get(Lang.NO));
                suta.setText(configuration.isSuta() ? language.get(Lang.YES) : language.get(Lang.NO));
                rate.setText(configuration.getRate() + "  " + language.get(Lang.SECONDS));
                delay.setText(configuration.getDelay() + "  " + language.get(Lang.SEC));
                resolution.setText(String.valueOf(configuration.getResolutionC1()));

                // Show the data using the preferred unit
                Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C: Unit.F;

                if(Unit.C.equals(unit)) {
                    highAlarm.setText((configuration.getHighAlarmC1() != null ? configuration.getHighAlarmC1() + Constants.SPACE + language.get(Lang.CELSIUS) : language.get(Lang.NOT_SET)));
                    lowAlarm.setText((configuration.getLowAlarmC1() != null ? configuration.getLowAlarmC1() + Constants.SPACE + language.get(Lang.CELSIUS) : language.get(Lang.NOT_SET)));
                } else {
                    highAlarm.setText((configuration.getHighAlarmC1() != null ? Calculator.celsiusToFahrenheit(configuration.getHighAlarmC1()) + Constants.SPACE + language.get(Lang.FAHRENHEIT) : language.get(Lang.NOT_SET)));
                    lowAlarm.setText((configuration.getLowAlarmC1() != null ? Calculator.celsiusToFahrenheit(configuration.getLowAlarmC1()) + Constants.SPACE + language.get(Lang.FAHRENHEIT) : language.get(Lang.NOT_SET)));
                }
            }
        });

        SortedList<Configuration> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(name);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();

        getAllElements();
    }

    /**
     * Fetch all Configurations from database and add it to the table.
     * Use a different thread than the UI thread.
     */
    private void getAllElements() {
        Task<List<Configuration>> getConfigurationsTask = new Task<List<Configuration>>() {
            @Override
            public List<Configuration> call() throws Exception {
                return configurationService.getAll();
            }
        };

        // on task completion add all configurations to the table
        getConfigurationsTask.setOnSucceeded(e -> configurations.setAll(getConfigurationsTask.getValue()));

        // run the task using a thread from the thread pool:
        databaseExecutor.submit(getConfigurationsTask);
    }

    /**
     * Show create new configuration screen with the data pre-selected
     */
    @FXML
    private void editConfiguration() {
        NewConfigurationController newConfigurationController = VistaNavigator.openModal(Constants.NEW_CONFIG, Constants.EMPTY);
        newConfigurationController.setConfigurationForUpdate(table.getSelectionModel().getSelectedItem());
    }

    /**
     * Create a new configuration
     */
    @FXML
    private void newConfiguration() {
        VistaNavigator.openModal(Constants.NEW_CONFIG, Constants.EMPTY);
    }

    /**
     * Delete selected configuration from the database and from the table.
     * The default configuration cannot be deleted.
     */
    @FXML
    private void deleteConfiguration() {
        Configuration configuration = table.getSelectionModel().getSelectedItem();
        if (DEFAULT.equals(configuration.getName())) {
            logger.info("Cannot delete default configuration...");
            VistaNavigator.showAlert(Alert.AlertType.INFORMATION, language.get(Lang.CANNOT_DELETE_DEFAULT_CONF));
        } else {
            logger.info("Deleting configuration... " + configuration);
            if (VistaNavigator.confirmationAlert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION))) {
                configurationService.delete(configuration);
                configurations.remove(configuration);
            }
        }
    }

    /**
     * Reload configuration on edit/create
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Configuration) {
            if (!configurations.contains(object)) {
                configurations.add((Configuration) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select((Configuration) object);
        }
    }

    @Override
    public void translate() {
        filterInput.setPromptText(language.get(Lang.FILTER));
        name.setText(language.get(Lang.NAME_COLUMN));
        newElementButton.setText(language.get(Lang.NEW_CONFIGURATION));
        editButton.setText(language.get(Lang.EDIT));
        deleteButton.setText(language.get(Lang.DELETE));
        syncLabel.setText(language.get(Lang.SYNC_LABEL));
        rollOverLabel.setText(language.get(Lang.ROLL_OVER_LABEL));
        sutaLabel.setText(language.get(Lang.SUTA_LABEL));
        highAlarmLabel.setText(language.get(Lang.HIGH_ALARM_LABEL));
        lowAlarmLabel.setText(language.get(Lang.LOW_ALARM_LABEL));
        rateLabel.setText(language.get(Lang.RATE_LABEL_DEVICE));
        delayLabel.setText(language.get(Lang.DELAY_LABEL));
        resolutionLabel.setText(language.get(Lang.RESOLUTION_LABEL));
        table.setPlaceholder(new Label(language.get(Lang.EMPTY_TABLE_CONFIGURATIONS)));
    }
}
