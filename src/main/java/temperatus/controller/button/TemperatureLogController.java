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
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.calculator.Calculator;
import temperatus.controller.AbstractController;
import temperatus.exporter.MissionExporter;
import temperatus.lang.Lang;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Position;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.types.Unit;
import temperatus.model.pojo.utils.DateAxis;
import temperatus.util.ChartToolTip;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.io.FileOutputStream;
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

    private List<Measurement> measurements;
    private ObservableList<XYChart.Series<Date, Number>> series;
    private String serial;
    private String defaultPosition;

    private static Logger logger = LoggerFactory.getLogger(TemperatureLogController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        measurements = new ArrayList<>();
        series = FXCollections.observableArrayList();

        lineChart.setData(series);
        lineChart.setAnimated(false);
        lineChart.legendVisibleProperty().setValue(false);
    }

    /**
     * Set the data to show on this view
     *
     * @param measurements list of measurements to show
     */
    public void setData(List<Measurement> measurements, String serial, String defaultPosition) {
        logger.debug("Setting data...");
        this.measurements = measurements;
        this.serial = serial;
        this.defaultPosition = defaultPosition;
        drawData();
    }

    /**
     * Draw the measurements in the chart.
     * Generate them in another thread for performance improvement.
     */
    private void drawData() {
        XYChart.Series<Date, Number> serie = new XYChart.Series<>();

        Task<Void> drawMeasurementsTask = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                // Show the data using the preferred unit
                Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;

                measurements.stream().forEach((measurement) -> {
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
        if (Constants.prefs.getBoolean(Constants.ACTIVATED, false)) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLS (*.xls)", "*.xls"));   //Set extension filter

            File file = fileChooser.showSaveDialog(stackPane.getScene().getWindow());   //Show save file dialog

            if (file != null) {
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
                dataMap.put(record, measurements);

                // Export the data using the preferred unit
                Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;

                // period = 1, no formulas and no all records needed
                missionExporter.setData(1, serial, records, new ArrayList<>(), dataMap, unit);

                Workbook workBook = missionExporter.export();

                FileOutputStream fileOut = new FileOutputStream(file);  // write generated data to a file
                workBook.write(fileOut);
                fileOut.close();
            }
            //back();      // close the window
        } else {
            VistaNavigator.openModal(Constants.BUY_COMPLETE, Constants.EMPTY);
        }
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
