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
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Measurement;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 6/5/16.
 */
@Controller
public class HistogramController implements Initializable, AbstractController {

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    private XYChart.Data<String, Number>[] series;

    private final int NUM_BINS = 8;

    private static Logger logger = LoggerFactory.getLogger(HistogramController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setData(List<List<Measurement>> measurementsLists, double maxTemp, double minTemp) {

        int[] histogram = IButtonDataAnalysis.calcHistogram(measurementsLists, minTemp, maxTemp, NUM_BINS);
        final int binSize = (int) (maxTemp - minTemp)/NUM_BINS;

        yAxis.setLowerBound(0);
        yAxis.setUpperBound(50);
        yAxis.setTickUnit(NUM_BINS);

        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setBarGap(0);
        barChart.setCategoryGap(1);
        barChart.setVerticalGridLinesVisible(false);

        barChart.setTitle("Temperature Histogram");
        xAxis.setLabel("");
        //yAxis.setLabel("Temperature in ºC");
        //yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, "ºC"));

        // add starting data
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Data Series 1");
        //noinspection unchecked
        series = new XYChart.Data[NUM_BINS];
        String[] categories = new String[NUM_BINS];
        for (int i = 0; i < series.length; i++) {
            categories[i] = Integer.toString(i + 1);
            series[i] = new XYChart.Data<>(((int) (minTemp+(binSize*i))) + "ºC", histogram[i]);
            series1.getData().add(series[i]);
        }
        barChart.getData().add(series1);
    }


    @Override
    public void translate() {

    }
}
