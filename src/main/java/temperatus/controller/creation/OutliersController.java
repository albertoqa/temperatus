package temperatus.controller.creation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.analysis.pojo.ValidatedData;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Measurement;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Show a modal window with all the detected outliers, allow the user to edit/remove them
 * <p>
 * Created by alberto on 1/5/16.
 */
@Controller
public class OutliersController implements Initializable, AbstractController {

    @FXML private TableView<Measurement> tableView;
    @FXML private StackPane stackPane;
    @FXML private Label headerLabel;
    @FXML private Button continueButton;

    private TableColumn<Measurement, String> position = new TableColumn<>();
    private TableColumn<Measurement, String> date = new TableColumn<>();
    private TableColumn<Measurement, String> value = new TableColumn<>();

    private ObservableList<Measurement> measurements;

    private static Logger logger = LoggerFactory.getLogger(OutliersController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        measurements = FXCollections.observableArrayList();

        position.setCellValueFactory(cellData -> cellData.getValue().getRecord().getPosition().getPlaceProperty());
        date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
        value.setCellValueFactory(cellData -> cellData.getValue().getDataProperty());

        tableView.getColumns().addAll(position, date, value);
        tableView.setItems(measurements);

        tableView.setEditable(true);
        value.setCellFactory(TextFieldTableCell.forTableColumn());
        value.setOnEditCommit(t -> {
            t.getTableView().getItems().get(t.getTablePosition().getRow()).setData(Double.valueOf(t.getNewValue()));
            // exception not controlled, let it like this
        });
    }

    /**
     * Set all the outliers detected
     *
     * @param data outliers
     */
    void setValidatedDataList(List<ValidatedData> data) {
        for (ValidatedData validatedData : data) {
            measurements.addAll(validatedData.getPossibleErrors());
        }
    }

    /**
     * Close the window and continue
     */
    @FXML
    private void saveAndContinue() {
        logger.debug("Closing outliers... continuing execution");
        VistaNavigator.closeModal(stackPane);
    }

    @Override
    public void translate() {
        date.setText(language.get(Lang.DATE_COLUMN));
        position.setText(language.get(Lang.POSITION_COLUMN));
        value.setText(language.get(Lang.VALUE_COLUMN));
        headerLabel.setText(language.get(Lang.OUTLIERS));
        continueButton.setText(language.get(Lang.CONTINUE));
    }
}
