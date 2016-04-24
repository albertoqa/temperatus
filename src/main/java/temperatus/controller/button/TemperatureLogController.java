package temperatus.controller.button;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.utils.DateAxis;
import temperatus.util.ChartToolTip;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Modal window showing a graphic with the data of a selected device
 * Created by alberto on 24/4/16.
 */
@Controller
@Scope("prototype")
public class TemperatureLogController implements Initializable, AbstractController {

    @FXML private StackPane stackPane;
    @FXML private Label headerLabel;
    @FXML private Button backButton;

    @FXML private LineChart<Date, Number> lineChart;
    @FXML private DateAxis dateAxis;
    @FXML private NumberAxis temperatureAxis;

    private ObservableList<Measurement> measurements;
    private ObservableList<XYChart.Series<Date, Number>> series;

    private static Logger logger = LoggerFactory.getLogger(TemperatureLogController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        measurements = FXCollections.observableArrayList();
        series = FXCollections.observableArrayList();

        lineChart.setData(series);
        lineChart.setAnimated(false);
        dateAxis.setLabel("Time of measurement");
        temperatureAxis.setLabel("Temperature in ºC");
    }

    /**
     * Set the data to show on this view
     *
     * @param measurements list of measurements to show
     */
    public void setData(List<Measurement> measurements) {
        this.measurements.addAll(measurements);

        XYChart.Series<Date, Number> serie = new XYChart.Series<>();
        serie.setName("name");
        measurements.stream().forEach((measurement) -> serie.getData().add(new XYChart.Data<>(measurement.getDate(), measurement.getData())));

        ChartToolTip.addToolTipOnHover(serie, lineChart);
        series.add(serie);

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
        headerLabel.setText(language.get(Lang.TEMPERATURE_LOG));
        backButton.setText(language.get(Lang.BACK_BUTTON));
    }
}
