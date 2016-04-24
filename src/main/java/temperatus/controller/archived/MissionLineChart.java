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
import javafx.scene.control.Spinner;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import org.controlsfx.control.CheckListView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.IButtonDataAnalysis;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.utils.DateAxis;
import temperatus.util.ChartToolTip;
import temperatus.util.IntegerSpinner;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by alberto on 6/4/16.
 */
@Controller
@Scope("prototype")
public class MissionLineChart implements Initializable, AbstractController {

    @FXML private LineChart<Date, Number> lineChart;
    @FXML private DateAxis dateAxis;
    @FXML private NumberAxis temperatureAxis;

    @FXML private CheckListView<Record> positionsList;
    @FXML private CheckListView<Formula> formulasList;

    @FXML private Spinner<Integer> spinner;

    private ObservableList<XYChart.Series<Date, Number>> series;
    private ObservableList<Record> records;
    private ObservableList<Formula> formulas;

    private HashMap<Record, List<Measurement>> dataMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        series = FXCollections.observableArrayList();
        records = FXCollections.observableArrayList();
        formulas = FXCollections.observableArrayList();

        positionsList.setItems(records);
        formulasList.setItems(formulas);

        IntegerSpinner.setSpinner(spinner);

        lineChart.setData(series);
        lineChart.setAnimated(false);
        dateAxis.setLabel("Time of measurement");
        temperatureAxis.setLabel("Temperature in ºC");
        //lineChart.setCreateSymbols(false);

        positionsList.getCheckModel().getCheckedItems().addListener(new ListChangeListener<Record>() {
            public void onChanged(ListChangeListener.Change<? extends Record> c) {
                c.next();
                if (c.wasAdded()) {
                    addSerieForRecord(c.getAddedSubList().get(0), spinner.getValue());
                } else if (c.wasRemoved()) {
                    for (XYChart.Series<Date, Number> serie : series) {
                        if (serie.getName().contains(c.getRemoved().get(0).getPosition().getPlace())) {
                            series.remove(serie);
                            break;
                        }
                    }
                }
            }
        });

        formulasList.getCheckModel().getCheckedItems().addListener(new ListChangeListener<Formula>() {
            public void onChanged(ListChangeListener.Change<? extends Formula> c) {
                c.next();
                if (c.wasAdded()) {
                    addSerieForFormula(c.getAddedSubList().get(0), spinner.getValue());
                } else if (c.wasRemoved()) {
                    for (XYChart.Series<Date, Number> serie : series) {
                        if (serie.getName().contains(c.getRemoved().get(0).getName())) {
                            series.remove(serie);
                            break;
                        }
                    }
                }
            }
        });

        spinner.valueProperty().addListener((obs, oldValue, newValue) -> reloadSelectedSeriesForPeriod(newValue));

    }

    private void addSerieForFormula(Formula formula, int period) {
        XYChart.Series<Date, Number> serie = createSerieForFormula(formula, period);
        series.add(serie);
        ChartToolTip.addToolTipOnHover(serie, lineChart);
    }

    private void addSerieForRecord(Record record, int period) {
        XYChart.Series<Date, Number> serie = createSerieForRecord(record, period);
        series.add(serie);
        ChartToolTip.addToolTipOnHover(serie, lineChart);
    }

    private void reloadSelectedSeriesForPeriod(int period) {
        series.clear();

        for(Record record: positionsList.getCheckModel().getCheckedItems()) {
            addSerieForRecord(record, period);
        }

        for(Formula formula: formulasList.getCheckModel().getCheckedItems()) {
            addSerieForFormula(formula, period);
        }
    }

    private XYChart.Series<Date, Number> createSerieForRecord(Record record, int period) {
        List<Measurement> measurements = IButtonDataAnalysis.getListOfMeasurementsForPeriod(new ArrayList<>(record.getMeasurements()), period);

        XYChart.Series<Date, Number> serie = new XYChart.Series<Date, Number>();
        serie.setName(record.getPosition().getPlace());
        measurements.stream().forEach((measurement) -> {
            serie.getData().add(new XYChart.Data<Date, Number>(measurement.getDate(), measurement.getData()));
        });

        return serie;
    }

    private XYChart.Series<Date, Number> createSerieForFormula(Formula formula, int period) {

        XYChart.Series<Date, Number> serie = new XYChart.Series<Date, Number>();
        serie.setName(formula.getName());

        List<Measurement> measurements = IButtonDataAnalysis.getListOfMeasurementsForFormulaAndPeriod(new ArrayList<>(dataMap.keySet()), formula, period);

        measurements.stream().forEach((measurement) -> {
            serie.getData().add(new XYChart.Data<Date, Number>(measurement.getDate(), measurement.getData()));
        });

        for(Measurement measurement: measurements) {
            if (measurement.getData() == Double.NaN) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Some formulas cannot be calculated due to an error in the operation. Please check that selected formulas are correct.");
                alert.show();
                break;
            }
        }

        return serie;
    }

    public void setData(HashMap<Record, List<Measurement>> dataMap, Set<Formula> formulas) {
        this.dataMap = dataMap;
        this.formulas.addAll(formulas);
        this.records.addAll(dataMap.keySet());

        positionsList.getCheckModel().checkAll();
        reloadSelectedSeriesForPeriod(1);   // TODO check this solution...
    }

    @FXML
    public void saveAsPng() {

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {

            WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);

            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                // TODO: handle exception here
            }
        }
    }

    @Override
    public void translate() {

    }
}
