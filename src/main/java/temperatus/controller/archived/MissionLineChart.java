package temperatus.controller.archived;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.controlsfx.control.CheckListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.IButtonDataAnalysis;
import temperatus.calculator.Calculator;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.types.Unit;
import temperatus.model.pojo.utils.DateAxis;
import temperatus.util.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Mission information shown in a graph
 * <p>
 * Created by alberto on 6/4/16.
 */
@Controller
@Scope("prototype")
public class MissionLineChart implements Initializable, AbstractController {

    @FXML private AnchorPane anchorPane;
    @FXML private StackPane stackPane;

    private LineChart lineChart;
    private DateAxis dateAxis;
    private NumberAxis indexAxis;
    private NumberAxis temperatureAxis;

    @FXML private CheckListView<Record> positionsList;
    @FXML private CheckListView<Formula> formulasList;

    @FXML private CheckBox showSymbols;

    @FXML private Button saveGraphicButton;

    @FXML private Spinner<Integer> spinner;

    private ObservableList<XYChart.Series> series;
    private ObservableList<Record> records;
    private ObservableList<Formula> formulas;
    private boolean writeAsIndex = false;

    private HashMap<Record, List<Measurement>> dataMap;

    private static Logger logger = LoggerFactory.getLogger(MissionLineChart.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        series = FXCollections.observableArrayList();
        records = FXCollections.observableArrayList();
        formulas = FXCollections.observableArrayList();

        positionsList.setItems(records);
        formulasList.setItems(formulas);

        SpinnerFactory.setIntegerSpinner(spinner, 1);

        writeAsIndex = Constants.prefs.getBoolean(Constants.WRITE_AS_INDEX, Constants.WRITE_INDEX);
        temperatureAxis = new NumberAxis();
        if (writeAsIndex) {
            indexAxis = new NumberAxis();
            lineChart = new LineChart<>(indexAxis, temperatureAxis);
        } else {
            dateAxis = new DateAxis();
            lineChart = new LineChart<>(dateAxis, temperatureAxis);
        }

        stackPane.getChildren().add(lineChart);

        lineChart.setData(series);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);

        // Listener for positions, when a position is checked show it on the graph
        positionsList.getCheckModel().getCheckedItems().addListener((ListChangeListener<Record>) c -> {
            c.next();
            if (c.wasAdded()) {
                addSerieForRecord(c.getAddedSubList().get(0), spinner.getValue(), showSymbols.isSelected());
            } else if (c.wasRemoved()) {
                for (XYChart.Series serie : series) {
                    if (serie.getName().contains(c.getRemoved().get(0).getPosition().getPlace())) {
                        series.remove(serie);
                        break;
                    }
                }
            }
        });

        // Listener for formulas, when a formula is checked show it on the graph
        formulasList.getCheckModel().getCheckedItems().addListener((ListChangeListener<Formula>) c -> {
            c.next();
            if (c.wasAdded()) {
                addSerieForFormula(c.getAddedSubList().get(0), spinner.getValue(), showSymbols.isSelected());
            } else if (c.wasRemoved()) {
                for (XYChart.Series serie : series) {
                    if (serie.getName().contains(c.getRemoved().get(0).getName())) {
                        series.remove(serie);
                        break;
                    }
                }
            }
        });

        // On spinner value change recalculate all the data shown on the graph
        spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                reloadSelectedSeriesForPeriod(newValue, showSymbols.isSelected());
            } else {
                spinner.getEditor().setText("1");
                reloadSelectedSeriesForPeriod(1, showSymbols.isSelected());
            }
        });

        showSymbols.selectedProperty().addListener((observable, oldValue, newValue) -> reloadSelectedSeriesForPeriod(spinner.getValue(), newValue));

        translate();
    }

    /**
     * Add a new serie for the selected formula and the given period to the chart
     *
     * @param formula formula to add
     * @param period  period to calculate
     */
    private void addSerieForFormula(Formula formula, int period, boolean showSymbols) {
        XYChart.Series serie = createSerieForFormula(formula, period);
        series.add(serie);
        if (showSymbols) {
            ChartToolTip.addToolTipOnHover(serie, lineChart);
        }
    }

    /**
     * Add a new serie for the selected record (position) and the given period to the chart
     *
     * @param record related to the position
     * @param period period to calculate
     */
    private void addSerieForRecord(Record record, int period, boolean showSymbols) {
        XYChart.Series serie = createSerieForRecord(record, period);
        series.add(serie);
        if (showSymbols) {
            ChartToolTip.addToolTipOnHover(serie, lineChart);
        }
    }

    /**
     * Reload selected positions and formulas with a new period
     *
     * @param period new period to calculate
     */
    private void reloadSelectedSeriesForPeriod(int period, boolean showSymbols) {
        series.clear();
        lineChart.setCreateSymbols(showSymbols);

        for (Record record : positionsList.getCheckModel().getCheckedItems()) {
            addSerieForRecord(record, period, showSymbols);
        }
        for (Formula formula : formulasList.getCheckModel().getCheckedItems()) {
            addSerieForFormula(formula, period, showSymbols);
        }
    }

    /**
     * Create a new serie for the given record and period. Group temperatures in groups of <period> measurements and
     * add them to the chart.
     *
     * @param record record to create the serie
     * @param period period to calculate
     * @return serie to show for the given record
     */
    private XYChart.Series createSerieForRecord(Record record, int period) {
        List<Measurement> measurements = IButtonDataAnalysis.getListOfMeasurementsForPeriod(dataMap.get(record), period);

        XYChart.Series serie = new XYChart.Series<>();
        serie.setName(record.getPosition().getPlace());

        writeSerie(serie, measurements);

        return serie;
    }

    /**
     * Create a new serie for the given formula and period. Group temperatures in groups of <period> measurements and
     * add them to the chart.
     *
     * @param formula formula to create the serie
     * @param period  period to calculate
     * @return serie to show for the given formula
     */
    private XYChart.Series createSerieForFormula(Formula formula, int period) {

        XYChart.Series serie = new XYChart.Series<>();
        serie.setName(formula.getName());
        List<Measurement> measurements = IButtonDataAnalysis.getListOfMeasurementsForFormulaAndPeriod(dataMap, formula, period);

        writeSerie(serie, measurements);

        for (Measurement measurement : measurements) {
            if (Double.isNaN(measurement.getData())) {
                VistaNavigator.showAlert(Alert.AlertType.WARNING, language.get(Lang.ERROR_CALCULATING_FORMULA));
                break;
            }
        }

        return serie;
    }

    private void writeSerie(XYChart.Series serie, List<Measurement> measurements) {
        // Show the data using the preferred unit
        Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;

        if (writeAsIndex) {
            for (int i = 0; i < measurements.size(); i++) {
                double data = Unit.C.equals(unit) ? measurements.get(i).getData() : Calculator.celsiusToFahrenheit(measurements.get(i).getData());
                serie.getData().add(new XYChart.Data<>(i, data));
            }
        } else {
            measurements.stream().forEach((measurement) -> {
                double data = Unit.C.equals(unit) ? measurement.getData() : Calculator.celsiusToFahrenheit(measurement.getData());
                serie.getData().add(new XYChart.Data<>(measurement.getDate(), data));
            });
        }
    }

    /**
     * Set the mission data for the controller
     *
     * @param dataMap  record and measurements for the mission
     * @param formulas formulas selected for the mission
     */
    public void setData(HashMap<Record, List<Measurement>> dataMap, Set<Formula> formulas) {
        this.dataMap = dataMap;
        this.formulas.addAll(formulas);
        this.records.addAll(dataMap.keySet());

        positionsList.getCheckModel().check(0);     // select only the first record at the beginning so it load fast
        reloadSelectedSeriesForPeriod(1, false);           // necessary to call this on init
    }

    /**
     * Save the current char (temperature log) to a png file
     */
    @FXML
    public void saveAsPng() {
        // Only allow export if complete version of the application, trial version cannot export data
        if (Constants.prefs.getBoolean(Constants.ACTIVATED, false)) {
            File file = FileUtils.saveDialog(null, anchorPane.getScene().getWindow(), new FileChooser.ExtensionFilter("PNG (*.png)", "*.png"));

            if (file != null) {
                WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);

                // set default directory to current
                VistaNavigator.directory = file.getParent();

                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                } catch (IOException e) {
                    logger.error("Error saving image of the chart - Mission Line Chart");
                    showAlert(Alert.AlertType.ERROR, language.get(Lang.ERROR_SAVING_IMAGE));
                }
            }
        } else {
            VistaNavigator.openModal(Constants.BUY_COMPLETE, Constants.EMPTY);
        }
    }

    @Override
    public void translate() {
        saveGraphicButton.setText(language.get(Lang.SAVE_GRAPHIC));
        if (writeAsIndex) {
            indexAxis.setLabel(language.get(Lang.INDEX));
        } else {
            dateAxis.setLabel(language.get(Lang.DATE_AXIS));
        }
        if (Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C)) {
            temperatureAxis.setLabel(language.get(Lang.TEMPERATURE_AXIS_C));
        } else {
            temperatureAxis.setLabel(language.get(Lang.TEMPERATURE_AXIS_F));
        }
    }
}
