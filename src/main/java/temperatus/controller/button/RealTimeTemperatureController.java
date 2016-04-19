package temperatus.controller.button;

import com.dalsemi.onewire.container.OneWireContainer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.calculator.Calculator;
import temperatus.controller.AbstractController;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceRealTimeTempTask;
import temperatus.model.pojo.types.Device;
import temperatus.model.pojo.utils.DateAxis;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Show a graphic with the temperature read form the device in real time
 * <p/>
 * Created by alberto on 18/4/16.
 */
@Controller
public class RealTimeTemperatureController implements Initializable, AbstractController {

    @FXML
    private LineChart<Date, Number> lineChart;
    @FXML
    private DateAxis dateAxis;    // time of measurement
    @FXML
    private NumberAxis temperatureAxis;   // temperature read

    @FXML
    private TextField currentTemp;    // show the current temperature in text
    @FXML
    private RadioButton unitC;
    @FXML
    private RadioButton unitF;

    @Autowired
    DeviceRealTimeTempTask deviceRealTimeTempTask;   // read from device task
    @Autowired
    DeviceOperationsManager deviceOperationsManager;

    private Timeline fiveSecondsWonder;     // read temperature and show it in the graph every X seconds
    private XYChart.Series<Date, Number> serie = new XYChart.Series<>();
    private static final int period = 10;   // period of read device
    private ToggleGroup unitGroup = new ToggleGroup();

    private OneWireContainer container;     // device container

    private static Logger logger = LoggerFactory.getLogger(RealTimeTemperatureController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        unitGroup.getToggles().addAll(unitC, unitF);
        unitGroup.selectToggle(unitC);

        createTimeLineWithPeriod();
        lineChart.setData(FXCollections.observableArrayList(serie));
        serie.setName("Real-time Temperature");

    }

    void setDevice(Device device) {
        this.container = device.getContainer();
        deviceRealTimeTempTask.setContainer(container, device.getAdapterName(), device.getAdapterPort());
    }

    private void createTimeLineWithPeriod() {
        fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(period), event -> {
            if (!(VistaNavigator.getController() instanceof ConnectedDevicesController)) {
                fiveSecondsWonder.stop();
                serie.getData().clear();
            }

            ListenableFuture future = deviceOperationsManager.submitTask(deviceRealTimeTempTask);

            Futures.addCallback(future, new FutureCallback<Double>() {
                public void onSuccess(Double result) {
                    Platform.runLater(() -> {
                        addNewTemperature(result);
                    });
                }
                public void onFailure(Throwable thrown) {
                    logger.error("Error fetching temperature - Future error");
                }
            });
        }));

        fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
    }

    private void addNewTemperature(double temp) {
        double temperature = temp;

        if (unitGroup.getSelectedToggle().equals(unitF)) {
            temperature = Calculator.fahrenheitToCelsius(temperature);
        }

        serie.getData().add(new XYChart.Data<>(new Date(), temperature));
        currentTemp.setText(temperature + "");
        logger.info("Temperature read: " + temperature);
    }

    /**
     * When the tab is selected it will start to read the device
     */
    void startReading() {
        fiveSecondsWonder.play();
    }

    /**
     * When the tab is deselected it will stop reading and it will clear the previously readed data
     */
    void stopReading() {
        serie.getData().clear();
        fiveSecondsWonder.stop();
    }

    @Override
    public void translate() {

    }

}
