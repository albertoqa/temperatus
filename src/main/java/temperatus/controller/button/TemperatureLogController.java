package temperatus.controller.button;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.pojo.DeviceMissionData;
import temperatus.calculator.Calculator;
import temperatus.controller.AbstractController;
import temperatus.exporter.CSVExporter;
import temperatus.exporter.MissionExporter;
import temperatus.lang.Lang;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Position;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.types.Unit;
import temperatus.model.pojo.utils.DateAxis;
import temperatus.util.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Modal window showing a graphic with the data of a selected device
 * <p>
 * Created by alberto on 24/4/16.
 */
@Controller
@Scope("prototype")
public class TemperatureLogController implements Initializable, AbstractController {

    @FXML private StackPane stackPane;
    @FXML private Label headerLabel;
    @FXML private Button backButton;
    @FXML private Button exportButton;

    @FXML private LineChart<Date, Number> lineChart;
    @FXML private DateAxis dateAxis;
    @FXML private NumberAxis temperatureAxis;

    private DeviceMissionData deviceMissionData;
    private ObservableList<XYChart.Series<Date, Number>> series;
    private String serial;
    private String defaultPosition;
    private String alias;

    private static Logger logger = LoggerFactory.getLogger(TemperatureLogController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        series = FXCollections.observableArrayList();

        lineChart.setData(series);
        lineChart.setAnimated(false);
        lineChart.legendVisibleProperty().setValue(false);
    }

    /**
     * Set data to show in this view
     *
     * @param deviceMissionData information of the device's mission
     * @param serial            serial of the device
     * @param defaultPosition   position of the device
     */
    public void setData(DeviceMissionData deviceMissionData, String serial, String defaultPosition, String alias) {
        logger.debug("Setting data...");
        this.deviceMissionData = deviceMissionData;
        this.serial = serial;
        this.defaultPosition = defaultPosition;
        this.alias = alias;
        drawData();
    }

    /**
     * Draw the measurements in the chart.
     * Generate them in another thread for performance improvement.
     */
    private void drawData() {
        XYChart.Series serie = new XYChart.Series<>();

        Task<Void> drawMeasurementsTask = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                // Show the data using the preferred unit
                Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;

                deviceMissionData.getMeasurements().stream().forEach((measurement) -> {
                    double data = Unit.C.equals(unit) ? measurement.getData() : Calculator.celsiusToFahrenheit(measurement.getData());
                    serie.getData().add(new XYChart.Data<>(measurement.getDate(), data));
                });

                return null;
            }
        };

        drawMeasurementsTask.setOnSucceeded(event -> Platform.runLater(() -> {
            series.add(serie);
            ChartToolTip.addToolTipOnHover(serie, lineChart);
        }));

        Thread thread = new Thread(drawMeasurementsTask);  // start task in a new thread
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Show a fileChooser and export this mission to the chosen file
     *
     * @throws IOException
     */
    @FXML
    private void export() throws IOException {
        logger.info("Exporting device data...");

        // Only allow export if complete version of the application, trial version cannot export data
        if (KeyValidator.checkActivationStatus()) {
            String name;

            if (alias != null && !alias.isEmpty()) {
                name = alias;
            } else {
                name = deviceMissionData.getSerial();
            }

            File file = FileUtils.saveCSVAndExcelDialog(name, stackPane.getScene().getWindow());
            if (file != null) {
                // Check if user wants to export to csv or excel
                if (file.getName().contains("csv") || file.getName().contains("CSV")) {
                    exportToCsv(file);
                } else {
                    exportToExcel(file);
                }

                // set default directory to current
                VistaNavigator.directory = file.getParent();
            }
            //back();      // close the window
        }
    }

    /**
     * Export the device information to excel
     *
     * @param file file to write to
     * @throws IOException
     */
    private void exportToExcel(File file) throws IOException {
        // create a new mission exporter and set the data to export
        MissionExporter missionExporter = new MissionExporter();

        Record record = new Record();

        if (defaultPosition != null && !defaultPosition.isEmpty()) {
            record.setPosition(new Position(defaultPosition));
        } else {
            record.setPosition(new Position(serial));
        }

        List<Record> records = new ArrayList<>();
        records.add(record);

        HashMap<Record, List<Measurement>> dataMap = new HashMap<>();
        dataMap.put(record, deviceMissionData.getMeasurements());

        // Export the data using the preferred unit
        Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;

        // period = 1, no formulas and no all records needed
        missionExporter.setData(1, serial, records, new ArrayList<>(), dataMap, unit);

        FileUtils.writeDataToFile(file, missionExporter.export());
    }

    /**
     * Export device's data to csv
     *
     * @param file file to write to
     */
    private void exportToCsv(File file) {
        CSVExporter.exportToCsv(file, deviceMissionData);
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
        dateAxis.setLabel(language.get(Lang.DATE_AXIS));
        if (Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C)) {
            temperatureAxis.setLabel(language.get(Lang.TEMPERATURE_AXIS_C));
        } else {
            temperatureAxis.setLabel(language.get(Lang.TEMPERATURE_AXIS_F));
        }
        headerLabel.setText(language.get(Lang.TEMPERATURE_LOG));
        backButton.setText(language.get(Lang.BACK_BUTTON));
        exportButton.setText(language.get(Lang.EXPORT));
    }
}
