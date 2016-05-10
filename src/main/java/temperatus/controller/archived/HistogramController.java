package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.analysis.IButtonDataAnalysis;
import temperatus.calculator.Calculator;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Show a temperature Histogram for the current mission
 * <p>
 * Created by alberto on 6/5/16.
 */
@Controller
public class HistogramController implements Initializable, AbstractController {

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final int NUM_BINS = 8;     // number of bins to generate

    private static Logger logger = LoggerFactory.getLogger(HistogramController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        xAxis.setLabel("");
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(Double.MAX_VALUE);
        yAxis.setTickUnit(NUM_BINS);

        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setBarGap(0);
        barChart.setCategoryGap(1);
        barChart.setVerticalGridLinesVisible(false);
    }

    /**
     * Set the list of measurements to calculate the histogram and generate it
     *
     * @param measurementsLists all the measurements of the mission
     * @param maxTemp           maximum temperature of the mission
     * @param minTemp           minimum temperature of the mission
     */
    public void setData(List<List<Measurement>> measurementsLists, double maxTemp, double minTemp) {
        logger.info("Generating histogram data...");

        Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C: Unit.F;

        int[] histogram = IButtonDataAnalysis.calcHistogram(measurementsLists, minTemp, maxTemp, NUM_BINS, unit);
        final int binSize = (int) (maxTemp - minTemp) / NUM_BINS;   // range of temperature of each bin

        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        if(unit.equals(Unit.F)) {
            minTemp = Calculator.celsiusToFahrenheit(minTemp);
        }

        //noinspection unchecked
        XYChart.Data<String, Number>[] series = new XYChart.Data[NUM_BINS];
        for (int i = 0; i < series.length; i++) {
            series[i] = new XYChart.Data<>(((int) (minTemp + (binSize * i))) + "ºC", histogram[i]);
            serie.getData().add(series[i]);
        }
        barChart.getData().add(serie);
    }


    @Override
    public void translate() {
        barChart.setTitle("Temperature Histogram");
    }
}
