package temperatus.controller.archived;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.calculator.Calculator;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Measurement;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Modal window showing the complete data of a selected device
 * <p>
 * Created by alberto on 5/4/16.
 */
@Controller
@Scope("prototype")
public class ButtonDataController implements Initializable, AbstractController {

    @FXML private TableView<Measurement> tableView;
    @FXML private StackPane stackPane;
    @FXML private Label headerLabel;
    @FXML private Button backButton;

    private TableColumn<Measurement, String> date = new TableColumn<>();
    private TableColumn<Measurement, String> unit = new TableColumn<>();
    private TableColumn<Measurement, String> value = new TableColumn<>();

    private ObservableList<Measurement> measurements;
    private SimpleStringProperty unitOfMeasurePreferred;

    private static Logger logger = LoggerFactory.getLogger(ButtonDataController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        unitOfMeasurePreferred = new SimpleStringProperty(Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Constants.UNIT_C: Constants.UNIT_F);

        measurements = FXCollections.observableArrayList();

        date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
        unit.setCellValueFactory(cellData -> unitOfMeasurePreferred);

        if(Constants.UNIT_C.equals(unitOfMeasurePreferred.getValue())) {
            value.setCellValueFactory(cellData -> cellData.getValue().getDataProperty());
        } else {
            value.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(Calculator.celsiusToFahrenheit(cellData.getValue().getData()))));
        }

        tableView.getColumns().addAll(date, unit, value);
        tableView.setItems(measurements);
    }

    /**
     * Set the data to show on this view
     *
     * @param measurements list of measurements to show
     */
    public void setData(List<Measurement> measurements) {
        this.measurements.addAll(measurements);
        logger.debug("Setting data...");
    }

    /**
     * Close the window and go back to the previous screen
     */
    @FXML
    private void back() {
        VistaNavigator.closeModal(stackPane);
    }

    @Override
    public void translate() {
        date.setText(language.get(Lang.DATE_COLUMN));
        unit.setText(language.get(Lang.UNIT_COLUMN));
        value.setText(language.get(Lang.VALUE_COLUMN));
        headerLabel.setText(language.get(Lang.BUTTON_DATA));
        backButton.setText(language.get(Lang.BACK_BUTTON));
    }
}
