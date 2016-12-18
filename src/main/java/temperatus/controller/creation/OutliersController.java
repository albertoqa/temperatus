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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.pojo.ValidatedData;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Measurement;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Show a modal window with all the detected outliers, allow the user to edit/remove them
 * <p>
 * Created by alberto on 1/5/16.
 */
@Controller
@Scope("prototype")
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

        position.setCellValueFactory(cellData -> cellData.getValue().positionProperty());
        date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
        value.setCellValueFactory(cellData -> cellData.getValue().getDataProperty());

        tableView.getColumns().addAll(position, date, value);
        tableView.setItems(measurements);

        tableView.setEditable(true);
        value.setCellFactory(TextFieldTableCell.forTableColumn());
        value.setOnEditCommit(t -> {
            Measurement m = t.getTableView().getItems().get(t.getTablePosition().getRow());
            updateMeasurementInFile(m, Double.valueOf(t.getNewValue()));
            // exception not controlled, let it like this
        });
    }

    /**
     * Update the temperature value in the file
     *
     * @param measurement measurement to update
     */
    private void updateMeasurementInFile(Measurement measurement, Double newValue) {
        String lineToChange = Constants.dateTimeFormat.format(measurement.getDate()) + Constants.COMMA + Constants.UNIT_C + Constants.COMMA + measurement.getData();
        String updatedLine = Constants.dateTimeFormat.format(measurement.getDate()) + Constants.COMMA + Constants.UNIT_C + Constants.COMMA + newValue;

        lineToChange = lineToChange.replace(Constants.DOT, Constants.COMMA);  // if measurement is like 3.3, change it to 3,3
        updatedLine = updatedLine.replace(Constants.DOT, Constants.COMMA);

        try {
            measurement.setData(newValue);
            updateLine(lineToChange, updatedLine, measurement.getFile());   // replace line in file
        } catch (IOException e) {
            logger.error("Line cannot be replaced...");
        }
    }

    /**
     * Open the file related to the measurement, search for the line line to update and replace it with the new line
     *
     * @param toUpdate line to update
     * @param updated new line
     * @param data file where the data is stored
     * @throws IOException
     */
    private void updateLine(String toUpdate, String updated, File data) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(data));
        String line;
        StringBuilder stringBuilder = new StringBuilder(Constants.EMPTY);

        while ((line = file.readLine()) != null) {
            stringBuilder.append(line).append(System.lineSeparator());
        }

        String input = stringBuilder.toString();
        input = input.replace(toUpdate, updated);

        FileOutputStream os = new FileOutputStream(data);
        os.write(input.getBytes());

        file.close();
        os.close();
    }

    /**
     * Set all the outliers detected
     *
     * @param data outliers
     */
    void setValidatedDataList(List<ValidatedData> data) {
        for (ValidatedData validatedData : data) {
            for (Measurement measurement : validatedData.getPossibleErrors()) {
                measurement.setFile(validatedData.getDataFile());
                measurement.setPosition(validatedData.getPosition().getPlace());
                measurements.add(measurement);
            }
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
        tableView.setPlaceholder(new Label(language.get(Lang.EMPTY_TABLE_MEASUREMENTS)));
    }
}
